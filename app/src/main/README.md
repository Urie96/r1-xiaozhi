# Java 源码目录说明

这个项目把原厂源码和魔改源码物理分开，避免在全量反编译代码中误判改动。

| 目录 | 是否编译 | 用途 |
|---|---:|---|
| `java/` | 是 | **我们新增或修改的源码**。查看这个目录即可知道当前 Java 层魔改范围。 |
| `java-original/` | 是 | 原厂反编译后、已修到可编译的依赖源码；不应在这里开发魔改功能。 |
| `java-jadx-reference/` | 否 | 直接从 `device_original.apk` 生成的完整 Jadx 原厂参考源码，用于对照，不保证可编译。 |

## 当前注入范围

构建脚本最终只从编译产物提取：

1. `com/phicomm/speaker/device/custom/**`（整包替换；其中大部分原厂类来自 `java-original/`，新增类来自 `java/`）
2. `com/phicomm/speaker/device/ui/ExampleANTEngineInitializer`

第 2 项是同名覆盖，魔改版位于 `java/`，原厂版可在 `java-jadx-reference/` 的相同包路径下对照。`NLUDispatcher` / `MqttTransportChannel` 没有当前功能魔改，其可编译源码位于 `java-original/`，最终 DEX 直接保留 `smali-base` 原厂实现。

## 如何判断新增和修改

- `java/` 与 `java-jadx-reference/` 中存在相同相对路径：**修改原厂类（同名覆盖）**。当前是：
  - `com/phicomm/speaker/device/ui/ExampleANTEngineInitializer.java`
- 只在 `java/` 中存在：**我们新增的类**。当前是 `EchoHandler`、`XiaoZhiHandler` 和 `custom/xiaozhi/` 下的 7 个类。
- 只在 `java-original/` 或 `java-jadx-reference/` 中存在：**原厂类**。

## 开发约定

- 新增功能或修改原厂类：代码放入 `java/`。
- 需要修改原厂类时，先从 `java-jadx-reference/` 复制到 `java/` 的相同包路径，再修正 Jadx 反编译问题。
- `java-original/` 只用于提供可编译依赖；不要把新魔改写进去。
- 若新增同名覆盖类，还必须在 `scripts/build-apk` 的 class 提取和 smali 注入阶段登记。
- 判定设备中的权威原厂实现仍以 `smali-base/` 为准；Jadx Java 只用于阅读。
