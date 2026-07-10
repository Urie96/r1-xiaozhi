package com.unisound.sdk;

/* JADX INFO: loaded from: classes.dex */
class bn implements Runnable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ bg f319a;

    bn(bg bgVar) {
        this.f319a = bgVar;
    }

    @Override // java.lang.Runnable
    public void run() {
        com.unisound.common.y.c(com.unisound.common.y.v, "Runnable refresh activate");
        this.f319a.refreshActivate();
    }
}
