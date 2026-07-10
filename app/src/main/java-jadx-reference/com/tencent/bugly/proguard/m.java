package com.tencent.bugly.proguard;

import java.io.Serializable;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class m implements Serializable, Comparable<m> {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public long f193a;
    public String b;
    public long c;
    public int d;
    public String e;
    public String f;
    public long g;

    @Override // java.lang.Comparable
    public final /* bridge */ /* synthetic */ int compareTo(m mVar) {
        return (int) (this.c - mVar.c);
    }
}
