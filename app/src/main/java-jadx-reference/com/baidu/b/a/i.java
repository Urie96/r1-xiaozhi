package com.baidu.b.a;

import java.util.HashMap;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
class i implements Runnable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ h f44a;

    i(h hVar) {
        this.f44a = hVar;
    }

    @Override // java.lang.Runnable
    public void run() throws Throwable {
        this.f44a.a((List<HashMap<String, String>>) this.f44a.b);
    }
}
