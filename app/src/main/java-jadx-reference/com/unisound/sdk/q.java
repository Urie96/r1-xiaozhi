package com.unisound.sdk;

/* JADX INFO: loaded from: classes.dex */
class q implements com.unisound.common.d {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ o f351a;

    q(o oVar) {
        this.f351a = oVar;
    }

    @Override // com.unisound.common.d
    public void a() {
        this.f351a.start();
    }

    @Override // com.unisound.common.d
    public void b() {
        this.f351a.stop();
    }

    @Override // com.unisound.common.d
    public void c() {
        this.f351a.cancel();
    }
}
