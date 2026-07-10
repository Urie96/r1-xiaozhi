package com.tencent.bugly.crashreport.crash.anr;

import android.app.ActivityManager;
import android.content.Context;
import android.os.FileObserver;
import android.os.Process;
import com.tencent.bugly.crashreport.common.strategy.StrategyBean;
import com.tencent.bugly.crashreport.crash.CrashDetailBean;
import com.tencent.bugly.crashreport.crash.anr.TraceFileHelper;
import com.tencent.bugly.crashreport.crash.c;
import com.tencent.bugly.proguard.w;
import com.tencent.bugly.proguard.x;
import com.tencent.bugly.proguard.y;
import com.tencent.bugly.proguard.z;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class b {
    private final Context c;
    private final com.tencent.bugly.crashreport.common.info.a d;
    private final w e;
    private final com.tencent.bugly.crashreport.common.strategy.a f;
    private final String g;
    private final com.tencent.bugly.crashreport.crash.b h;
    private FileObserver i;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private AtomicInteger f154a = new AtomicInteger(0);
    private long b = -1;
    private boolean j = true;

    public b(Context context, com.tencent.bugly.crashreport.common.strategy.a aVar, com.tencent.bugly.crashreport.common.info.a aVar2, w wVar, com.tencent.bugly.crashreport.crash.b bVar) {
        this.c = z.a(context);
        this.g = context.getDir("bugly", 0).getAbsolutePath();
        this.d = aVar2;
        this.e = wVar;
        this.f = aVar;
        this.h = bVar;
    }

    private CrashDetailBean a(a aVar) {
        CrashDetailBean crashDetailBean = new CrashDetailBean();
        try {
            crashDetailBean.B = com.tencent.bugly.crashreport.common.info.b.h();
            crashDetailBean.C = com.tencent.bugly.crashreport.common.info.b.f();
            crashDetailBean.D = com.tencent.bugly.crashreport.common.info.b.j();
            crashDetailBean.E = this.d.p();
            crashDetailBean.F = this.d.o();
            crashDetailBean.G = this.d.q();
            crashDetailBean.w = z.a(this.c, c.e, (String) null);
            crashDetailBean.b = 3;
            crashDetailBean.e = this.d.h();
            crashDetailBean.f = this.d.j;
            crashDetailBean.g = this.d.w();
            crashDetailBean.m = this.d.g();
            crashDetailBean.n = "ANR_EXCEPTION";
            crashDetailBean.o = aVar.f;
            crashDetailBean.q = aVar.g;
            crashDetailBean.N = new HashMap();
            crashDetailBean.N.put("BUGLY_CR_01", aVar.e);
            int iIndexOf = -1;
            if (crashDetailBean.q != null) {
                iIndexOf = crashDetailBean.q.indexOf("\n");
            }
            crashDetailBean.p = iIndexOf > 0 ? crashDetailBean.q.substring(0, iIndexOf) : "GET_FAIL";
            crashDetailBean.r = aVar.c;
            if (crashDetailBean.q != null) {
                crashDetailBean.u = z.b(crashDetailBean.q.getBytes());
            }
            crashDetailBean.y = aVar.b;
            crashDetailBean.z = this.d.d;
            crashDetailBean.A = "main(1)";
            crashDetailBean.H = this.d.y();
            crashDetailBean.h = this.d.v();
            crashDetailBean.i = this.d.K();
            crashDetailBean.v = aVar.d;
            crashDetailBean.K = this.d.n;
            crashDetailBean.L = this.d.f141a;
            crashDetailBean.M = this.d.a();
            crashDetailBean.O = this.d.H();
            crashDetailBean.P = this.d.I();
            crashDetailBean.Q = this.d.B();
            crashDetailBean.R = this.d.G();
            this.h.c(crashDetailBean);
            crashDetailBean.x = y.a();
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
        }
        return crashDetailBean;
    }

    /* JADX WARN: Removed duplicated region for block: B:79:0x01d6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static boolean a(String str, String str2, String str3) throws Throwable {
        BufferedWriter bufferedWriter;
        TraceFileHelper.a targetDumpInfo = TraceFileHelper.readTargetDumpInfo(str3, str, true);
        if (targetDumpInfo == null || targetDumpInfo.d == null || targetDumpInfo.d.size() <= 0) {
            x.e("not found trace dump for %s", str3);
            return false;
        }
        File file = new File(str2);
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            if (!file.exists() || !file.canWrite()) {
                x.e("backup file create fail %s", str2);
                return false;
            }
            BufferedWriter bufferedWriter2 = null;
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(file, false));
            } catch (IOException e) {
                e = e;
            } catch (Throwable th) {
                th = th;
                bufferedWriter = null;
            }
            try {
                String[] strArr = targetDumpInfo.d.get("main");
                if (strArr != null && strArr.length >= 3) {
                    bufferedWriter.write("\"main\" tid=" + strArr[2] + " :\n" + strArr[0] + "\n" + strArr[1] + "\n\n");
                    bufferedWriter.flush();
                }
                for (Map.Entry<String, String[]> entry : targetDumpInfo.d.entrySet()) {
                    if (!entry.getKey().equals("main") && entry.getValue() != null && entry.getValue().length >= 3) {
                        bufferedWriter.write("\"" + entry.getKey() + "\" tid=" + entry.getValue()[2] + " :\n" + entry.getValue()[0] + "\n" + entry.getValue()[1] + "\n\n");
                        bufferedWriter.flush();
                    }
                }
                try {
                    bufferedWriter.close();
                } catch (IOException e2) {
                    if (!x.a(e2)) {
                        e2.printStackTrace();
                    }
                }
                return true;
            } catch (IOException e3) {
                e = e3;
                bufferedWriter2 = bufferedWriter;
                try {
                    if (!x.a(e)) {
                        e.printStackTrace();
                    }
                    x.e("dump trace fail %s", e.getClass().getName() + ":" + e.getMessage());
                    if (bufferedWriter2 != null) {
                        try {
                            bufferedWriter2.close();
                        } catch (IOException e4) {
                            if (!x.a(e4)) {
                                e4.printStackTrace();
                            }
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedWriter = bufferedWriter2;
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e5) {
                            if (!x.a(e5)) {
                                e5.printStackTrace();
                            }
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (bufferedWriter != null) {
                }
                throw th;
            }
        } catch (Exception e6) {
            if (!x.a(e6)) {
                e6.printStackTrace();
            }
            x.e("backup file create error! %s  %s", e6.getClass().getName() + ":" + e6.getMessage(), str2);
            return false;
        }
    }

    public final boolean a() {
        return this.f154a.get() != 0;
    }

    /* JADX WARN: Removed duplicated region for block: B:112:0x02ed A[LOOP:0: B:37:0x00c6->B:112:0x02ed, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:119:0x0113 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void a(String str) {
        long jCurrentTimeMillis;
        ActivityManager.ProcessErrorStateInfo next;
        synchronized (this) {
            if (this.f154a.get() != 0) {
                x.c("trace started return ", new Object[0]);
                return;
            }
            this.f154a.set(1);
            try {
                x.c("read trace first dump for create time!", new Object[0]);
                TraceFileHelper.a firstDumpInfo = TraceFileHelper.readFirstDumpInfo(str, false);
                long j = firstDumpInfo != null ? firstDumpInfo.c : -1L;
                if (j == -1) {
                    x.d("trace dump fail could not get time!", new Object[0]);
                    jCurrentTimeMillis = System.currentTimeMillis();
                } else {
                    jCurrentTimeMillis = j;
                }
                if (Math.abs(jCurrentTimeMillis - this.b) < 10000) {
                    x.d("should not process ANR too Fre in %d", 10000);
                    return;
                }
                this.b = jCurrentTimeMillis;
                this.f154a.set(1);
                try {
                    Map<String, String> mapA = z.a(c.f, false);
                    if (mapA == null || mapA.size() <= 0) {
                        x.d("can't get all thread skip this anr", new Object[0]);
                        return;
                    }
                    Context context = this.c;
                    long j2 = 10000 < 0 ? 0L : 10000L;
                    x.c("to find!", new Object[0]);
                    ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
                    long j3 = j2 / 500;
                    int i = 0;
                    loop0: while (true) {
                        int i2 = i;
                        x.c("waiting!", new Object[0]);
                        List<ActivityManager.ProcessErrorStateInfo> processesInErrorState = activityManager.getProcessesInErrorState();
                        if (processesInErrorState != null) {
                            Iterator<ActivityManager.ProcessErrorStateInfo> it = processesInErrorState.iterator();
                            while (it.hasNext()) {
                                next = it.next();
                                if (next.condition == 2) {
                                    x.c("found!", new Object[0]);
                                    break loop0;
                                }
                            }
                            z.b(500L);
                            i = i2 + 1;
                            if (i2 < j3) {
                                x.c("end!", new Object[0]);
                                next = null;
                                break;
                            }
                        } else {
                            z.b(500L);
                            i = i2 + 1;
                            if (i2 < j3) {
                            }
                        }
                    }
                    if (next == null) {
                        x.c("proc state is unvisiable!", new Object[0]);
                        return;
                    }
                    if (next.pid != Process.myPid()) {
                        x.c("not mind proc!", next.processName);
                        return;
                    }
                    x.a("found visiable anr , start to process!", new Object[0]);
                    Context context2 = this.c;
                    this.f.c();
                    if (!this.f.b()) {
                        x.e("waiting for remote sync", new Object[0]);
                        int i3 = 0;
                        while (!this.f.b()) {
                            z.b(500L);
                            i3 += cn.yunzhisheng.asr.a.U;
                            if (i3 >= 3000) {
                                break;
                            }
                        }
                    }
                    File file = new File(context2.getFilesDir(), "bugly/bugly_trace_" + jCurrentTimeMillis + ".txt");
                    a aVar = new a();
                    aVar.c = jCurrentTimeMillis;
                    aVar.d = file.getAbsolutePath();
                    aVar.f153a = next.processName;
                    aVar.f = next.shortMsg;
                    aVar.e = next.longMsg;
                    aVar.b = mapA;
                    if (mapA != null) {
                        for (String str2 : mapA.keySet()) {
                            if (str2.startsWith("main(")) {
                                aVar.g = mapA.get(str2);
                            }
                        }
                    }
                    Object[] objArr = new Object[6];
                    objArr[0] = Long.valueOf(aVar.c);
                    objArr[1] = aVar.d;
                    objArr[2] = aVar.f153a;
                    objArr[3] = aVar.f;
                    objArr[4] = aVar.e;
                    objArr[5] = Integer.valueOf(aVar.b == null ? 0 : aVar.b.size());
                    x.c("anr tm:%d\ntr:%s\nproc:%s\nsMsg:%s\n lMsg:%s\n threads:%d", objArr);
                    if (!this.f.b()) {
                        x.e("crash report sync remote fail, will not upload to Bugly , print local for helpful!", new Object[0]);
                        com.tencent.bugly.crashreport.crash.b.a("ANR", z.a(), aVar.f153a, null, aVar.e, null);
                    } else if (this.f.c().j) {
                        x.a("found visiable anr , start to upload!", new Object[0]);
                        CrashDetailBean crashDetailBeanA = a(aVar);
                        if (crashDetailBeanA == null) {
                            x.e("pack anr fail!", new Object[0]);
                        } else {
                            c.a().a(crashDetailBeanA);
                            if (crashDetailBeanA.f148a >= 0) {
                                x.a("backup anr record success!", new Object[0]);
                            } else {
                                x.d("backup anr record fail!", new Object[0]);
                            }
                            if (str != null && new File(str).exists()) {
                                this.f154a.set(3);
                                if (a(str, aVar.d, aVar.f153a)) {
                                    x.a("backup trace success", new Object[0]);
                                }
                            }
                            com.tencent.bugly.crashreport.crash.b.a("ANR", z.a(), aVar.f153a, null, aVar.e, crashDetailBeanA);
                            if (!this.h.a(crashDetailBeanA)) {
                                this.h.a(crashDetailBeanA, 3000L, true);
                            }
                            this.h.b(crashDetailBeanA);
                        }
                    } else {
                        x.d("ANR Report is closed!", new Object[0]);
                    }
                } catch (Throwable th) {
                    x.a(th);
                    x.e("get all thread stack fail!", new Object[0]);
                }
            } catch (Throwable th2) {
                if (!x.a(th2)) {
                    th2.printStackTrace();
                }
                x.e("handle anr error %s", th2.getClass().toString());
            } finally {
                this.f154a.set(0);
            }
        }
    }

    private synchronized void c() {
        if (e()) {
            x.d("start when started!", new Object[0]);
        } else {
            this.i = new FileObserver("/data/anr/", 8) { // from class: com.tencent.bugly.crashreport.crash.anr.b.1
                @Override // android.os.FileObserver
                public final void onEvent(int i, String str) {
                    if (str != null) {
                        String str2 = "/data/anr/" + str;
                        if (!str2.contains("trace")) {
                            x.d("not anr file %s", str2);
                        } else {
                            b.this.a(str2);
                        }
                    }
                }
            };
            try {
                this.i.startWatching();
                x.a("start anr monitor!", new Object[0]);
                this.e.a(new Runnable() { // from class: com.tencent.bugly.crashreport.crash.anr.b.2
                    @Override // java.lang.Runnable
                    public final void run() {
                        b.this.b();
                    }
                });
            } catch (Throwable th) {
                this.i = null;
                x.d("start anr monitor failed!", new Object[0]);
                if (!x.a(th)) {
                    th.printStackTrace();
                }
            }
        }
    }

    private synchronized void d() {
        if (!e()) {
            x.d("close when closed!", new Object[0]);
        } else {
            try {
                this.i.stopWatching();
                this.i = null;
                x.d("close anr monitor!", new Object[0]);
            } catch (Throwable th) {
                x.d("stop anr monitor failed!", new Object[0]);
                if (!x.a(th)) {
                    th.printStackTrace();
                }
            }
        }
    }

    private synchronized boolean e() {
        return this.i != null;
    }

    private synchronized void b(boolean z) {
        if (z) {
            c();
        } else {
            d();
        }
    }

    private synchronized boolean f() {
        return this.j;
    }

    private synchronized void c(boolean z) {
        if (this.j != z) {
            x.a("user change anr %b", Boolean.valueOf(z));
            this.j = z;
        }
    }

    public final void a(boolean z) {
        c(z);
        boolean zF = f();
        com.tencent.bugly.crashreport.common.strategy.a aVarA = com.tencent.bugly.crashreport.common.strategy.a.a();
        if (aVarA != null) {
            zF = zF && aVarA.c().g;
        }
        if (zF != e()) {
            x.a("anr changed to %b", Boolean.valueOf(zF));
            b(zF);
        }
    }

    protected final void b() {
        File[] fileArrListFiles;
        int iIndexOf;
        long jB = z.b() - c.g;
        File file = new File(this.g);
        if (file.exists() && file.isDirectory() && (fileArrListFiles = file.listFiles()) != null && fileArrListFiles.length != 0) {
            int length = "bugly_trace_".length();
            int i = 0;
            for (File file2 : fileArrListFiles) {
                String name = file2.getName();
                if (name.startsWith("bugly_trace_")) {
                    try {
                        iIndexOf = name.indexOf(".txt");
                    } catch (Throwable th) {
                        x.e("tomb format error delete %s", name);
                    }
                    if (iIndexOf <= 0 || Long.parseLong(name.substring(length, iIndexOf)) < jB) {
                        if (file2.delete()) {
                            i++;
                        }
                    }
                }
            }
            x.c("clean tombs %d", Integer.valueOf(i));
        }
    }

    public final synchronized void a(StrategyBean strategyBean) {
        synchronized (this) {
            if (strategyBean != null) {
                if (strategyBean.j != e()) {
                    x.d("server anr changed to %b", Boolean.valueOf(strategyBean.j));
                }
                boolean z = strategyBean.j && f();
                if (z != e()) {
                    x.a("anr changed to %b", Boolean.valueOf(z));
                    b(z);
                }
            }
        }
    }
}
