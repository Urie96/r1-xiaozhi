# ── 配置 ──────────────────────────────────────
device_serial := "192.168.2.13:5555"

# ── 构建 APK ──────────────────────────────────
build:
    ./scripts/build-apk

install:
    ./scripts/install-apk {{ device_serial }}

log:
    adb -s {{ device_serial }} logcat -v threadtime | grep -E "XiaoZhi|XiaoZhiSession|XiaoZhiStreamPlayer|XiaoZhiDataSource|ExoPlayer"

default:
    @echo "用法:"
    @echo "  just build                   构建 Janus APK（dist/janus_unisound.apk）"
    @echo "  just install                 安装 dist/janus_unisound.apk 并重启"
    @echo "  just log                     查看日志（过滤 XiaoZhi）"
    @echo ""
    @echo "设备 serial: {{ device_serial }}（在 Justfile 顶部修改）"
    @echo "提示: 可传 serial 覆盖: just install 192.168.2.13:5555"
