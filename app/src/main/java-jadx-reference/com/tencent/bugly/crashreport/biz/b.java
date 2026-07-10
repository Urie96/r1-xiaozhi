package com.tencent.bugly.crashreport.biz;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.crashreport.biz.a.AnonymousClass2;
import com.tencent.bugly.crashreport.biz.a.RunnableC0009a;
import com.tencent.bugly.crashreport.biz.a.c;
import com.tencent.bugly.crashreport.common.strategy.StrategyBean;
import com.tencent.bugly.proguard.w;
import com.tencent.bugly.proguard.x;
import com.tencent.bugly.proguard.z;
import java.util.List;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public class b {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static a f137a;
    private static boolean b;
    private static int g;
    private static long h;
    private static long i;
    private static int c = 10;
    private static long d = 300000;
    private static long e = 30000;
    private static long f = 0;
    private static long j = 0;
    private static Application.ActivityLifecycleCallbacks k = null;
    private static Class<?> l = null;
    private static boolean m = true;

    static /* synthetic */ String a(String str, String str2) {
        return z.a() + "  " + str + "  " + str2 + "\n";
    }

    static /* synthetic */ int g() {
        int i2 = g;
        g = i2 + 1;
        return i2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0067 A[ORIG_RETURN, RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:29:0x006e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static void c(Context context, BuglyStrategy buglyStrategy) {
        boolean zIsEnableUserInfo;
        boolean z;
        boolean z2;
        if (buglyStrategy == null) {
            zIsEnableUserInfo = true;
            z = false;
        } else {
            boolean zRecordUserInfoOnceADay = buglyStrategy.recordUserInfoOnceADay();
            zIsEnableUserInfo = buglyStrategy.isEnableUserInfo();
            z = zRecordUserInfoOnceADay;
        }
        if (z) {
            com.tencent.bugly.crashreport.common.info.a aVarA = com.tencent.bugly.crashreport.common.info.a.a(context);
            List<UserInfoBean> listA = f137a.a(aVarA.d);
            if (listA != null) {
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 >= listA.size()) {
                        break;
                    }
                    UserInfoBean userInfoBean = listA.get(i3);
                    if (userInfoBean.n.equals(aVarA.j) && userInfoBean.b == 1) {
                        long jB = z.b();
                        if (jB <= 0) {
                            break;
                        }
                        if (userInfoBean.e >= jB) {
                            if (userInfoBean.f <= 0) {
                                a aVar = f137a;
                                w wVarA = w.a();
                                if (wVarA != null) {
                                    wVarA.a(aVar.new AnonymousClass2());
                                }
                            }
                            z2 = false;
                        }
                    }
                    i2 = i3 + 1;
                }
                if (!z2) {
                    zIsEnableUserInfo = false;
                } else {
                    return;
                }
            } else {
                z2 = true;
                if (!z2) {
                }
            }
        }
        com.tencent.bugly.crashreport.common.info.a aVarB = com.tencent.bugly.crashreport.common.info.a.b();
        if (aVarB != null) {
            boolean z3 = false;
            String className = null;
            for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                if (stackTraceElement.getMethodName().equals("onCreate")) {
                    className = stackTraceElement.getClassName();
                }
                if (stackTraceElement.getClassName().equals("android.app.Activity")) {
                    z3 = true;
                }
            }
            if (className == null) {
                className = "unknown";
            } else if (z3) {
                aVarB.a(true);
            } else {
                className = "background";
            }
            aVarB.p = className;
        }
        if (zIsEnableUserInfo) {
            if (Build.VERSION.SDK_INT >= 14) {
                Application application = context.getApplicationContext() instanceof Application ? (Application) context.getApplicationContext() : null;
                if (application != null) {
                    try {
                        if (k == null) {
                            k = new Application.ActivityLifecycleCallbacks() { // from class: com.tencent.bugly.crashreport.biz.b.2
                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityStopped(Activity activity) {
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityStarted(Activity activity) {
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityResumed(Activity activity) {
                                    String name = "unknown";
                                    if (activity != null) {
                                        name = activity.getClass().getName();
                                    }
                                    if (b.l == null || b.l.getName().equals(name)) {
                                        x.c(">>> %s onResumed <<<", name);
                                        com.tencent.bugly.crashreport.common.info.a aVarB2 = com.tencent.bugly.crashreport.common.info.a.b();
                                        if (aVarB2 != null) {
                                            aVarB2.C.add(b.a(name, "onResumed"));
                                            aVarB2.a(true);
                                            aVarB2.p = name;
                                            aVarB2.q = System.currentTimeMillis();
                                            aVarB2.t = aVarB2.q - b.i;
                                            long j2 = aVarB2.q - b.h;
                                            if (j2 > (b.f > 0 ? b.f : b.e)) {
                                                aVarB2.d();
                                                b.g();
                                                x.a("[session] launch app one times (app in background %d seconds and over %d seconds)", Long.valueOf(j2 / 1000), Long.valueOf(b.e / 1000));
                                                if (b.g % b.c == 0) {
                                                    b.f137a.a(4, b.m, 0L);
                                                    return;
                                                }
                                                b.f137a.a(4, false, 0L);
                                                long jCurrentTimeMillis = System.currentTimeMillis();
                                                if (jCurrentTimeMillis - b.j > b.d) {
                                                    long unused = b.j = jCurrentTimeMillis;
                                                    x.a("add a timer to upload hot start user info", new Object[0]);
                                                    if (b.m) {
                                                        w.a().a(b.f137a.new RunnableC0009a(null, true), b.d);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityPaused(Activity activity) {
                                    String name = "unknown";
                                    if (activity != null) {
                                        name = activity.getClass().getName();
                                    }
                                    if (b.l == null || b.l.getName().equals(name)) {
                                        x.c(">>> %s onPaused <<<", name);
                                        com.tencent.bugly.crashreport.common.info.a aVarB2 = com.tencent.bugly.crashreport.common.info.a.b();
                                        if (aVarB2 != null) {
                                            aVarB2.C.add(b.a(name, "onPaused"));
                                            aVarB2.a(false);
                                            aVarB2.r = System.currentTimeMillis();
                                            aVarB2.s = aVarB2.r - aVarB2.q;
                                            long unused = b.h = aVarB2.r;
                                            if (aVarB2.s < 0) {
                                                aVarB2.s = 0L;
                                            }
                                            if (activity != null) {
                                                aVarB2.p = "background";
                                            } else {
                                                aVarB2.p = "unknown";
                                            }
                                        }
                                    }
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityDestroyed(Activity activity) {
                                    String name = "unknown";
                                    if (activity != null) {
                                        name = activity.getClass().getName();
                                    }
                                    if (b.l == null || b.l.getName().equals(name)) {
                                        x.c(">>> %s onDestroyed <<<", name);
                                        com.tencent.bugly.crashreport.common.info.a aVarB2 = com.tencent.bugly.crashreport.common.info.a.b();
                                        if (aVarB2 != null) {
                                            aVarB2.C.add(b.a(name, "onDestroyed"));
                                        }
                                    }
                                }

                                @Override // android.app.Application.ActivityLifecycleCallbacks
                                public final void onActivityCreated(Activity activity, Bundle bundle) {
                                    String name = "unknown";
                                    if (activity != null) {
                                        name = activity.getClass().getName();
                                    }
                                    if (b.l == null || b.l.getName().equals(name)) {
                                        x.c(">>> %s onCreated <<<", name);
                                        com.tencent.bugly.crashreport.common.info.a aVarB2 = com.tencent.bugly.crashreport.common.info.a.b();
                                        if (aVarB2 != null) {
                                            aVarB2.C.add(b.a(name, "onCreated"));
                                        }
                                    }
                                }
                            };
                        }
                        application.registerActivityLifecycleCallbacks(k);
                    } catch (Exception e2) {
                        if (!x.a(e2)) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
        if (m) {
            i = System.currentTimeMillis();
            f137a.a(1, false, 0L);
            x.a("[session] launch app, new start", new Object[0]);
            f137a.a();
            w.a().a(f137a.new c(21600000L), 21600000L);
        }
    }

    public static void a(final Context context, final BuglyStrategy buglyStrategy) {
        long appReportDelay;
        if (!b) {
            m = com.tencent.bugly.crashreport.common.info.a.a(context).e;
            f137a = new a(context, m);
            b = true;
            if (buglyStrategy != null) {
                l = buglyStrategy.getUserInfoActivity();
                appReportDelay = buglyStrategy.getAppReportDelay();
            } else {
                appReportDelay = 0;
            }
            if (appReportDelay <= 0) {
                c(context, buglyStrategy);
            } else {
                w.a().a(new Runnable() { // from class: com.tencent.bugly.crashreport.biz.b.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        b.c(context, buglyStrategy);
                    }
                }, appReportDelay);
            }
        }
    }

    public static void a(long j2) {
        if (j2 < 0) {
            j2 = com.tencent.bugly.crashreport.common.strategy.a.a().c().q;
        }
        f = j2;
    }

    public static void a(StrategyBean strategyBean, boolean z) {
        if (f137a != null && !z) {
            a aVar = f137a;
            w wVarA = w.a();
            if (wVarA != null) {
                wVarA.a(aVar.new AnonymousClass2());
            }
        }
        if (strategyBean != null) {
            if (strategyBean.q > 0) {
                e = strategyBean.q;
            }
            if (strategyBean.w > 0) {
                c = strategyBean.w;
            }
            if (strategyBean.x > 0) {
                d = strategyBean.x;
            }
        }
    }

    public static void a() {
        if (f137a != null) {
            f137a.a(2, false, 0L);
        }
    }

    public static void a(Context context) {
        if (b && context != null) {
            if (Build.VERSION.SDK_INT >= 14) {
                Application application = context.getApplicationContext() instanceof Application ? (Application) context.getApplicationContext() : null;
                if (application != null) {
                    try {
                        if (k != null) {
                            application.unregisterActivityLifecycleCallbacks(k);
                        }
                    } catch (Exception e2) {
                        if (!x.a(e2)) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
            b = false;
        }
    }
}
