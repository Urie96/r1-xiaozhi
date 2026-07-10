package cn.kuwo.autosdk;

import android.os.Process;

/* JADX INFO: loaded from: classes.dex */
final class t extends Thread {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private volatile Runnable f21a;
    private volatile int b;
    private volatile boolean c;

    private t() {
    }

    /* synthetic */ t(t tVar) {
        this();
    }

    public void a(Runnable runnable, int i) {
        this.f21a = runnable;
        this.b = i;
        if (this.c) {
            synchronized (this) {
                notify();
            }
        } else {
            start();
            this.c = true;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x003b, code lost:
    
        wait();
     */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        while (true) {
            Process.setThreadPriority(this.b);
            this.f21a.run();
            if (r.f19a < 5) {
                synchronized (this) {
                    synchronized (r.b) {
                        if (r.f19a >= 5) {
                            break;
                        }
                        r.b[r.f19a] = this;
                        r.f19a++;
                    }
                }
                break;
            }
            break;
        }
        this.c = false;
    }
}
