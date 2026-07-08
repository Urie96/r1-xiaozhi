{
  pkgs,
  lib,
  config,
  ...
}:

{
  # ── Java ──
  # Must use JDK 11 (not newer) because Gradle 6.5's Groovy/ASM
  # can only read class files up to JDK 17 (version 61).
  # JDK 21+ produces version 65 which Groovy can't parse.
  # sourceCompatibility = VERSION_1_8 in app/build.gradle ensures
  # compiled bytecode targets JDK 8 for Android 5.1.
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk11;
  };
  # };

  # ── Android SDK ──
  # Uses devenv's built-in android module (handles licensing, unzipping, etc.)
  #   compileSdkVersion 30  → platform "30"
  #   targetSdkVersion 22   → platform "22"
  #   buildToolsVersion 29.0.3
  android = {
    enable = true;

    platforms.version = [
      "22"
      "30"
    ];

    buildTools.version = [ "30.0.3" ];
    # Note: dx tool removed from build-tools >= 31.0.0, so 30.0.3 is the last
    # version compatible with both AGP 4.1.1's AAPT2 and the dx DEX compiler.

    # No need for heavy components
    ndk.enable = false;
    emulator.enable = false;
    systemImages.enable = false;
    sources.enable = false;

    googleAPIs.enable = false;
    googleTVAddOns.enable = false;
    extras = [ ];
    extraLicenses = [ ];

    android-studio.enable = false;
    flutter.enable = false;
    reactNative.enable = false;
  };

  # ── Extra packages ──
  packages = with pkgs; [
    # ADB for device communication
    android-tools

    # zip / unzip for APK inspection
    zip
    unzip

    # Python 3 explicitly (for build_janus_v2.py)
    python3
  ];

  scripts.logcat.exec = ''
    #!/usr/bin/env bash
    DEVICE_IP="''${1:-192.168.5.9}"
    adb connect "$DEVICE_IP:5555"
    sleep 1
    adb logcat | grep -E 'Phicomm|NLUDispatcher|DEBUG|Persona|Eavesdrop|Wakeup|VAD|NativeANTEngine'
  '';

  enterShell = ''
    # Override local.properties with nix-provided SDK path
    echo "sdk.dir=''${ANDROID_HOME}" > "$DEVENV_ROOT/local.properties"

    echo ""
    echo "╔══════════════════════════════════════════════╗"
    echo "║  斐讯 R1 智能音箱 APK 开发环境                ║"
    echo "╠══════════════════════════════════════════════╣"
    echo "║  ANDROID_HOME:  ''${ANDROID_HOME}"
    echo "║  JAVA_HOME:     ''${JAVA_HOME}"
    echo "║  ADB:           $(adb version 2>/dev/null | head -1)"
    echo "╠══════════════════════════════════════════════╣"
    echo "║  build-apk <v>    Build Janus APK            ║"
    echo "║  deploy-apk <v>   Install APK to device      ║"
    echo "║  logcat [ip]      Tail device logs           ║"
    echo "╚══════════════════════════════════════════════╝"
    echo ""
  '';
}
