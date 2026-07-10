package com.unisound.jni;

/* JADX INFO: loaded from: classes.dex */
public class NR {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    int f278a = init();

    static {
        System.loadLibrary("nr");
    }

    private native int process(byte[] bArr, byte[] bArr2);

    private native byte[] process2(byte[] bArr);

    public native int init();

    public byte[] process(byte[] bArr) {
        return process2(bArr);
    }

    public native void release();

    public native void reset();
}
