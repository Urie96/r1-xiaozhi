package com.unisound.common;

import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;
import com.unisound.jni.Uni4micHalJNI;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;

/* JADX INFO: loaded from: classes.dex */
public class s {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static final int f270a = 100000;
    public static final int b = 100001;
    public static final int c = 100002;
    public static final int d = 100003;
    public static final int e = 100004;
    public static final int f = 100005;
    public static final int g = 100006;
    public static final int h = 100007;
    public static final int i = 100008;
    public static final int j = 100009;
    public static final int k = 100010;
    private static final String r = "USCFourMic";
    private static final boolean s = false;
    private AudioManager t;
    private Uni4micHalJNI u;
    private boolean v = false;
    private boolean w = true;
    public boolean l = false;
    public boolean m = false;
    int n = 0;
    int o = 0;
    public String p = Environment.getExternalStorageDirectory().getPath() + "/YunZhiSheng/4mic/";
    public String q = "";
    private boolean x = false;
    private int y = 0;
    private boolean z = false;

    public s(AudioManager audioManager) {
        this.t = audioManager;
    }

    private void b(String str) {
        int iLastIndexOf;
        if (str != null && (iLastIndexOf = str.lastIndexOf(47)) >= 0) {
            new File(str.substring(0, iLastIndexOf)).mkdirs();
        }
    }

    private void c(String str) {
        if (this.l && this.m) {
            Log.d(r, str);
        }
    }

    private boolean u() {
        return 1 == d();
    }

    private boolean v() {
        return e().contains("UNI_4MIC_HAL_ANDROID");
    }

    public int a(int i2, int i3) {
        return ((i2 / 2) * 1000) / i3;
    }

    public void a(int i2) {
        if (a() && u()) {
            c("set4MicWakeup -> " + i2);
            this.t.adjustStreamVolume(100000, 0, i2);
        }
        c("print debug log set4MicWakeup -> " + i2);
    }

    public void a(String str) {
        this.q = str;
    }

    public void a(boolean z) {
        this.l = z;
    }

    public boolean a() {
        return this.l;
    }

