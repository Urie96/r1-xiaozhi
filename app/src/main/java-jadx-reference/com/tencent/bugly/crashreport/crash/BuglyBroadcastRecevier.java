package com.tencent.bugly.crashreport.crash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.tencent.bugly.proguard.u;
import com.tencent.bugly.proguard.x;
import com.tencent.bugly.proguard.z;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public class BuglyBroadcastRecevier extends BroadcastReceiver {
    private static BuglyBroadcastRecevier d = null;
    private Context b;
    private String c;
    private boolean e = true;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private IntentFilter f146a = new IntentFilter();

    public static synchronized BuglyBroadcastRecevier getInstance() {
        if (d == null) {
            d = new BuglyBroadcastRecevier();
        }
        return d;
    }

    public synchronized void addFilter(String str) {
        if (!this.f146a.hasAction(str)) {
            this.f146a.addAction(str);
        }
        x.c("add action %s", str);
    }

    public synchronized void register(Context context) {
        this.b = context;
        z.a(new Runnable() { // from class: com.tencent.bugly.crashreport.crash.BuglyBroadcastRecevier.1
            @Override // java.lang.Runnable
            public final void run() {
                try {
                    x.a(BuglyBroadcastRecevier.d.getClass(), "Register broadcast receiver of Bugly.", new Object[0]);
                    synchronized (this) {
                        BuglyBroadcastRecevier.this.b.registerReceiver(BuglyBroadcastRecevier.d, BuglyBroadcastRecevier.this.f146a);
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        });
    }

    public synchronized void unregister(Context context) {
        try {
            x.a(getClass(), "Unregister broadcast receiver of Bugly.", new Object[0]);
            context.unregisterReceiver(this);
            this.b = context;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
        }
    }

    @Override // android.content.BroadcastReceiver
    public final void onReceive(Context context, Intent intent) {
        try {
            a(context, intent);
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:8:0x0015  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private synchronized boolean a(Context context, Intent intent) {
        boolean z = true;
        synchronized (this) {
            if (context == null || intent == null) {
                z = false;
            } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (this.e) {
                    this.e = false;
                } else {
                    String strF = com.tencent.bugly.crashreport.common.info.b.f(this.b);
                    x.c("is Connect BC " + strF, new Object[0]);
                    x.a("network %s changed to %s", this.c, strF);
                    if (strF == null) {
                        this.c = null;
                    } else {
                        String str = this.c;
                        this.c = strF;
                        long jCurrentTimeMillis = System.currentTimeMillis();
                        com.tencent.bugly.crashreport.common.strategy.a aVarA = com.tencent.bugly.crashreport.common.strategy.a.a();
                        u uVarA = u.a();
                        com.tencent.bugly.crashreport.common.info.a aVarA2 = com.tencent.bugly.crashreport.common.info.a.a(context);
                        if (aVarA == null || uVarA == null || aVarA2 == null) {
                            x.d("not inited BC not work", new Object[0]);
                        } else if (!strF.equals(str)) {
                            if (jCurrentTimeMillis - uVarA.a(c.f159a) > 30000) {
                                x.a("try to upload crash on network changed.", new Object[0]);
                                c cVarA = c.a();
                                if (cVarA != null) {
                                    cVarA.a(0L);
                                }
                            }
                            if (jCurrentTimeMillis - uVarA.a(1001) > 30000) {
                                x.a("try to upload userinfo on network changed.", new Object[0]);
                                com.tencent.bugly.crashreport.biz.b.f137a.b();
                            }
                        }
                    }
                }
            }
        }
        return z;
    }
}
