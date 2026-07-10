package com.tencent.bugly.crashreport.crash.jni;

import android.annotation.SuppressLint;
import android.content.Context;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.common.strategy.StrategyBean;
import com.tencent.bugly.crashreport.crash.CrashDetailBean;
import com.tencent.bugly.crashreport.crash.c;
import com.tencent.bugly.proguard.w;
import com.tencent.bugly.proguard.x;
import com.tencent.bugly.proguard.z;
import java.io.File;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public class NativeCrashHandler implements com.tencent.bugly.crashreport.a {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private static NativeCrashHandler f169a;
    private static boolean l = false;
    private static boolean m = false;
    private final Context b;
    private final com.tencent.bugly.crashreport.common.info.a c;
    private final w d;
    private NativeExceptionHandler e;
    private String f;
    private final boolean g;
    private boolean h = false;
    private boolean i = false;
    private boolean j = false;
    private boolean k = false;
    private com.tencent.bugly.crashreport.crash.b n;

    protected native boolean appendNativeLog(String str, String str2, String str3);

    protected native boolean appendWholeNativeLog(String str);

    protected native String getNativeKeyValueList();

    protected native String getNativeLog();

    protected native boolean putNativeKeyValue(String str, String str2);

    protected native String regist(String str, boolean z, int i);

    protected native String removeNativeKeyValue(String str);

    protected native void setNativeInfo(int i, String str);

    protected native void testCrash();

    protected native String unregist();

    @SuppressLint({"SdCardPath"})
    private NativeCrashHandler(Context context, com.tencent.bugly.crashreport.common.info.a aVar, com.tencent.bugly.crashreport.crash.b bVar, w wVar, boolean z, String str) {
        this.b = z.a(context);
        try {
            if (z.a(str)) {
                str = context.getDir("bugly", 0).getAbsolutePath();
            }
        } catch (Throwable th) {
            str = "/data/data/" + com.tencent.bugly.crashreport.common.info.a.a(context).c + "/app_bugly";
        }
        this.n = bVar;
        this.f = str;
        this.c = aVar;
        this.d = wVar;
        this.g = z;
        this.e = new a(context, aVar, bVar, com.tencent.bugly.crashreport.common.strategy.a.a());
    }

    public static synchronized NativeCrashHandler getInstance(Context context, com.tencent.bugly.crashreport.common.info.a aVar, com.tencent.bugly.crashreport.crash.b bVar, com.tencent.bugly.crashreport.common.strategy.a aVar2, w wVar, boolean z, String str) {
        if (f169a == null) {
            f169a = new NativeCrashHandler(context, aVar, bVar, wVar, z, str);
        }
        return f169a;
    }

    public static synchronized NativeCrashHandler getInstance() {
        return f169a;
    }

    public synchronized String getDumpFilePath() {
        return this.f;
    }

    public synchronized void setDumpFilePath(String str) {
        this.f = str;
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x00aa A[Catch: all -> 0x00b2, TRY_LEAVE, TryCatch #0 {, blocks: (B:4:0x0003, B:6:0x0007, B:9:0x0011, B:11:0x0015, B:13:0x001e, B:15:0x0053, B:16:0x0066, B:18:0x0070, B:19:0x0073, B:21:0x007d, B:22:0x0080, B:24:0x0084, B:25:0x008c, B:27:0x0090, B:28:0x0098, B:41:0x00d8, B:40:0x00cf, B:37:0x00b5, B:39:0x00bb, B:32:0x00aa, B:43:0x00e1, B:45:0x00e5, B:48:0x0115, B:50:0x012b, B:52:0x0168, B:54:0x018c, B:55:0x0192, B:58:0x01b0, B:31:0x00a2), top: B:66:0x0003, inners: #2 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private synchronized void a(boolean z) {
        String strRegist;
        if (this.j) {
            x.d("[Native] Native crash report has already registered.", new Object[0]);
        } else if (this.i) {
            try {
                strRegist = regist(this.f, z, 1);
            } catch (Throwable th) {
                x.c("[Native] Failed to load Bugly SO file.", new Object[0]);
            }
            if (strRegist != null) {
                x.a("[Native] Native Crash Report enable.", new Object[0]);
                x.c("[Native] Check extra jni for Bugly NDK v%s", strRegist);
                String strReplace = "2.1.1".replace(".", "");
                String strReplace2 = "2.3.0".replace(".", "");
                String strReplace3 = strRegist.replace(".", "");
                if (strReplace3.length() == 2) {
                    strReplace3 = strReplace3 + "0";
                } else if (strReplace3.length() == 1) {
                    strReplace3 = strReplace3 + "00";
                }
                try {
                    if (Integer.parseInt(strReplace3) >= Integer.parseInt(strReplace)) {
                        l = true;
                    }
                    if (Integer.parseInt(strReplace3) >= Integer.parseInt(strReplace2)) {
                        m = true;
                    }
                } catch (Throwable th2) {
                }
                if (m) {
                    x.a("[Native] Info setting jni can be accessed.", new Object[0]);
                } else {
                    x.d("[Native] Info setting jni can not be accessed.", new Object[0]);
                }
                if (l) {
                    x.a("[Native] Extra jni can be accessed.", new Object[0]);
                } else {
                    x.d("[Native] Extra jni can not be accessed.", new Object[0]);
                }
                this.c.n = strRegist;
                this.j = true;
            } else {
                this.i = false;
                this.h = false;
            }
        } else if (this.h) {
            try {
                Class[] clsArr = {String.class, String.class, Integer.TYPE, Integer.TYPE};
                Object[] objArr = new Object[4];
                objArr[0] = this.f;
                objArr[1] = com.tencent.bugly.crashreport.common.info.b.a(false);
                objArr[2] = Integer.valueOf(z ? 1 : 5);
                objArr[3] = 1;
                String str = (String) z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "registNativeExceptionHandler2", null, clsArr, objArr);
                if (str == null) {
                    Class[] clsArr2 = {String.class, String.class, Integer.TYPE};
                    com.tencent.bugly.crashreport.common.info.a.b();
                    str = (String) z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "registNativeExceptionHandler", null, clsArr2, new Object[]{this.f, com.tencent.bugly.crashreport.common.info.b.a(false), Integer.valueOf(com.tencent.bugly.crashreport.common.info.a.L())});
                }
                if (str != null) {
                    this.j = true;
                    com.tencent.bugly.crashreport.common.info.a.b().n = str;
                    Boolean bool = (Boolean) z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "checkExtraJni", null, new Class[]{String.class}, new Object[]{str});
                    if (bool != null) {
                        l = bool.booleanValue();
                    }
                    z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "enableHandler", null, new Class[]{Boolean.TYPE}, new Object[]{true});
                    z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "setLogMode", null, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(z ? 1 : 5)});
                }
            } catch (Throwable th3) {
            }
        }
    }

    public synchronized void startNativeMonitor() {
        if (this.i || this.h) {
            a(this.g);
        } else {
            String str = "Bugly";
            boolean z = !z.a(this.c.m);
            String str2 = this.c.m;
            if (z) {
                str = str2;
            } else {
                this.c.getClass();
            }
            this.i = a(str, z);
            if (this.i || this.h) {
                a(this.g);
                this.d.a(new Runnable() { // from class: com.tencent.bugly.crashreport.crash.jni.NativeCrashHandler.1
                    @Override // java.lang.Runnable
                    public final void run() throws Throwable {
                        if (z.a(NativeCrashHandler.this.b, "native_record_lock", 10000L)) {
                            try {
                                NativeCrashHandler.this.setNativeAppVersion(NativeCrashHandler.this.c.j);
                                NativeCrashHandler.this.setNativeAppChannel(NativeCrashHandler.this.c.l);
                                NativeCrashHandler.this.setNativeAppPackage(NativeCrashHandler.this.c.c);
                                NativeCrashHandler.this.setNativeUserId(NativeCrashHandler.this.c.g());
                                NativeCrashHandler.this.setNativeIsAppForeground(NativeCrashHandler.this.c.a());
                                NativeCrashHandler.this.setNativeLaunchTime(NativeCrashHandler.this.c.f141a);
                            } catch (Throwable th) {
                                if (!x.a(th)) {
                                    th.printStackTrace();
                                }
                            }
                            CrashDetailBean crashDetailBeanA = b.a(NativeCrashHandler.this.b, NativeCrashHandler.this.f, NativeCrashHandler.this.e);
                            if (crashDetailBeanA != null) {
                                x.a("[Native] Get crash from native record.", new Object[0]);
                                if (!NativeCrashHandler.this.n.a(crashDetailBeanA)) {
                                    NativeCrashHandler.this.n.a(crashDetailBeanA, 3000L, false);
                                }
                                b.a(false, NativeCrashHandler.this.f);
                            }
                            NativeCrashHandler.this.a();
                            z.b(NativeCrashHandler.this.b, "native_record_lock");
                            return;
                        }
                        x.a("[Native] Failed to lock file for handling native crash record.", new Object[0]);
                    }
                });
            }
        }
    }

    private static boolean a(String str, boolean z) {
        Throwable th;
        boolean z2;
        try {
            x.a("[Native] Trying to load so: %s", str);
            if (z) {
                System.load(str);
            } else {
                System.loadLibrary(str);
            }
        } catch (Throwable th2) {
            th = th2;
            z2 = false;
        }
        try {
            x.a("[Native] Successfully loaded SO: %s", str);
            return true;
        } catch (Throwable th3) {
            th = th3;
            z2 = true;
            x.d(th.getMessage(), new Object[0]);
            x.d("[Native] Failed to load so: %s", str);
            return z2;
        }
    }

    private synchronized void b() {
        if (!this.j) {
            x.d("[Native] Native crash report has already unregistered.", new Object[0]);
        } else {
            try {
                if (unregist() != null) {
                    x.a("[Native] Successfully closed native crash report.", new Object[0]);
                    this.j = false;
                }
            } catch (Throwable th) {
                x.c("[Native] Failed to close native crash report.", new Object[0]);
            }
            try {
                z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "enableHandler", null, new Class[]{Boolean.TYPE}, new Object[]{false});
                this.j = false;
                x.a("[Native] Successfully closed native crash report.", new Object[0]);
            } catch (Throwable th2) {
                x.c("[Native] Failed to close native crash report.", new Object[0]);
                this.i = false;
                this.h = false;
            }
        }
    }

    public void testNativeCrash() {
        if (!this.i) {
            x.d("[Native] Bugly SO file has not been load.", new Object[0]);
        } else {
            testCrash();
        }
    }

    public NativeExceptionHandler getNativeExceptionHandler() {
        return this.e;
    }

    protected final void a() {
        File[] fileArrListFiles;
        int iIndexOf;
        long jB = z.b() - c.g;
        File file = new File(this.f);
        if (file.exists() && file.isDirectory() && (fileArrListFiles = file.listFiles()) != null && fileArrListFiles.length != 0) {
            int length = "tomb_".length();
            int i = 0;
            for (File file2 : fileArrListFiles) {
                String name = file2.getName();
                if (name.startsWith("tomb_")) {
                    try {
                        iIndexOf = name.indexOf(".txt");
                    } catch (Throwable th) {
                        x.e("[Native] Tomb file format error, delete %s", name);
                    }
                    if (iIndexOf <= 0 || Long.parseLong(name.substring(length, iIndexOf)) < jB) {
                        if (file2.delete()) {
                            i++;
                        }
                    }
                }
            }
            x.c("[Native] Clean tombs %d", Integer.valueOf(i));
        }
    }

    private synchronized void b(boolean z) {
        if (z) {
            startNativeMonitor();
        } else {
            b();
        }
    }

    public synchronized boolean isUserOpened() {
        return this.k;
    }

    private synchronized void c(boolean z) {
        if (this.k != z) {
            x.a("user change native %b", Boolean.valueOf(z));
            this.k = z;
        }
    }

    public synchronized void setUserOpened(boolean z) {
        boolean z2 = true;
        synchronized (this) {
            c(z);
            boolean zIsUserOpened = isUserOpened();
            com.tencent.bugly.crashreport.common.strategy.a aVarA = com.tencent.bugly.crashreport.common.strategy.a.a();
            if (aVarA == null) {
                z2 = zIsUserOpened;
            } else if (!zIsUserOpened || !aVarA.c().g) {
                z2 = false;
            }
            if (z2 != this.j) {
                x.a("native changed to %b", Boolean.valueOf(z2));
                b(z2);
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x0030 A[Catch: all -> 0x0046, TRY_LEAVE, TryCatch #0 {, blocks: (B:5:0x0005, B:7:0x000b, B:8:0x001c, B:10:0x0028, B:12:0x002c, B:14:0x0030), top: B:21:0x0005 }] */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0044  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public synchronized void onStrategyChanged(StrategyBean strategyBean) {
        boolean z;
        synchronized (this) {
            if (strategyBean != null) {
                if (strategyBean.g != this.j) {
                    x.d("server native changed to %b", Boolean.valueOf(strategyBean.g));
                }
                z = !com.tencent.bugly.crashreport.common.strategy.a.a().c().g && this.k;
                if (z != this.j) {
                    x.a("native changed to %b", Boolean.valueOf(z));
                    b(z);
                }
            } else if (com.tencent.bugly.crashreport.common.strategy.a.a().c().g) {
                if (z != this.j) {
                }
            }
        }
    }

    @Override // com.tencent.bugly.crashreport.a
    public boolean appendLogToNative(String str, String str2, String str3) {
        boolean zBooleanValue;
        if ((this.h || this.i) && l) {
            if (str == null || str2 == null || str3 == null) {
                return false;
            }
            try {
                if (this.i) {
                    zBooleanValue = appendNativeLog(str, str2, str3);
                } else {
                    Boolean bool = (Boolean) z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "appendNativeLog", null, new Class[]{String.class, String.class, String.class}, new Object[]{str, str2, str3});
                    zBooleanValue = bool != null ? bool.booleanValue() : false;
                }
                return zBooleanValue;
            } catch (UnsatisfiedLinkError e) {
                l = false;
                return false;
            } catch (Throwable th) {
                if (!x.a(th)) {
                    th.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }

    public boolean putKeyValueToNative(String str, String str2) {
        boolean zBooleanValue;
        if ((this.h || this.i) && l) {
            if (str == null || str2 == null) {
                return false;
            }
            try {
                if (this.i) {
                    zBooleanValue = putNativeKeyValue(str, str2);
                } else {
                    Boolean bool = (Boolean) z.a("com.tencent.feedback.eup.jni.NativeExceptionUpload", "putNativeKeyValue", null, new Class[]{String.class, String.class}, new Object[]{str, str2});
                    zBooleanValue = bool != null ? bool.booleanValue() : false;
                }
                return zBooleanValue;
            } catch (UnsatisfiedLinkError e) {
                l = false;
                return false;
            } catch (Throwable th) {
                if (!x.a(th)) {
                    th.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }

    private boolean a(int i, String str) {
        if (!this.i || !m) {
            return false;
        }
        try {
            setNativeInfo(i, str);
            return true;
        } catch (UnsatisfiedLinkError e) {
            m = false;
            return false;
        } catch (Throwable th) {
            if (x.a(th)) {
                return false;
            }
            th.printStackTrace();
            return false;
        }
    }

    public boolean filterSigabrtSysLog() {
        return a(998, "true");
    }

    public boolean setNativeAppVersion(String str) {
        return a(10, str);
    }

    public boolean setNativeAppChannel(String str) {
        return a(12, str);
    }

    public boolean setNativeAppPackage(String str) {
        return a(13, str);
    }

    public boolean setNativeUserId(String str) {
        return a(11, str);
    }

    @Override // com.tencent.bugly.crashreport.a
    public boolean setNativeIsAppForeground(boolean z) {
        return a(14, z ? "true" : Bugly.SDK_IS_DEV);
    }

    public boolean setNativeLaunchTime(long j) {
        try {
            return a(15, String.valueOf(j));
        } catch (NumberFormatException e) {
            if (!x.a(e)) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
