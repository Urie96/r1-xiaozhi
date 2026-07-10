# AGENTS.md — 斐讯 R1 智能音箱魔改项目总纲

> 这一个文件就是全部背景。新进来看这里即可，不必翻 docs/。

## 0. 一句话定位

斐讯 R1 智能音箱（Android 5.1，云知声/Unisound 方案）。原厂云服务器已下线，NLU/TTS 回复不可用。
**路线**：不刷机、不重写服务器，用 **Janus (CVE-2017-13156) 覆盖安装**单 APK：保留原厂唤醒/ASR/NLU/TTS native 引擎，在云知声 ANT pipeline 里插入自己的 Handler 拦截 NLU 事件、接管回复。

## 1. 铁律（违反必卡死）

**永远不要 hook `com/unisound/vui/engine/ApiHelper.interceptTTS`。** 那是 TTS 回调线程，持有 audio 管线临界锁，任何同步等待/HTTP/Thread.join 都会让整个 audio+wakeup+ASR 停摆，直到看门狗杀进程。

**所有耗时操作（HTTP、大模型调用）必须在 ANT pipeline 工作线程做**，即 `SimpleUserEventInboundHandler.eventReceived(evt, ctx)` 里（可放 `ThreadUtils.execute` 异步，也可同步）。这里独立工作线程，同步 HTTP 都安全。处理完用 `ctx.playTTS(text)` 主动播报。

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
| 1 | `./gradlew clean :app:assembleRelease` —— 编译 `java/` 魔改源码 + `java-original/` 原厂依赖源码（生成 R.java、解析 maven 依赖、javac 全树）| gradle 6.5 / AGP 4.1.1 / JDK 11 |
| 2 | 只挑 2 组 .class：`custom/**`、`ExampleANTEngineInitializer` | bash cp |
| 3 | .class → dex | `dx` (build-tools 30.0.3) |
| 4 | dex → smali | `tools/baksmali.jar` |
| 5 | 从 `device_original.apk` 提取原厂 classes.dex 并反编译为基线 smali，再将魔改 smali 注入（`custom/` 整包替换 + `ExampleANTEngineInitializer` 同名覆盖） | `unzip` + `baksmali.jar` + bash cp |
| 6 | `smali.jar a -o dist/classes_new_v<N>.dex <work_smali>` —— 整目录汇编成最终 dex | `tools/smali.jar` |
| 7 | `scripts/build_janus_v2.py device_original.apk dist/classes_new_v<N>.dex dist/janus_unisound_v<N>.apk` | python3 |

**为什么只注入这 2 组**：当前 Java 魔改只涉及 `custom/**` 和 `ExampleANTEngineInitializer` 这个同名覆盖入口。其余类直接使用从 `device_original.apk` 反编译出的原厂 smali，不重新编译。`custom/` 整包替换，所以删掉的魔改文件不会残留；`NLUDispatcher` / `MqttTransportChannel` 经核对不属于当前功能魔改，其可编译修正版仅放在 `java-original/` 提供编译依赖，最终 DEX 使用 `smali-base` 原厂版本。

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
| `dist/` | 构建产物目录：`janus_unisound.apk` + `classes_new.dex` |
| `app/src/main/java/` | **我们新增/修改的 Java 源**（最终魔改入口；当前仅 `custom/**` + `ExampleANTEngineInitializer`） |
| `app/src/main/java-original/` | 原厂反编译后、已修到可编译的依赖源码；参与 Gradle 编译，但不在这里开发魔改 |
| `app/src/main/java-jadx-reference/` | 从 `device_original.apk` 直接生成的完整 Jadx 原厂参考源码；不参与编译，只用于对照原厂实现 |
| `app/src/main/README.md` | Java 源码拆分说明、当前注入范围、开发约定 |
| `app/build.gradle` | `com.android.library`，compileSdk 30 / targetSdk 22 / minSdk 22 / JDK 1.8 / abi armeabi-v7a；`sourceSets.main.java.srcDirs=['src/main/java','src/main/java-original']` |
| `app/libs/` | `BaiduLBS_Android.jar`、`Baidu_Mtj...jar`、`zsd.jar` + `armeabi-v7a/`(so) |

## 7. 环境工具

- 进入环境：`devenv shell`（或已在 devenv shell 里）。JDK 11、Android platform-22 + 30、build-tools 30.0.3、adb、python3、zip/unzip 均就绪。
- **jadx**（反编译原厂 APK 取干净 Java）：`/nix/store/mrqpqjvw0nrqh5hagyy6djwvfrm9xhmx-jadx-1.5.5/bin/jadx`
- 原厂干净 smali 基准备在 `smali-base/`（由首次构建或 `just prepare-smali` 生成）。判定"原厂有没有某类/某引用"以它为准。
- R 类是 `com.phicomm.speaker.device.R`（aapt2 生成）。


## 10. 下次加新魔改的步骤

1. 在 `app/src/main/java/` 下新增/修改魔改代码；新 Handler 放 `app/src/main/java/com/phicomm/speaker/device/custom/handler/`。
2. 若要改原厂类，先从 `app/src/main/java-jadx-reference/` 复制到 `app/src/main/java/` 的相同包路径，再修正 Jadx 反编译问题并改代码。
3. 在 `ExampleANTEngineInitializer.onEngineInitDone()` `pipeline.addLast(new YourHandler())`，优先级 `setPriority` 设到高于要拦截的原厂 Handler。
4. 若新 Handler 引用新工具类，放在 `custom/` 下（整包会被注入）。
5. 若需改 NLU 分发/唤醒词逻辑才动 `NLUDispatcher`；若需改 MQTT 才动 `MqttTransportChannel`；否则别碰。新增同名覆盖类时，还必须在 `scripts/build-apk` 的 class 提取和 smali 注入阶段登记。
6. `build-apk` / `./scripts/build-apk` → `./scripts/install-apk`。

## 11. 踩坑备忘

- **jadx 反编译 Java 不能盲抄**：常见毛病 `nluList == 0`（List 与 0 比较）、工具类名 `e.a` vs `a.a`（混淆名不一致）、override 方法标成 `protected` 而父类是 `public`（编译报"试图分配更低访问权限"）、R 解析成 `com.unisound.vui.transport.R`（项目实际是 `com.phicomm.speaker.device.R`）。优先在已能编译的 FernFlower 版上做剥离，或抄 jadx 后逐一修这些。
- **不要 `ctx.cancelEngine()`**：会取消整个 ANT 引擎，无法再次唤醒。会话结束只调 `reset()` + 可选 `enterWakeup(false)`。
- 当前只有 `ExampleANTEngineInitializer` 是**同名覆盖**注入——魔改版在 `app/src/main/java/`，原厂参考版在 `app/src/main/java-jadx-reference/`。`NLUDispatcher` / `MqttTransportChannel` 不覆盖注入，最终使用 `smali-base` 原厂版本。
- `custom/` 是**整包 rm+cp**——删文件要在 `app/src/main/java/` 和必要的 `app/src/main/java-original/` 里删干净，别留孤立内部类 `.java`（Java 源里匿名类本就只在主文件，不会留孤立 .java；smali 里才有 `$1.smali`）。
- `multiDexEnabled false`：只能有一个 dex，所以全部类必须塞进从 `device_original.apk` 反编译 + 魔改注入后汇编出的那一个 dex。