    /* JADX WARN: Removed duplicated region for block: B:40:0x0042 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean a(byte[] bArr, String str) throws Throwable {
        RandomAccessFile randomAccessFile;
        if (a() && f()) {
            b(str);
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArr);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            return true;
                        }
                    } catch (Exception e3) {
                        e = e3;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e6) {
                e = e6;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    public byte[][] a(byte[] bArr) {
        byte[][] bArr2 = (byte[][]) Array.newInstance((Class<?>) Byte.TYPE, 2, bArr.length / 2);
        for (int i2 = 0; (i2 * 4) + 2 < bArr.length; i2++) {
            System.arraycopy(bArr, i2 * 4, bArr2[0], i2 * 2, 2);
            System.arraycopy(bArr, (i2 * 4) + 2, bArr2[1], i2 * 2, 2);
        }
        return bArr2;
    }

    public int b() {
        if (a() && u()) {
            return this.t.getVibrateSetting(c);
        }
        return -1;
    }

    public void b(int i2) {
        if (a() && v()) {
            c("_set4MicWakeup -> " + i2);
            this.u.set4MicWakeUpStatus(i2);
        }
        c("print debug log _set4MicWakeup -> " + i2);
    }

    public void b(boolean z) {
        if (a() && u()) {
            this.t.adjustStreamVolume(e, 0, z ? 1 : 0);
            c("setDebugMode set To board  -> " + z);
        }
        c("setDebugMode  -> " + z);
        this.m = z;
    }

    public void b(byte[] bArr) throws Throwable {
        if (a() && f()) {
            a(bArr, this.p + this.q + "_real.pcm");
        }
    }

    public int c() {
        if (a() && v()) {
            return this.u.get4MicDoaResult();
        }
        return -1;
    }

    public void c(int i2) {
        if (a() && u()) {
            c("set4MicDoaTimeLen -> " + i2);
            this.t.adjustStreamVolume(b, 0, i2);
        }
    }

    public void c(boolean z) {
        if (a() && v()) {
            this.u.set4MicDebugMode(z ? 1 : 0);
            c("setDebugMode set To board  -> " + z);
        }
        c("setDebugMode  -> " + z);
        this.m = z;
    }

    public void c(byte[] bArr) throws Throwable {
        if (a() && f()) {
            a(bArr, this.p + this.q + "_asr.pcm");
        }
    }

    public int d() {
        if (a()) {
            return this.t.getVibrateSetting(d);
        }
        return -1;
    }

    public void d(int i2) {
        if (a() && v()) {
            c("set4MicDoaTimeLen -> " + i2);
            this.u.set4MicUtteranceTimeLen(i2);
        }
        c("print debug log _set4MicDoaTimeLen -> " + i2);
    }

    public void d(boolean z) {
        if (u()) {
            this.t.adjustStreamVolume(g, 0, z ? 0 : 1);
            c("close4MicAlgorithm set To board  -> " + z);
        }
    }

    public void d(byte[] bArr) throws Throwable {
        if (a() && f()) {
            a(bArr, this.p + this.q + "_vad.pcm");
        }
    }

    public String e() {
        return a() ? this.u.get4MicBoardVersion() : "";
    }

    public void e(int i2) {
        this.n += i2;
    }

    public void e(boolean z) {
        if (v()) {
            int i2 = z ? 1 : 0;
            this.u.close4MicAlgorithm(i2);
            y.c("FourMicUtil", "close4MicAlgorithm flag = " + i2);
            c("_close4MicAlgorithm set To board  -> " + z);
        }
    }

    public void f(int i2) {
        if (a() && u()) {
            if (i2 < 0) {
                i2 = 0;
            }
            this.t.adjustStreamVolume(f, 0, i2);
            c("setDelayTime -> " + i2);
        }
    }

    public void f(boolean z) {
        if (!a() || !u()) {
            c("setOneShotReadyFor4Mic error not 4mic");
            return;
        }
        this.x = z;
        int i2 = z ? 1 : 0;
        m(i2);
        c("setOneShotReadyFor4Mic -> " + i2);
    }

    public boolean f() {
        return this.m;
    }

    public void g() {
        this.n = 0;
    }

    public void g(int i2) {
        if (a() && v()) {
            if (i2 < 0) {
                i2 = 0;
            }
            this.u.set4MicDelayTime(i2);
            c("setDelayTime -> " + i2);
        }
    }

    public void g(boolean z) {
        if (!a() || !v()) {
            c("setOneShotReadyFor4Mic error not 4mic");
            return;
        }
        this.x = z;
        int i2 = z ? 1 : 0;
        n(i2);
        c("setOneShotReadyFor4Mic -> " + i2);
    }

    public int h() {
        return this.n;
    }

    public void h(int i2) {
        this.o = i2;
    }

    public void h(boolean z) {
        this.z = z;
    }

    public int i() {
        return this.o;
    }

    public void i(int i2) {
        if (!a() || !u()) {
            c("setOneShotTimeStart error not 4mic");
        } else {
            c("setOneShotTimeStart -> " + i2);
            this.t.adjustStreamVolume(h, 0, i2);
        }
    }

    public void i(boolean z) {
        this.v = z;
    }

    public int j() {
        if (a() && u()) {
            return this.t.getVibrateSetting(k);
        }
        return -1;
    }

    public void j(int i2) {
        if (!a() || !v()) {
            c("_setOneShotTimeStart error not 4mic");
        } else {
            c("setOneShotTimeStart -> " + i2);
            this.u.set4MicOneShotStartLen(i2);
        }
    }

    public void j(boolean z) {
        this.w = z;
    }

    public int k() {
        if (a() && v()) {
            this.u.get4MicOneShotReady();
        }
        return -1;
    }

    public void k(int i2) {
        if (!a() || !u()) {
            c("setStartWakeupTimeLen error not 4mic");
            return;
        }
        if (i2 < 0) {
            c("setStartWakeupTimeLen -> timeLen min");
            i2 = 0;
        } else if (i2 > Integer.MAX_VALUE) {
            c("setStartWakeupTimeLen -> timeLen max");
            i2 = Integer.MAX_VALUE;
        }
        c("setStartWakeupTimeLen -> " + i2);
        this.t.adjustStreamVolume(i, 0, i2);
    }

    public void l(int i2) {
        if (!a() || !v()) {
            c("setStartWakeupTimeLen error not 4mic");
            return;
        }
        if (i2 < 0) {
            i2 = 0;
            c("setStartWakeupTimeLen -> timeLen min");
        } else if (i2 > Integer.MAX_VALUE) {
            c("setStartWakeupTimeLen -> timeLen max");
            i2 = Integer.MAX_VALUE;
        }
        c("setStartWakeupTimeLen -> " + i2);
        this.u.set4MicWakeupStartLen(i2);
    }

    public boolean l() {
        return this.x;
    }

    public int m() {
        return this.y;
    }

    public void m(int i2) {
        if (!a() || !u()) {
            c("setOneshotReady error not 4mic");
        } else {
            c("setOneshotReady -> " + i2);
            this.t.adjustStreamVolume(j, 0, i2);
        }
    }

    public void n() {
        this.y = 0;
    }

    public void n(int i2) {
        if (!a() || !v()) {
            c("setOneshotReady error not 4mic");
        } else {
            c("setOneshotReady -> " + i2);
            this.u.set4MicOneShotReady(i2);
        }
    }

    public void o(int i2) {
        this.y = i2;
    }

    public boolean o() {
        return this.z;
    }

    public Uni4micHalJNI p() {
        return this.u;
    }

    public void p(int i2) {
        this.y += i2;
    }

    public int q() {
        this.u = Uni4micHalJNI.getInstance();
        int iInit = this.u.init(1);
        y.c("FourMicUtil", "initFourMic status = " + iInit);
        return iInit;
    }

    public int r() {
        if (this.u != null) {
            return this.u.release();
        }
        return -1;
    }

    public boolean s() {
        return this.v;
    }

    public boolean t() {
        return this.w;
    }
}
