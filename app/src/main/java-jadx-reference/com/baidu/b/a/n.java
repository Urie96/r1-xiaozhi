package com.baidu.b.a;

import com.baidu.b.a.h;

/* JADX INFO: loaded from: classes.dex */
class n implements h.a<String> {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ String f49a;
    final /* synthetic */ b b;

    n(b bVar, String str) {
        this.b = bVar;
        this.f49a = str;
    }

    @Override // com.baidu.b.a.h.a
    public void a(String str) {
        this.b.a(str, this.f49a);
    }
}
