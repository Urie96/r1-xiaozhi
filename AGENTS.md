# AGENTS.md — 斐讯 R1 智能音箱魔改项目总纲

> 这一个文件就是全部背景。新进来看这里即可，不必翻 docs/。

## 0. 一句话定位

斐讯 R1 智能音箱（Android 5.1，云知声/Unisound 方案）。原厂云服务器已下线，NLU/TTS 回复不可用。
**路线**：不刷机、不重写服务器，用 **Janus (CVE-2017-13156) 覆盖安装**单 APK：保留原厂唤醒/ASR/NLU/TTS native 引擎，在云知声 ANT pipeline 里插入自己的 Handler 拦截 NLU 事件、接管回复。

## 1. 铁律（违反必卡死）

**永远不要 hook `com/unisound/vui/engine/ApiHelper.interceptTTS`。** 那是 TTS 回调线程，持有 audio 管线临界锁，任何同步等待/HTTP/Thread.join 都会让整个 audio+wakeup+ASR 停摆，直到看门狗杀进程。v25~v27 多个版本验证死路一条。

**所有耗时操作（HTTP、大模型调用）必须在 ANT pipeline 工作线程做**，即 `SimpleUserEventInboundHandler.eventReceived(evt, ctx)` 里（可放 `ThreadUtils.execute` 异步，也可同步）。这里独立工作线程，同步 HTTP 都安全。处理完用 `ctx.playTTS(text)` 主动播报。

原 `original_v2/check_v17/` 就是当年那条死路的冻结标本，已于重构时删除。

## 2. 架构：ANT pipeline（Netty 风格）

```
ANTEngine
  └─ pipeline (ANTPipeline)
        ├─ NLUDispatcher          (NLU 分发)
        ├─ EchoHandler            ← 我们插入的拦截点
        ├─ PhicommDataStatisticHandler
        ├─ ... 原厂各业务 Handler (天气/音乐/提醒/闲聊/设置...)
        └─ DefaultUnSupportHandler
```

- NLU 事件（ASR 识别+语义结果）走 inbound 链路依次喂给每个 Handler。
- 每个 Handler：`acceptInboundEvent0(evt)` 返回 true = **消费**（下游收不到）；`eventReceived(evt, ctx)` 在工作线程回调。
- **优先级**：`initPriority()` 里 `setPriority(int)`，数值越大越先收到。EchoHandler = 1000（全局最高，吃掉所有 NLU）。
- 入口：`ExampleANTEngineInitializer.onEngineInitDone()` 里 `pipeline.addLast(...)` 注册所有 Handler。

## 3. 写一个 Handler 的 API 速查

```java
public class XxxHandler extends SimpleUserEventInboundHandler<NLU> {  // 或 NLU<Intent,Result>
    public XxxHandler() { this.sessionName = SessionRegister.SESSION_CHAT; }  // 或 SESSION_WEATHER 等
    @Override public void initPriority() { setPriority(310); }                // > 原厂同名(300)
    @Override public boolean onASREventEngineInitDone(ANTHandlerContext ctx) { this.ctx = ctx; return super.onASREventEngineInitDone(ctx); }
    @Override public boolean acceptInboundEvent0(NLU evt) { return evt != null && ...; }  // true=拦截
    @Override public void eventReceived(NLU evt, ANTHandlerContext ctx) {       // 工作线程,可 HTTP
        String text = evt.getText();        // ASR 识别文本
        String svc   = evt.getService();    // 意图 service 名
        ctx.stopWakeup(); ctx.stopASR();
        ctx.playTTS(text);                  // 主动播报
    }
    @Override public boolean onTTSEventPlayingEnd(ANTHandlerContext ctx) { exit(); return true; }  // 播完回唤醒
    @Override public void doInterrupt(ANTHandlerContext ctx, String type) { ctx.cancelTTS(); ...; reset(); }
    // 切勿 ctx.cancelEngine() —— 会取消整个 ANT 引擎导致无法再次唤醒
}
```
关键 ctx 方法：`playTTS/stopWakeup/stopASR/enterWakeup(false)/enterASR/cancelTTS`。
父类字段：`protected boolean eventReceived`（是否已收到事件）、`this.ctx`。
常量：`ExoConstants.DO_ONE_SHOT_INTERRUPT`。
NLU 包：`nluparser.scheme.NLU`、`nluparser.scheme.SName`（service 名常量）。

## 4. 构建链路（`build-apk <N>`，定义在 `devenv.nix`）

只此一条命令产出 `janus_unisound_v<N>.apk`。当前用 `build-apk 5`。

