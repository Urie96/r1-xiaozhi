package com.tencent.bugly.proguard;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class ao extends k implements Cloneable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public String f183a = "";
    private String b = "";

    @Override // com.tencent.bugly.proguard.k
    public final void a(j jVar) {
        jVar.a(this.f183a, 0);
        jVar.a(this.b, 1);
    }

    @Override // com.tencent.bugly.proguard.k
    public final void a(i iVar) {
        this.f183a = iVar.b(0, true);
        this.b = iVar.b(1, true);
    }

    @Override // com.tencent.bugly.proguard.k
    public final void a(StringBuilder sb, int i) {
    }
}
