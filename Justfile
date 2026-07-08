# ── 配置 ──────────────────────────────────────
device_serial := "192.168.2.13:5555"

# ── 构建 APK ──────────────────────────────────
build VERSION:
    ./scripts/build-apk {{ VERSION }}

install VERSION:
    ./scripts/install-apk {{ VERSION }} {{ device_serial }}

log:
    adb -s {{ device_serial }} logcat -v threadtime | grep -E "Phicomm|NLUDispatcher|EchoHandler|Wakeup|NativeANTEngine"

default:
    @echo "用法:"
    @echo "  just build <N>               构建 v<N> 的 Janus APK"
    @echo "  just install <N>             安装 dist/janus_unisound_v<N>.apk 并重启"
    @echo "  just log                     查看日志（过滤 Phicomm/NLU/Echo/Wakeup）"
    @echo ""
    @echo "设备 serial: {{ device_serial }}（在 Justfile 顶部修改）"