| 步 | 动作 | 工具 |
|---|---|---|
| 1 | `./gradlew clean :app:assembleRelease` —— 编译整个 app/src（生成 R.java、解析 maven 依赖、javac 全树）| gradle 6.5 / AGP 4.1.1 / JDK 11 |
| 2 | 只挑 4 组 .class：`custom/**`、`ExampleANTEngineInitializer`、`NLUDispatcher`、`MqttTransportChannel` | bash cp |
| 3 | .class → dex | `dx` (build-tools 30.0.3) |
| 4 | dex → smali | `tools/baksmali.jar` |
| 5 | 从 `device_original.apk` 提取原厂 classes.dex 并反编译为基线 smali，再将魔改 smali 注入（`custom/` 整包替换 + 3 组同名覆盖） | `unzip` + `baksmali.jar` + bash cp |
| 6 | `smali.jar a -o dist/classes_new_v<N>.dex <work_smali>` —— 整目录汇编成最终 dex | `tools/smali.jar` |
| 7 | `scripts/build_janus_v2.py device_original.apk dist/classes_new_v<N>.dex dist/janus_unisound_v<N>.apk` | python3 |

**为什么只注入这 4 组**：整个项目只有这 4 处被魔改触碰过。其余类与原厂一字不差，直接从 `device_original.apk` 反编译出的原厂 smali，不必重编（反编译源码重编易失真/报错）。`custom/` 整包换所以删掉的魔改文件不会残留；`ExampleANTEngineInitializer`/`NLUDispatcher`/`MqttTransportChannel` 同名覆盖，故还原时必须改回原厂行为。

**gradle 只在第 1 步需要**（顺带做 aapt2 生成 R + maven 依赖解析 + 整树 javac）。第 3~7 步是 baksmali/smali/python 独立工具，与 gradle 无关。

## 5. Janus 缝合原理（第 7 步）

```
[ 我们的 DEX ][ 原厂 APK 的 ZIP 主体(manifest/res/assets/lib/*.so/原厂classes.dex) ]
```
- 把我们 dex 顶到原厂 APK 前；
- 修 ZIP 的 EOCD + Central Directory 各 entry 本地偏移（+dex 长度）；
- DEX 头 `file_size` = 整个文件总大小，重算 adler32 + SHA-1。
效果：安装器按 APK 装（资源/so/manifest 全用原厂）；dalvik 读文件头按 DEX 加载 → **跑我们的 dex，原厂 classes.dex 被遮蔽**。我们 dex 含原厂全部类(从 smali 重建)+魔改类，功能完整。

## 6. 关键文件/目录

