package com.baidu.location.b;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import cn.yunzhisheng.common.PinyinConverter;
import com.baidu.location.d.j;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public class f {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public List<ScanResult> f95a;
    private long b;
    private long c;
    private boolean d = false;
    private boolean e;

    public f(List<ScanResult> list, long j) {
        this.f95a = null;
        this.b = 0L;
        this.c = 0L;
        this.b = j;
        this.f95a = list;
        this.c = System.currentTimeMillis();
        k();
    }

    private boolean a(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return Pattern.compile("wpa|wep", 2).matcher(str).find();
    }

    private String b(String str) {
        return str != null ? (str.contains("&") || str.contains(";")) ? str.replace("&", "_").replace(";", "_") : str : str;
    }

    private void k() {
        boolean z;
        if (a() < 1) {
            return;
        }
        boolean z2 = true;
        for (int size = this.f95a.size() - 1; size >= 1 && z2; size--) {
            int i = 0;
            z2 = false;
            while (i < size) {
                if (this.f95a.get(i).level < this.f95a.get(i + 1).level) {
                    ScanResult scanResult = this.f95a.get(i + 1);
                    this.f95a.set(i + 1, this.f95a.get(i));
                    this.f95a.set(i, scanResult);
                    z = true;
                } else {
                    z = z2;
                }
                i++;
                z2 = z;
            }
        }
    }

    public int a() {
        if (this.f95a == null) {
            return 0;
        }
        return this.f95a.size();
    }

    public String a(int i) {
        return a(i, false, false);
    }

    /* JADX WARN: Removed duplicated region for block: B:134:0x039f  */
    /* JADX WARN: Removed duplicated region for block: B:137:0x03a9 A[PHI: r4
      0x03a9: PHI (r4v17 long) = (r4v1 long), (r4v4 long) binds: [B:16:0x005a, B:19:0x0067] A[DONT_GENERATE, DONT_INLINE]] */
    @SuppressLint({"NewApi"})
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public String a(int i, boolean z, boolean z2) {
        int i2;
        String str;
        String str2;
        long j;
        boolean z3;
        long j2;
        StringBuffer stringBuffer;
        int i3;
        int i4;
        long j3;
        char c;
        boolean z4;
        char c2;
        if (a() < 1) {
            return null;
        }
        char c3 = 0;
        try {
            try {
                Random random = new Random();
                StringBuffer stringBuffer2 = new StringBuffer(512);
                ArrayList<Long> arrayList = new ArrayList();
                WifiInfo wifiInfoK = g.a().k();
                if (wifiInfoK == null || wifiInfoK.getBSSID() == null) {
                    i2 = -1;
                    str = null;
                    str2 = null;
                } else {
                    String strReplace = wifiInfoK.getBSSID().replace(":", "");
                    int rssi = wifiInfoK.getRssi();
                    String strM = g.a().m();
                    if (rssi < 0) {
                        i2 = -rssi;
                        str = strM;
                        str2 = strReplace;
                    } else {
                        i2 = rssi;
                        str = strM;
                        str2 = strReplace;
                    }
                }
                long jElapsedRealtimeNanos = 0;
                long j4 = 0;
                boolean z5 = false;
                if (Build.VERSION.SDK_INT >= 17) {
                    try {
                        jElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos() / 1000;
                    } catch (Error e) {
                        jElapsedRealtimeNanos = 0;
                    }
                    if (jElapsedRealtimeNanos > 0) {
                        z5 = true;
                        j = jElapsedRealtimeNanos;
                    } else {
                        j = jElapsedRealtimeNanos;
                    }
                }
                boolean z6 = z5 ? z5 && z : z5;
                int i5 = 0;
                int i6 = 0;
                int size = this.f95a.size();
                boolean z7 = true;
                if (size <= i) {
                    i = size;
                }
                StringBuffer stringBuffer3 = null;
                int i7 = 0;
                while (i7 < i) {
                    if (this.f95a.get(i7).level == 0) {
                        stringBuffer = stringBuffer3;
                        i3 = i6;
                        boolean z8 = z7;
                        i4 = i5;
                        j3 = j4;
                        c = c3;
                        z4 = z8;
                    } else {
                        if (z6) {
                            try {
                                j2 = (j - this.f95a.get(i7).timestamp) / C.MICROS_PER_SECOND;
                            } catch (Exception e2) {
                                j2 = 0;
                            }
                            arrayList.add(Long.valueOf(j2));
                            if (j2 > j4) {
                                j4 = j2;
                            }
                        }
                        if (z7) {
                            z7 = false;
                            stringBuffer2.append("&wf=");
                            if (z2) {
                                stringBuffer3 = new StringBuffer();
                                stringBuffer3.append("&wf_ch=");
                                stringBuffer3.append(b(this.f95a.get(i7).frequency));
                            }
                        } else {
                            stringBuffer2.append(PinyinConverter.PINYIN_EXCLUDE);
                            if (z2) {
                                stringBuffer3.append(PinyinConverter.PINYIN_EXCLUDE);
                                stringBuffer3.append(b(this.f95a.get(i7).frequency));
                            }
                        }
                        String str3 = this.f95a.get(i7).BSSID;
                        if (str3 != null) {
                            String strReplace2 = str3.replace(":", "");
                            stringBuffer2.append(strReplace2);
                            int i8 = this.f95a.get(i7).level;
                            if (i8 < 0) {
                                i8 = -i8;
                            }
                            stringBuffer2.append(String.format(Locale.CHINA, ";%d;", Integer.valueOf(i8)));
                            i5++;
                            boolean z9 = false;
                            if (str2 != null && str2.equals(strReplace2)) {
                                this.e = a(this.f95a.get(i7).capabilities);
                                z9 = true;
                                i6 = i5;
                            }
                            if (z9) {
                                stringBuffer2.append(b(this.f95a.get(i7).SSID));
                                stringBuffer = stringBuffer3;
                                i3 = i6;
                                boolean z10 = z7;
                                i4 = i5;
                                j3 = j4;
                                c = c3;
                                z4 = z10;
                            } else if (c3 == 0) {
                                try {
                                    if (random.nextInt(10) != 2 || this.f95a.get(i7).SSID == null || this.f95a.get(i7).SSID.length() >= 30) {
                                        c2 = c3;
                                    } else {
                                        stringBuffer2.append(b(this.f95a.get(i7).SSID));
                                        c2 = 1;
                                    }
                                    z4 = z7;
                                    i4 = i5;
                                    int i9 = i6;
                                    j3 = j4;
                                    c = c2;
                                    stringBuffer = stringBuffer3;
                                    i3 = i9;
                                } catch (Exception e3) {
                                    stringBuffer = stringBuffer3;
                                    i3 = i6;
                                    boolean z11 = z7;
                                    i4 = i5;
                                    j3 = j4;
                                    c = c3;
                                    z4 = z11;
                                }
                            } else {
                                if (c3 == 1 && random.nextInt(20) == 1 && this.f95a.get(i7).SSID != null && this.f95a.get(i7).SSID.length() < 30) {
                                    stringBuffer2.append(b(this.f95a.get(i7).SSID));
                                    c2 = 2;
                                }
                                z4 = z7;
                                i4 = i5;
                                int i92 = i6;
                                j3 = j4;
                                c = c2;
                                stringBuffer = stringBuffer3;
                                i3 = i92;
                            }
                        } else {
                            stringBuffer = stringBuffer3;
                            i3 = i6;
                            boolean z102 = z7;
                            i4 = i5;
                            j3 = j4;
                            c = c3;
                            z4 = z102;
                        }
                    }
                    i7++;
                    boolean z12 = z4;
                    c3 = c;
                    j4 = j3;
                    i6 = i3;
                    i5 = i4;
                    stringBuffer3 = stringBuffer;
                    z7 = z12;
                }
                if (z7) {
                    return null;
                }
                stringBuffer2.append("&wf_n=" + i6);
                if (str2 != null && i2 != -1) {
                    stringBuffer2.append("&wf_rs=" + i2);
                }
                if (j4 > 10 && arrayList.size() > 0 && ((Long) arrayList.get(0)).longValue() > 0) {
                    StringBuffer stringBuffer4 = new StringBuffer(128);
                    stringBuffer4.append("&wf_ut=");
                    boolean z13 = true;
                    Long l = (Long) arrayList.get(0);
                    for (Long l2 : arrayList) {
                        if (z13) {
                            stringBuffer4.append(l2.longValue());
                            z3 = false;
                        } else {
                            long jLongValue = l2.longValue() - l.longValue();
                            if (jLongValue != 0) {
                                stringBuffer4.append("" + jLongValue);
                            }
                            z3 = z13;
                        }
                        stringBuffer4.append(PinyinConverter.PINYIN_EXCLUDE);
                        z13 = z3;
                    }
                    stringBuffer2.append(stringBuffer4.toString());
                }
                stringBuffer2.append("&wf_st=");
                stringBuffer2.append(this.b);
                stringBuffer2.append("&wf_et=");
                stringBuffer2.append(this.c);
                stringBuffer2.append("&wf_vt=");
                stringBuffer2.append(g.f96a);
                if (i6 > 0) {
                    this.d = true;
                    stringBuffer2.append("&wf_en=");
                    stringBuffer2.append(this.e ? 1 : 0);
                }
                if (str != null) {
                    stringBuffer2.append("&wf_gw=");
                    stringBuffer2.append(str);
                }
                if (stringBuffer3 != null) {
                    stringBuffer2.append(stringBuffer3.toString());
                }
                return stringBuffer2.toString();
            } catch (Error e4) {
                return null;
            }
        } catch (Exception e5) {
            return null;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x009a A[PHI: r8
      0x009a: PHI (r8v12 long) = (r8v0 long), (r8v3 long) binds: [B:3:0x000c, B:6:0x0019] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public boolean a(long j) {
        boolean z;
        long j2;
        long j3;
        long jElapsedRealtimeNanos = 0;
        long j4 = 0;
        long j5 = 0;
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                jElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos() / 1000;
            } catch (Error e) {
                jElapsedRealtimeNanos = 0;
            } catch (Exception e2) {
                jElapsedRealtimeNanos = 0;
            }
            if (jElapsedRealtimeNanos > 0) {
                z = true;
                j2 = jElapsedRealtimeNanos;
            } else {
                z = false;
                j2 = jElapsedRealtimeNanos;
            }
        }
        if (!z || this.f95a == null || this.f95a.size() == 0) {
            return false;
        }
        int size = this.f95a.size();
        int i = size > 16 ? 16 : size;
        for (int i2 = 0; i2 < i; i2++) {
            if (this.f95a.get(i2).level != 0 && z) {
                try {
                    j3 = (j2 - this.f95a.get(i2).timestamp) / C.MICROS_PER_SECOND;
                } catch (Error e3) {
                    j3 = 0;
                } catch (Exception e4) {
                    j3 = 0;
                }
                j5 += j3;
                if (j3 > j4) {
                    j4 = j3;
                }
            }
        }
        return 1000 * j4 > j || (j5 / ((long) i)) * 1000 > j;
    }

    public boolean a(f fVar) {
        if (this.f95a == null || fVar == null || fVar.f95a == null) {
            return false;
        }
        int size = this.f95a.size() < fVar.f95a.size() ? this.f95a.size() : fVar.f95a.size();
        for (int i = 0; i < size; i++) {
            if (!this.f95a.get(i).BSSID.equals(fVar.f95a.get(i).BSSID)) {
                return false;
            }
        }
        return true;
    }

    public int b(int i) {
        if (i <= 2400 || i >= 2500) {
            return (i <= 4900 || i >= 5900) ? 0 : 5;
        }
        return 2;
    }

    public String b() {
        try {
            return a(j.N, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean b(f fVar) {
        if (this.f95a == null || fVar == null || fVar.f95a == null) {
            return false;
        }
        int size = this.f95a.size() < fVar.f95a.size() ? this.f95a.size() : fVar.f95a.size();
        for (int i = 0; i < size; i++) {
            String str = this.f95a.get(i).BSSID;
            int i2 = this.f95a.get(i).level;
            String str2 = fVar.f95a.get(i).BSSID;
            int i3 = fVar.f95a.get(i).level;
            if (!str.equals(str2) || i2 != i3) {
                return false;
            }
        }
        return true;
    }

    public String c() {
        try {
            return a(j.N, true, false);
        } catch (Exception e) {
            return null;
        }
    }

    public String c(int i) {
        int i2;
        int i3 = 0;
        if (i == 0 || a() < 1) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer(256);
        int size = this.f95a.size();
        int i4 = size > j.N ? j.N : size;
        int i5 = 1;
        int i6 = 0;
        while (i6 < i4) {
            if ((i5 & i) == 0 || this.f95a.get(i6).BSSID == null) {
                i2 = i3;
            } else {
                if (i3 == 0) {
                    stringBuffer.append("&ssid=");
                } else {
                    stringBuffer.append(PinyinConverter.PINYIN_EXCLUDE);
                }
                stringBuffer.append(this.f95a.get(i6).BSSID.replace(":", ""));
                stringBuffer.append(";");
                stringBuffer.append(b(this.f95a.get(i6).SSID));
                i2 = i3 + 1;
            }
            i5 <<= 1;
            i6++;
            i3 = i2;
        }
        return stringBuffer.toString();
    }

    public boolean c(f fVar) {
        return g.a(fVar, this);
    }

    public String d() {
        try {
            return a(15);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean e() {
        return a(j.ae);
    }

    public int f() {
        for (int i = 0; i < a(); i++) {
            int i2 = -this.f95a.get(i).level;
            if (i2 > 0) {
                return i2;
            }
        }
        return 0;
    }

    public boolean g() {
        return this.d;
    }

    public boolean h() {
        return System.currentTimeMillis() - this.c > 0 && System.currentTimeMillis() - this.c < DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;
    }

    public boolean i() {
        return System.currentTimeMillis() - this.c > 0 && System.currentTimeMillis() - this.c < DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;
    }

    public boolean j() {
        return System.currentTimeMillis() - this.c > 0 && System.currentTimeMillis() - this.b < DefaultRenderersFactory.DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS;
    }
}
