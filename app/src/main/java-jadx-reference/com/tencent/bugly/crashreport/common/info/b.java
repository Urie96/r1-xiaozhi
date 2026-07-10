package com.tencent.bugly.crashreport.common.info;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import cn.yunzhisheng.asr.JniUscClient;
import cn.yunzhisheng.common.PinyinConverter;
import com.tencent.bugly.proguard.x;
import com.tencent.bugly.proguard.z;
import com.unisound.client.SpeechConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Locale;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class b {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private static String f142a = null;
    private static String b = null;

    public static String a() {
        try {
            return Build.MODEL;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return "fail";
        }
    }

    public static String b() {
        try {
            return Build.VERSION.RELEASE;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return "fail";
        }
    }

    public static int c() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return -1;
        }
    }

    public static String a(Context context) {
        String deviceId;
        if (context == null) {
            return null;
        }
        if (!AppInfo.a(context, SpeechConstants.PERMISSION_READ_PHONE_STATE)) {
            x.d("no READ_PHONE_STATE permission to get IMEI", new Object[0]);
            return null;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager != null) {
                deviceId = telephonyManager.getDeviceId();
                if (deviceId != null) {
                    try {
                        deviceId = deviceId.toLowerCase();
                    } catch (Throwable th) {
                        x.a("Failed to get IMEI.", new Object[0]);
                    }
                }
            } else {
                deviceId = null;
            }
        } catch (Throwable th2) {
            deviceId = null;
        }
        return deviceId;
    }

    public static String b(Context context) {
        String subscriberId;
        if (context == null) {
            return null;
        }
        if (!AppInfo.a(context, SpeechConstants.PERMISSION_READ_PHONE_STATE)) {
            x.d("no READ_PHONE_STATE permission to get IMSI", new Object[0]);
            return null;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager != null) {
                subscriberId = telephonyManager.getSubscriberId();
                if (subscriberId != null) {
                    try {
                        subscriberId = subscriberId.toLowerCase();
                    } catch (Throwable th) {
                        x.a("Failed to get IMSI.", new Object[0]);
                    }
                }
            } else {
                subscriberId = null;
            }
        } catch (Throwable th2) {
            subscriberId = null;
        }
        return subscriberId;
    }

    public static String c(Context context) {
        String str = "fail";
        if (context == null) {
            return "fail";
        }
        try {
            String string = Settings.Secure.getString(context.getContentResolver(), "android_id");
            if (string == null) {
                return JniUscClient.az;
            }
            try {
                return string.toLowerCase();
            } catch (Throwable th) {
                str = string;
                th = th;
                if (!x.a(th)) {
                    x.a("Failed to get Android ID.", new Object[0]);
                    return str;
                }
                return str;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0025 A[Catch: Throwable -> 0x0072, TryCatch #0 {Throwable -> 0x0072, blocks: (B:11:0x001d, B:13:0x0025, B:15:0x003c, B:17:0x0044, B:19:0x004c), top: B:30:0x001d }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String d(Context context) {
        String macAddress;
        WifiManager wifiManager;
        WifiInfo connectionInfo;
        String str = "fail";
        if (context == null) {
            return "fail";
        }
        try {
            wifiManager = (WifiManager) context.getSystemService("wifi");
        } catch (Throwable th) {
            th = th;
        }
        if (wifiManager == null || (connectionInfo = wifiManager.getConnectionInfo()) == null) {
            macAddress = str;
        } else {
            macAddress = connectionInfo.getMacAddress();
            if (macAddress != null) {
                try {
                    if (macAddress.equals("02:00:00:00:00:00")) {
                        String strA = z.a(context, "wifi.interface");
                        x.c("MAC interface: %s", strA);
                        NetworkInterface byName = NetworkInterface.getByName(strA);
                        if (byName == null) {
                            byName = NetworkInterface.getByName("wlan0");
                        }
                        if (byName == null) {
                            byName = NetworkInterface.getByName("eth0");
                        }
                        if (byName != null) {
                            macAddress = z.d(byName.getHardwareAddress());
                        }
                    }
                } catch (Throwable th2) {
                    str = macAddress;
                    th = th2;
                    if (!x.a(th)) {
                        th.printStackTrace();
                    }
                    macAddress = str;
                }
            }
        }
        if (macAddress == null) {
            macAddress = JniUscClient.az;
        }
        x.c("MAC address: %s", macAddress);
        return macAddress.toLowerCase();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public static String e(Context context) {
        String simSerialNumber;
        if (context == null) {
            return "fail";
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager == null) {
                simSerialNumber = "fail";
            } else {
                simSerialNumber = telephonyManager.getSimSerialNumber();
                if (simSerialNumber == null) {
                    simSerialNumber = JniUscClient.az;
                }
            }
        } catch (Throwable th) {
            simSerialNumber = "fail";
            x.a("Failed to get SIM serial number.", new Object[0]);
        }
        return simSerialNumber;
    }

    public static String d() {
        try {
            return Build.SERIAL;
        } catch (Throwable th) {
            x.a("Failed to get hardware serial number.", new Object[0]);
            return "fail";
        }
    }

    private static boolean p() {
        try {
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
        }
        if (Environment.getExternalStorageState().equals("mounted")) {
            return true;
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:78:0x0098 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:85:0x009d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static String q() throws Throwable {
        BufferedReader bufferedReader;
        FileReader fileReader;
        String str;
        try {
            fileReader = new FileReader("/system/build.prop");
            try {
                bufferedReader = new BufferedReader(fileReader, 2048);
                while (true) {
                    try {
                        try {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                str = null;
                                break;
                            }
                            String[] strArrSplit = line.split("=", 2);
                            if (strArrSplit.length == 2) {
                                if (strArrSplit[0].equals("ro.product.cpu.abilist")) {
                                    str = strArrSplit[1];
                                    break;
                                }
                                if (strArrSplit[0].equals("ro.product.cpu.abi")) {
                                    str = strArrSplit[1];
                                    break;
                                }
                            }
                        } catch (Throwable th) {
                            th = th;
                            if (!x.a(th)) {
                                th.printStackTrace();
                            }
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e) {
                                    if (!x.a(e)) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (fileReader != null) {
                                try {
                                    fileReader.close();
                                } catch (IOException e2) {
                                    if (!x.a(e2)) {
                                        e2.printStackTrace();
                                    }
                                }
                            }
                            return null;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e3) {
                                if (!x.a(e3)) {
                                    e3.printStackTrace();
                                }
                            }
                        }
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException e4) {
                                if (!x.a(e4)) {
                                    e4.printStackTrace();
                                }
                            }
                        }
                        throw th;
                    }
                }
                if (str != null) {
                    str = str.split(",")[0];
                }
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                    if (!x.a(e5)) {
                        e5.printStackTrace();
                    }
                }
                try {
                    fileReader.close();
                    return str;
                } catch (IOException e6) {
                    if (!x.a(e6)) {
                        e6.printStackTrace();
                        return str;
                    }
                    return str;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = null;
                if (bufferedReader != null) {
                }
                if (fileReader != null) {
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            bufferedReader = null;
            fileReader = null;
        }
    }

    public static String a(boolean z) {
        String strQ = null;
        if (z) {
            try {
                strQ = q();
            } catch (Throwable th) {
                if (!x.a(th)) {
                    th.printStackTrace();
                }
                return "fail";
            }
        }
        if (strQ == null) {
            strQ = System.getProperty("os.arch");
        }
        return strQ;
    }

    public static long e() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            return ((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize());
        } catch (Throwable th) {
            if (x.a(th)) {
                return -1L;
            }
            th.printStackTrace();
            return -1L;
        }
    }

    public static long f() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
        } catch (Throwable th) {
            if (x.a(th)) {
                return -1L;
            }
            th.printStackTrace();
            return -1L;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:75:0x00aa A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:81:0x00a5 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static long g() throws Throwable {
        FileReader fileReader;
        BufferedReader bufferedReader;
        FileReader fileReader2;
        BufferedReader bufferedReader2 = null;
        try {
            fileReader2 = new FileReader("/proc/meminfo");
            try {
                bufferedReader = new BufferedReader(fileReader2, 2048);
            } catch (Throwable th) {
                th = th;
                fileReader = fileReader2;
            }
        } catch (Throwable th2) {
            th = th2;
            fileReader = null;
        }
        try {
            String line = bufferedReader.readLine();
            if (line == null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    if (!x.a(e)) {
                        e.printStackTrace();
                    }
                }
                try {
                    fileReader2.close();
                } catch (IOException e2) {
                    if (!x.a(e2)) {
                        e2.printStackTrace();
                    }
                }
                return -1L;
            }
            long j = Long.parseLong(line.split(":\\s+", 2)[1].toLowerCase().replace("kb", "").trim()) << 10;
            try {
                bufferedReader.close();
            } catch (IOException e3) {
                if (!x.a(e3)) {
                    e3.printStackTrace();
                }
            }
            try {
                fileReader2.close();
                return j;
            } catch (IOException e4) {
                if (x.a(e4)) {
                    return j;
                }
                e4.printStackTrace();
                return j;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedReader2 = bufferedReader;
            fileReader = fileReader2;
            try {
                if (!x.a(th)) {
                    th.printStackTrace();
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e5) {
                        if (!x.a(e5)) {
                            e5.printStackTrace();
                        }
                    }
                }
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e6) {
                        if (!x.a(e6)) {
                            e6.printStackTrace();
                        }
                    }
                }
                return -2L;
            } catch (Throwable th4) {
                th = th4;
                fileReader2 = fileReader;
                bufferedReader = bufferedReader2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e7) {
                        if (!x.a(e7)) {
                            e7.printStackTrace();
                        }
                    }
                }
                if (fileReader2 != null) {
                    try {
                        fileReader2.close();
                    } catch (IOException e8) {
                        if (!x.a(e8)) {
                            e8.printStackTrace();
                        }
                    }
                }
                throw th;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:112:0x013b A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:122:0x0136 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static long h() throws Throwable {
        BufferedReader bufferedReader;
        FileReader fileReader;
        BufferedReader bufferedReader2;
        FileReader fileReader2 = null;
        try {
            fileReader = new FileReader("/proc/meminfo");
            try {
                bufferedReader = new BufferedReader(fileReader, 2048);
            } catch (Throwable th) {
                th = th;
                bufferedReader = null;
            }
            try {
                bufferedReader.readLine();
                String line = bufferedReader.readLine();
                if (line == null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        if (!x.a(e)) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        fileReader.close();
                        return -1L;
                    } catch (IOException e2) {
                        if (x.a(e2)) {
                            return -1L;
                        }
                        e2.printStackTrace();
                        return -1L;
                    }
                }
                long j = 0 + (Long.parseLong(line.split(":\\s+", 2)[1].toLowerCase().replace("kb", "").trim()) << 10);
                String line2 = bufferedReader.readLine();
                if (line2 == null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                        if (!x.a(e3)) {
                            e3.printStackTrace();
                        }
                    }
                    try {
                        fileReader.close();
                        return -1L;
                    } catch (IOException e4) {
                        if (x.a(e4)) {
                            return -1L;
                        }
                        e4.printStackTrace();
                        return -1L;
                    }
                }
                long j2 = j + (Long.parseLong(line2.split(":\\s+", 2)[1].toLowerCase().replace("kb", "").trim()) << 10);
                String line3 = bufferedReader.readLine();
                if (line3 == null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e5) {
                        if (!x.a(e5)) {
                            e5.printStackTrace();
                        }
                    }
                    try {
                        fileReader.close();
                        return -1L;
                    } catch (IOException e6) {
                        if (x.a(e6)) {
                            return -1L;
                        }
                        e6.printStackTrace();
                        return -1L;
                    }
                }
                long j3 = (Long.parseLong(line3.split(":\\s+", 2)[1].toLowerCase().replace("kb", "").trim()) << 10) + j2;
                try {
                    bufferedReader.close();
                } catch (IOException e7) {
                    if (!x.a(e7)) {
                        e7.printStackTrace();
                    }
                }
                try {
                    fileReader.close();
                    return j3;
                } catch (IOException e8) {
                    if (x.a(e8)) {
                        return j3;
                    }
                    e8.printStackTrace();
                    return j3;
                }
            } catch (Throwable th2) {
                th = th2;
                if (bufferedReader != null) {
                }
                if (fileReader != null) {
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
            fileReader = null;
        }
    }

    public static long i() {
        if (!p()) {
            return 0L;
        }
        try {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int blockSize = statFs.getBlockSize();
            return ((long) blockSize) * ((long) statFs.getBlockCount());
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return -2L;
        }
    }

    public static long j() {
        if (!p()) {
            return 0L;
        }
        try {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int blockSize = statFs.getBlockSize();
            return ((long) blockSize) * ((long) statFs.getAvailableBlocks());
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return -2L;
        }
    }

    public static String k() {
        try {
            return Locale.getDefault().getCountry();
        } catch (Throwable th) {
            if (x.a(th)) {
                return "fail";
            }
            th.printStackTrace();
            return "fail";
        }
    }

    public static String l() {
        try {
            return Build.BRAND;
        } catch (Throwable th) {
            if (x.a(th)) {
                return "fail";
            }
            th.printStackTrace();
            return "fail";
        }
    }

    public static String f(Context context) {
        String str;
        TelephonyManager telephonyManager;
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo == null) {
                return null;
            }
            if (activeNetworkInfo.getType() == 1) {
                return "WIFI";
            }
            if (activeNetworkInfo.getType() != 0 || (telephonyManager = (TelephonyManager) context.getSystemService("phone")) == null) {
                str = "unknown";
            } else {
                int networkType = telephonyManager.getNetworkType();
                switch (networkType) {
                    case 1:
                        return "GPRS";
                    case 2:
                        return "EDGE";
                    case 3:
                        return "UMTS";
                    case 4:
                        return "CDMA";
                    case 5:
                        return "EVDO_0";
                    case 6:
                        return "EVDO_A";
                    case 7:
                        return "1xRTT";
                    case 8:
                        return "HSDPA";
                    case 9:
                        return "HSUPA";
                    case 10:
                        return "HSPA";
                    case 11:
                        return "iDen";
                    case 12:
                        return "EVDO_B";
                    case 13:
                        return "LTE";
                    case 14:
                        return "eHRPD";
                    case 15:
                        return "HSPA+";
                    default:
                        str = "MOBILE(" + networkType + ")";
                        break;
                }
            }
            return str;
        } catch (Exception e) {
            if (x.a(e)) {
                return "unknown";
            }
            e.printStackTrace();
            return "unknown";
        }
    }

    public static String g(Context context) throws Throwable {
        String strA = z.a(context, "ro.miui.ui.version.name");
        if (!z.a(strA) && !strA.equals("fail")) {
            return "XiaoMi/MIUI/" + strA;
        }
        String strA2 = z.a(context, "ro.build.version.emui");
        if (!z.a(strA2) && !strA2.equals("fail")) {
            return "HuaWei/EMOTION/" + strA2;
        }
        String strA3 = z.a(context, "ro.lenovo.series");
        if (!z.a(strA3) && !strA3.equals("fail")) {
            return "Lenovo/VIBE/" + z.a(context, "ro.build.version.incremental");
        }
        String strA4 = z.a(context, "ro.build.nubia.rom.name");
        if (!z.a(strA4) && !strA4.equals("fail")) {
            return "Zte/NUBIA/" + strA4 + "_" + z.a(context, "ro.build.nubia.rom.code");
        }
        String strA5 = z.a(context, "ro.meizu.product.model");
        if (!z.a(strA5) && !strA5.equals("fail")) {
            return "Meizu/FLYME/" + z.a(context, "ro.build.display.id");
        }
        String strA6 = z.a(context, "ro.build.version.opporom");
        if (!z.a(strA6) && !strA6.equals("fail")) {
            return "Oppo/COLOROS/" + strA6;
        }
        String strA7 = z.a(context, "ro.vivo.os.build.display.id");
        if (!z.a(strA7) && !strA7.equals("fail")) {
            return "vivo/FUNTOUCH/" + strA7;
        }
        String strA8 = z.a(context, "ro.aa.romver");
        if (!z.a(strA8) && !strA8.equals("fail")) {
            return "htc/" + strA8 + MqttTopic.TOPIC_LEVEL_SEPARATOR + z.a(context, "ro.build.description");
        }
        String strA9 = z.a(context, "ro.lewa.version");
        if (!z.a(strA9) && !strA9.equals("fail")) {
            return "tcl/" + strA9 + MqttTopic.TOPIC_LEVEL_SEPARATOR + z.a(context, "ro.build.display.id");
        }
        String strA10 = z.a(context, "ro.gn.gnromvernumber");
        if (!z.a(strA10) && !strA10.equals("fail")) {
            return "amigo/" + strA10 + MqttTopic.TOPIC_LEVEL_SEPARATOR + z.a(context, "ro.build.display.id");
        }
        String strA11 = z.a(context, "ro.build.tyd.kbstyle_version");
        if (!z.a(strA11) && !strA11.equals("fail")) {
            return "dido/" + strA11;
        }
        return z.a(context, "ro.build.fingerprint") + MqttTopic.TOPIC_LEVEL_SEPARATOR + z.a(context, "ro.build.rom.id");
    }

    public static String h(Context context) {
        return z.a(context, "ro.board.platform");
    }

    public static boolean i(Context context) throws Throwable {
        boolean zExists;
        try {
            zExists = new File("/system/app/Superuser.apk").exists();
        } catch (Throwable th) {
            if (!x.b(th)) {
                th.printStackTrace();
            }
            zExists = false;
        }
        Boolean bool = null;
        ArrayList<String> arrayListA = z.a(context, new String[]{"/system/bin/sh", "-c", "type su"});
        if (arrayListA != null && arrayListA.size() > 0) {
            for (String str : arrayListA) {
                x.c(str, new Object[0]);
                bool = str.contains("not found") ? false : bool;
            }
            if (bool == null) {
                bool = true;
            }
        }
        return (Build.TAGS != null && Build.TAGS.contains("test-keys")) || zExists || Boolean.valueOf(bool == null ? false : bool.booleanValue()).booleanValue();
    }

    /* JADX WARN: Removed duplicated region for block: B:21:0x0069 A[Catch: all -> 0x00ab, Throwable -> 0x00bb, TRY_LEAVE, TryCatch #1 {Throwable -> 0x00bb, blocks: (B:19:0x0057, B:21:0x0069), top: B:65:0x0057 }] */
    /* JADX WARN: Removed duplicated region for block: B:61:0x00be  */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0084 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:71:0x0091 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:73:0x009f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String m() throws Throwable {
        BufferedReader bufferedReader;
        Throwable th;
        BufferedReader bufferedReader2;
        String string = null;
        try {
            StringBuilder sb = new StringBuilder();
            if (new File("/sys/block/mmcblk0/device/type").exists()) {
                bufferedReader2 = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/type"));
                try {
                    try {
                        String line = bufferedReader2.readLine();
                        if (line != null) {
                            sb.append(line);
                        }
                        bufferedReader2.close();
                        bufferedReader = bufferedReader2;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = bufferedReader2;
                        if (bufferedReader != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    if (bufferedReader2 != null) {
                    }
                    return string;
                }
            } else {
                bufferedReader = null;
            }
            try {
                try {
                    sb.append(",");
                    if (new File("/sys/block/mmcblk0/device/name").exists()) {
                        BufferedReader bufferedReader3 = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/name"));
                        try {
                            String line2 = bufferedReader3.readLine();
                            if (line2 != null) {
                                sb.append(line2);
                            }
                            bufferedReader3.close();
                            bufferedReader = bufferedReader3;
                            try {
                                sb.append(",");
                                if (new File("/sys/block/mmcblk0/device/cid").exists()) {
                                    bufferedReader2 = bufferedReader;
                                } else {
                                    bufferedReader2 = new BufferedReader(new FileReader("/sys/block/mmcblk0/device/cid"));
                                    try {
                                        String line3 = bufferedReader2.readLine();
                                        if (line3 != null) {
                                            sb.append(line3);
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        bufferedReader = bufferedReader2;
                                        if (bufferedReader != null) {
                                            try {
                                                bufferedReader.close();
                                            } catch (IOException e) {
                                                x.a(e);
                                            }
                                        }
                                        throw th;
                                    }
                                }
                                try {
                                    string = sb.toString();
                                    if (bufferedReader2 != null) {
                                        try {
                                            bufferedReader2.close();
                                        } catch (IOException e2) {
                                            x.a(e2);
                                        }
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    bufferedReader = bufferedReader2;
                                    if (bufferedReader != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th6) {
                                bufferedReader2 = bufferedReader;
                                if (bufferedReader2 != null) {
                                    try {
                                        bufferedReader2.close();
                                    } catch (IOException e3) {
                                        x.a(e3);
                                    }
                                }
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            bufferedReader = bufferedReader3;
                            if (bufferedReader != null) {
                            }
                            throw th;
                        }
                    } else {
                        sb.append(",");
                        if (new File("/sys/block/mmcblk0/device/cid").exists()) {
                        }
                        string = sb.toString();
                        if (bufferedReader2 != null) {
                        }
                    }
                } catch (Throwable th8) {
                    th = th8;
                }
            } catch (Throwable th9) {
                bufferedReader2 = bufferedReader;
            }
        } catch (Throwable th10) {
            bufferedReader = null;
            th = th10;
        }
        return string;
    }

    public static String j(Context context) throws Throwable {
        StringBuilder sb = new StringBuilder();
        String strA = z.a(context, "ro.genymotion.version");
        if (strA != null) {
            sb.append("ro.genymotion.version");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(strA);
            sb.append("\n");
        }
        String strA2 = z.a(context, "androVM.vbox_dpi");
        if (strA2 != null) {
            sb.append("androVM.vbox_dpi");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(strA2);
            sb.append("\n");
        }
        String strA3 = z.a(context, "qemu.sf.fake_camera");
        if (strA3 != null) {
            sb.append("qemu.sf.fake_camera");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(strA3);
        }
        return sb.toString();
    }

    /* JADX WARN: Removed duplicated region for block: B:48:0x00a6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String k(Context context) throws Throwable {
        BufferedReader bufferedReader;
        String line;
        StringBuilder sb = new StringBuilder();
        if (f142a == null) {
            f142a = z.a(context, "ro.secure");
        }
        if (f142a != null) {
            sb.append("ro.secure");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(f142a);
            sb.append("\n");
        }
        if (b == null) {
            b = z.a(context, "ro.debuggable");
        }
        if (b != null) {
            sb.append("ro.debuggable");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(b);
            sb.append("\n");
        }
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/self/status"));
            do {
                try {
                    try {
                        line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                    } catch (Throwable th) {
                        th = th;
                        x.a(th);
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                x.a(e);
                            }
                        }
                        return sb.toString();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e2) {
                            x.a(e2);
                        }
                    }
                    throw th;
                }
            } while (!line.startsWith("TracerPid:"));
            if (line != null) {
                String strTrim = line.substring(10).trim();
                sb.append("tracer_pid");
                sb.append(PinyinConverter.PINYIN_EXCLUDE);
                sb.append(strTrim);
            }
            String string = sb.toString();
            try {
                bufferedReader.close();
                return string;
            } catch (IOException e3) {
                x.a(e3);
                return string;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:13:0x0048 A[Catch: all -> 0x00bb, Throwable -> 0x00d8, TRY_LEAVE, TryCatch #7 {all -> 0x00bb, blocks: (B:3:0x0006, B:5:0x0013, B:11:0x0036, B:13:0x0048, B:19:0x006b, B:21:0x007d), top: B:66:0x0006 }] */
    /* JADX WARN: Removed duplicated region for block: B:21:0x007d A[Catch: all -> 0x00bb, Throwable -> 0x00db, TRY_LEAVE, TryCatch #1 {Throwable -> 0x00db, blocks: (B:19:0x006b, B:21:0x007d), top: B:60:0x006b }] */
    /* JADX WARN: Removed duplicated region for block: B:57:0x00de  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x00a1 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:60:0x006b A[EXC_TOP_SPLITTER, PHI: r1
      0x006b: PHI (r1v5 java.io.BufferedReader) = (r1v4 java.io.BufferedReader), (r1v12 java.io.BufferedReader) binds: [B:12:0x0046, B:18:0x006a] A[DONT_GENERATE, DONT_INLINE], SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:64:0x00b2 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:70:0x00be A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String n() throws Throwable {
        BufferedReader bufferedReader;
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader2 = null;
        try {
            try {
                if (new File("/sys/class/power_supply/ac/online").exists()) {
                    bufferedReader = new BufferedReader(new FileReader("/sys/class/power_supply/ac/online"));
                    try {
                        try {
                            String line = bufferedReader.readLine();
                            if (line != null) {
                                sb.append("ac_online");
                                sb.append(PinyinConverter.PINYIN_EXCLUDE);
                                sb.append(line);
                            }
                            bufferedReader.close();
                            bufferedReader2 = bufferedReader;
                            try {
                                sb.append("\n");
                                if (new File("/sys/class/power_supply/usb/online").exists()) {
                                    sb.append("\n");
                                    if (new File("/sys/class/power_supply/battery/capacity").exists()) {
                                    }
                                    if (bufferedReader != null) {
                                    }
                                } else {
                                    BufferedReader bufferedReader3 = new BufferedReader(new FileReader("/sys/class/power_supply/usb/online"));
                                    try {
                                        String line2 = bufferedReader3.readLine();
                                        if (line2 != null) {
                                            sb.append("usb_online");
                                            sb.append(PinyinConverter.PINYIN_EXCLUDE);
                                            sb.append(line2);
                                        }
                                        bufferedReader3.close();
                                        bufferedReader2 = bufferedReader3;
                                        try {
                                            sb.append("\n");
                                            if (new File("/sys/class/power_supply/battery/capacity").exists()) {
                                                bufferedReader = bufferedReader2;
                                            } else {
                                                bufferedReader = new BufferedReader(new FileReader("/sys/class/power_supply/battery/capacity"));
                                                try {
                                                    String line3 = bufferedReader.readLine();
                                                    if (line3 != null) {
                                                        sb.append("battery_capacity");
                                                        sb.append(PinyinConverter.PINYIN_EXCLUDE);
                                                        sb.append(line3);
                                                    }
                                                    bufferedReader.close();
                                                } catch (Throwable th) {
                                                    bufferedReader2 = bufferedReader;
                                                    th = th;
                                                    if (bufferedReader2 != null) {
                                                        try {
                                                            bufferedReader2.close();
                                                        } catch (IOException e) {
                                                            x.a(e);
                                                        }
                                                    }
                                                    throw th;
                                                }
                                            }
                                            if (bufferedReader != null) {
                                                try {
                                                    bufferedReader.close();
                                                } catch (IOException e2) {
                                                    x.a(e2);
                                                }
                                            }
                                        } catch (Throwable th2) {
                                            bufferedReader = bufferedReader2;
                                            if (bufferedReader != null) {
                                                try {
                                                    bufferedReader.close();
                                                } catch (IOException e3) {
                                                    x.a(e3);
                                                }
                                            }
                                        }
                                    } catch (Throwable th3) {
                                        bufferedReader2 = bufferedReader3;
                                        th = th3;
                                        if (bufferedReader2 != null) {
                                        }
                                        throw th;
                                    }
                                }
                            } catch (Throwable th4) {
                                bufferedReader = bufferedReader2;
                            }
                        } catch (Throwable th5) {
                            bufferedReader2 = bufferedReader;
                            th = th5;
                            if (bufferedReader2 != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th6) {
                        if (bufferedReader != null) {
                        }
                        return sb.toString();
                    }
                } else {
                    sb.append("\n");
                    if (new File("/sys/class/power_supply/usb/online").exists()) {
                    }
                }
            } catch (Throwable th7) {
                bufferedReader = null;
            }
            return sb.toString();
        } catch (Throwable th8) {
            th = th8;
        }
    }

    public static String l(Context context) throws Throwable {
        StringBuilder sb = new StringBuilder();
        String strA = z.a(context, "gsm.sim.state");
        if (strA != null) {
            sb.append("gsm.sim.state");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(strA);
        }
        sb.append("\n");
        String strA2 = z.a(context, "gsm.sim.state2");
        if (strA2 != null) {
            sb.append("gsm.sim.state2");
            sb.append(PinyinConverter.PINYIN_EXCLUDE);
            sb.append(strA2);
        }
        return sb.toString();
    }

    /* JADX WARN: Removed duplicated region for block: B:39:0x0049 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static long o() throws Throwable {
        BufferedReader bufferedReader;
        float fCurrentTimeMillis = 0.0f;
        BufferedReader bufferedReader2 = null;
        try {
            try {
                bufferedReader = new BufferedReader(new FileReader("/proc/uptime"));
                try {
                    String line = bufferedReader.readLine();
                    if (line != null) {
                        fCurrentTimeMillis = (System.currentTimeMillis() / 1000) - Float.parseFloat(line.split(PinyinConverter.PINYIN_SEPARATOR)[0]);
                    }
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        x.a(e);
                    }
                } catch (Throwable th) {
                    th = th;
                    x.a(th);
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e2) {
                            x.a(e2);
                        }
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                if (0 != 0) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e3) {
                        x.a(e3);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
        }
        return (long) fCurrentTimeMillis;
    }
}
