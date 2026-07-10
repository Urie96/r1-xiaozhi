package com.baidu.location.a;

import android.location.Location;

/* JADX INFO: loaded from: classes.dex */
class e implements Runnable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ Location f64a;
    final /* synthetic */ d b;

    e(d dVar, Location location) {
        this.b = dVar;
        this.f64a = location;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.b.b(this.f64a);
    }
}
