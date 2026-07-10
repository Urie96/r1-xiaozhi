package com.unisound.common;

/* JADX INFO: loaded from: classes.dex */
public class au {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    static final int f247a = 1;
    int b = 0;

    public int a() {
        return this.b;
    }

    public void a(boolean z) {
        if (z) {
            this.b |= 1;
        } else {
            this.b &= -2;
        }
    }

    public boolean b() {
        return (this.b & 1) != 0;
    }
}
