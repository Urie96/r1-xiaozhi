package com.tencent.bugly.proguard;

import android.content.Context;
import android.os.Process;
import cn.yunzhisheng.common.PinyinConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class y {
    private static SimpleDateFormat b;
    private static StringBuilder d;
    private static StringBuilder e;
    private static boolean f;
    private static a g;
    private static String h;
    private static String i;
    private static Context j;
    private static String k;
    private static boolean l;
    private static int m;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static boolean f209a = true;
    private static int c = 5120;
    private static final Object n = new Object();

    static /* synthetic */ boolean a(boolean z) {
        f = false;
        return false;
    }

    static {
        b = null;
        try {
            b = new SimpleDateFormat("MM-dd HH:mm:ss");
        } catch (Throwable th) {
        }
    }

    private static boolean b(String str, String str2, String str3) {
        try {
            com.tencent.bugly.crashreport.common.info.a aVarB = com.tencent.bugly.crashreport.common.info.a.b();
            if (aVarB != null && aVarB.D != null) {
                return aVarB.D.appendLogToNative(str, str2, str3);
            }
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
        }
        return false;
    }

    public static synchronized void a(Context context) {
        if (!l && context != null && f209a) {
            try {
                e = new StringBuilder(0);
                d = new StringBuilder(0);
                j = context;
                com.tencent.bugly.crashreport.common.info.a aVarA = com.tencent.bugly.crashreport.common.info.a.a(context);
                h = aVarA.d;
                aVarA.getClass();
                i = "";
                k = j.getFilesDir().getPath() + "/buglylog_" + h + "_" + i + ".txt";
                m = Process.myPid();
            } catch (Throwable th) {
            }
            l = true;
        }
    }

    public static void a(int i2) {
        synchronized (n) {
            c = i2;
            if (i2 < 0) {
                c = 0;
            } else if (i2 > 10240) {
                c = 10240;
            }
        }
    }

    public static void a(String str, String str2, Throwable th) {
        if (th != null) {
            String message = th.getMessage();
            if (message == null) {
                message = "";
            }
            a(str, str2, message + '\n' + z.b(th));
        }
    }

    public static synchronized void a(String str, String str2, String str3) {
        if (l && f209a) {
            b(str, str2, str3);
            long jMyTid = Process.myTid();
            d.setLength(0);
            if (str3.length() > 30720) {
                str3 = str3.substring(str3.length() - 30720, str3.length() - 1);
            }
            Date date = new Date();
            d.append(b != null ? b.format(date) : date.toString()).append(PinyinConverter.PINYIN_SEPARATOR).append(m).append(PinyinConverter.PINYIN_SEPARATOR).append(jMyTid).append(PinyinConverter.PINYIN_SEPARATOR).append(str).append(PinyinConverter.PINYIN_SEPARATOR).append(str2).append(": ").append(str3).append("\u0001\r\n");
            final String string = d.toString();
            synchronized (n) {
                e.append(string);
                if (e.length() > c) {
                    if (!f) {
                        f = true;
                        w.a().a(new Runnable() { // from class: com.tencent.bugly.proguard.y.1
                            @Override // java.lang.Runnable
                            public final void run() {
                                synchronized (y.n) {
                                    try {
                                        if (y.g == null) {
                                            a unused = y.g = new a(y.k);
                                        } else if (y.g.b == null || y.g.b.length() + ((long) y.e.length()) > y.g.e) {
                                            y.g.a();
                                        }
                                        if (y.g.f211a) {
                                            y.g.a(y.e.toString());
                                            y.e.setLength(0);
                                        } else {
                                            y.e.setLength(0);
                                            y.e.append(string);
                                        }
                                        y.a(false);
                                    } catch (Throwable th) {
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public static byte[] a() {
        byte[] bArrA = null;
        if (f209a) {
            synchronized (n) {
                try {
                    File file = (g == null || !g.f211a) ? null : g.b;
                    if (e.length() != 0 || file != null) {
                        bArrA = z.a(file, e.toString(), "BuglyLog.txt");
                    }
                } catch (Throwable th) {
                }
            }
        }
        return bArrA;
    }

    /* JADX INFO: compiled from: BUGLY */
    public static class a {

        /* JADX INFO: renamed from: a, reason: collision with root package name */
        private boolean f211a;
        private File b;
        private String c;
        private long d;
        private long e = 30720;

        public a(String str) {
            if (str != null && !str.equals("")) {
                this.c = str;
                this.f211a = a();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* JADX WARN: Code restructure failed: missing block: B:19:0x001d, code lost:
        
            r0 = true;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean a() {
            boolean z = false;
            try {
                this.b = new File(this.c);
            } catch (Throwable th) {
                this.f211a = z;
                z = true;
            }
            if ((this.b.exists() && !this.b.delete()) || !this.b.createNewFile()) {
                this.f211a = false;
            } else {
                z = true;
            }
            return z;
        }

        /* JADX WARN: Removed duplicated region for block: B:38:0x003c A[EXC_TOP_SPLITTER, SYNTHETIC] */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public final boolean a(String str) throws Throwable {
            FileOutputStream fileOutputStream;
            FileOutputStream fileOutputStream2;
            if (!this.f211a) {
                return false;
            }
            try {
                fileOutputStream = new FileOutputStream(this.b, true);
            } catch (Throwable th) {
                th = th;
                fileOutputStream = null;
            }
            try {
                byte[] bytes = str.getBytes("UTF-8");
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                fileOutputStream.close();
                this.d += (long) bytes.length;
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }
                return true;
            } catch (Throwable th2) {
                fileOutputStream2 = fileOutputStream;
                try {
                    this.f211a = false;
                    if (fileOutputStream2 == null) {
                        return false;
                    }
                    try {
                        fileOutputStream2.close();
                        return false;
                    } catch (IOException e2) {
                        return false;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutputStream2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th;
                }
            }
        }
    }
}
