package com.unisound.common;

import android.content.Context;
import android.net.ConnectivityManager;
import java.util.concurrent.BlockingQueue;

/* JADX INFO: loaded from: classes.dex */
class bc extends Thread {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ Context f254a;
    final /* synthetic */ ba b;

    bc(ba baVar, Context context) {
        this.b = baVar;
        this.f254a = context;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        try {
            y.c("WakeupWordCacheAndUpload uploadCacheWakeupContent");
            if (!ba.b((ConnectivityManager) this.f254a.getSystemService("connectivity"))) {
                y.a("WakeupWordCacheAndUpload network is not available, do nothing!");
                return;
            }
            for (String str : ba.c(this.f254a)) {
                bd bdVarG = this.b.g(this.f254a, str);
                if (bdVarG == null) {
                    this.b.f(this.f254a, str);
                } else {
                    BlockingQueue blockingQueueC = ba.c(this.f254a, str);
                    if (blockingQueueC.isEmpty()) {
                        this.b.f(this.f254a, str);
                    } else if (this.b.c(this.f254a, bdVarG, blockingQueueC)) {
                        this.b.f(this.f254a, str);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