| 路径 | 角色 |
|---|---|
| 路径 | 角色 |
|---|---|
| `device_original.apk` | 原厂 APK 外壳（ZIP：manifest/res/assets/lib/*.so + 原厂 classes.dex） |
| `smali-base/` | 原厂 smali 基准备（~5307 文件），由构建脚本或 `just prepare-smali` 自动生成。持久保留，不会被 gradle clean 删除 |
| `tools/smali.jar` / `tools/baksmali.jar` | dex↔smali (2.5.2) |
| `scripts/build_janus_v2.py` | Janus 缝合脚本 |
| `scripts/build-apk` | 一键构建脚本（第 1~7 步） |
| `scripts/install-apk` | APK 安装脚本 |
| `dist/` | 构建产物目录：`janus_unisound_v<N>.apk` + `classes_new_v<N>.dex` |
| `app/src/main/java/` | 全部反编译 Java 源（FernFlower）。**只有 4 组被注入**，其余仅为让 gradle 编译通过 |
| `app/build.gradle` | `com.android.library`，compileSdk 30 / targetSdk 22 / minSdk 22 / JDK 1.8 / abi armeabi-v7a |
| `app/libs/` | `BaiduLBS_Android.jar`、`Baidu_Mtj...jar`、`zsd.jar` + `armeabi-v7a/`(so) |
| `devenv.nix` | 环境 + `build-apk`/`deploy-apk`/`logcat` 脚本定义 |
| `docs/` | 历史设计文档（天气改造、插嘴模式、唤醒词、成语接龙等，多数对应已删功能） |
| `Justfile` | 常用命令：`just prepare-smali`、`just build <N>`、`just install <apk>` |

## 7. 环境工具

- 进入环境：`devenv shell`（或已在 devenv shell 里）。JDK 11、Android platform-22 + 30、build-tools 30.0.3、adb、python3、zip/unzip 均就绪。
- **jadx**（反编译原厂 APK 取干净 Java）：`/nix/store/mrqpqjvw0nrqh5hagyy6djwvfrm9xhmx-jadx-1.5.5/bin/jadx`
- 原厂干净 smali 基准备在 `smali-base/`（由首次构建或 `just prepare-smali` 生成）。判定"原厂有没有某类/某引用"以它为准。
- R 类是 `com.phicomm.speaker.device.R`（aapt2 生成）。

## 8. 当前魔改状态（本次会话产出）

**已删除全部旧魔改**，回到干净原厂基线，再实现**唯一一个魔改：鹦鹉/Echo**。

- 删除：`custom/ai/`、`custom/config/AIConfig`、`custom/engine/`(Eavesdropper*、PlaybackStateMonitor、VadAudioDetector、USCCredentialExtractor)、`custom/event/`、`custom/persona/`、`custom/music/NetEaseMusicClient`、`custom/tts/XfyunTtsClient`、`custom/util/EventLog`、`custom/handler/{Eavesdropper,PersonaRouter,PhicommChat,PhicommMusicSearch,PhicommWeather}Handler`、`utils/PhicommUtils`。
- 还原回原厂行为（用 jadx 原版 + 修访问修饰符，或在 FernFlower 版上剥离魔改）：`PhicommInitializeHandler`、`PhicommMusicHandler`、`unisound/sdk/an.java`、`NLUDispatcher`、`ExampleANTEngineInitializer`。
- 构建脚本改为每次从 `device_original.apk` 新鲜反编译基线 smali，不再依赖 `build/smali-base` 缓存。
- **新增 `app/src/main/java/com/phicomm/speaker/device/custom/handler/EchoHandler.java`**：
  - `extends SimpleUserEventInboundHandler<NLU>`，优先级 1000，session=SESSION_CHAT。
  - `acceptInboundEvent0`：消费所有带文本的 NLU。
  - `eventReceived`：`stopWakeup/stopASR/playTTS(getText())` —— 用户说什么 TTS 就复述什么。
  - `onTTSEventPlayingEnd`→回唤醒；`doInterrupt`→cancelTTS+reset。
- 在 `ExampleANTEngineInitializer` 的 `NLUDispatcher` 之后注册 `new EchoHandler()`。
- 产物 `janus_unisound_v5.apk` 已成功构建（≈31MB），EchoHandler.smali 已注入最终 dex。

行为：唤醒 → 说话 → ASR → EchoHandler 拦截(其余 Handler 收不到) → 原样 TTS 复述 → 回唤醒。

## 9. 部署到设备

```bash
# 一键安装 + 重启
just install 5

# 指定 serial（临时覆盖 Justfile 配置）
./scripts/install-apk 5 192.168.5.9:5555
./scripts/install-apk 5 1234567890abcdef  # USB 设备

# 看日志:
just log
```
- **serial 在 `Justfile` 顶部 `device_serial := "192.168.5.9:5555"` 配置**，一处改处处生效
- TCP 格式（含 `:`）会自动先 `adb connect`，USB 格式直接用 `adb -s`
- 不传 serial 时自动检测：单设备直接安装，多设备报错要求指定
- `just install <N>` 自动完成：连接(若TCP)→推包→安装→清缓存→重启

## 10. 下次加新魔改的步骤

1. 在 `app/src/main/java/com/phicomm/speaker/device/custom/handler/` 加 Handler（参考 EchoHandler 或 `docs/` 里的 WeatherHandler 模板）。
2. 在 `ExampleANTEngineInitializer.onEngineInitDone()` `pipeline.addLast(new YourHandler())`，优先级 `setPriority` 设到高于要拦截的原厂 Handler。
3. 若新 Handler 引用新工具类，放在 `custom/` 下（整包会被注入）。
4. 若需改 NLU 分发/唤醒词逻辑才动 `NLUDispatcher`；否则别碰。
5. `build-apk <N>` → `deploy-apk <N>`。

## 11. 踩坑备忘

- **jadx 反编译 Java 不能盲抄**：常见毛病 `nluList == 0`（List 与 0 比较）、工具类名 `e.a` vs `a.a`（混淆名不一致）、override 方法标成 `protected` 而父类是 `public`（编译报"试图分配更低访问权限"）、R 解析成 `com.unisound.vui.transport.R`（项目实际是 `com.phicomm.speaker.device.R`）。优先在已能编译的 FernFlower 版上做剥离，或抄 jadx 后逐一修这些。
- **不要 `ctx.cancelEngine()`**：会取消整个 ANT 引擎，无法再次唤醒。会话结束只调 `reset()` + 可选 `enterWakeup(false)`。
- `ExampleANTEngineInitializer` / `NLUDispatcher` / `MqttTransportChannel` 是**同名覆盖**注入——还原它们时必须保证行为=原厂，否则把魔改带进 dex。
- `custom/` 是**整包 rm+cp**——删文件要在 app/src 里删干净，别留孤立内部类 `.java`（Java 源里匿名类本就只在主文件，不会留孤立 .java；smali 里才有 `$1.smali`）。
- `multiDexEnabled false`：只能有一个 dex，所以全部类必须塞进从 `device_original.apk` 反编译 + 魔改注入后汇编出的那一个 dex。
