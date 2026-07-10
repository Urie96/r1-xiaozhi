package com.unisound.sdk;

import android.content.Context;
import cn.yunzhisheng.asr.VAD;
import cn.yunzhisheng.asrfix.JniAsrFix;
import com.unisound.client.SpeechConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public class j extends k {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    VAD f346a;
    aa b;
    boolean c;
    boolean d;
    public List<byte[]> e;
    boolean f;
    int g;
    private boolean t;
    private int u;
    private int v;
    private String w;

    public j(JniAsrFix jniAsrFix, String str, w wVar, Context context) {
        super(jniAsrFix, str, wVar, context);
        this.c = false;
        this.d = false;
        this.t = true;
        this.f = true;
        this.g = 0;
        this.u = 200;
        this.v = 3000;
        this.w = "";
    }

    public j(JniAsrFix jniAsrFix, String str, w wVar, aa aaVar, Context context) {
        super(jniAsrFix, str, wVar, context);
        this.c = false;
        this.d = false;
        this.t = true;
        this.f = true;
        this.g = 0;
        this.u = 200;
        this.v = 3000;
        this.w = "";
        this.f346a = aaVar.d;
        this.b = aaVar;
        this.e = new ArrayList();
    }

    private List<byte[]> a(BlockingQueue<byte[]> blockingQueue) {
        ArrayList arrayList = new ArrayList();
        long jAk = this.l.ak();
        while (!blockingQueue.isEmpty()) {
            byte[] bArrPoll = blockingQueue.poll();
            if (bArrPoll != null) {
                a(arrayList, bArrPoll, (int) jAk);
            }
        }
        return arrayList;
    }

    private void a(String str, boolean z) {
        if (str == null || str.length() < 0) {
            return;
        }
        if (l.d(str) < this.l.E()) {
            q();
            return;
        }
        if (this.l.z()) {
            com.unisound.common.j.a(false, this.l.c());
        }
        String strE = l.e(str);
        this.l.d(strE);
        int iL = this.m.l();
        int iQ = this.m.q();
        int i = iQ - iL;
        int iA = this.l.A(this.l.ag()) - iQ;
        com.unisound.common.y.c("utteranceEndTime = ", Integer.valueOf(iQ));
        com.unisound.common.y.c("utteranceStartTime = ", Integer.valueOf(iL));
        com.unisound.common.y.c("utteranceTime = ", Integer.valueOf(iQ - iL));
        com.unisound.common.y.c("delayTime = ", Integer.valueOf(iA));
        this.l.j(iL);
        this.l.k(i);
        long jU = this.l.U();
        long jR = this.l.R();
        long jH = this.l.h(jR - jU) + ((long) iL);
        com.unisound.common.y.c("wakeupLength = " + this.l.h(jU));
        com.unisound.common.y.c("outCacheLength = " + this.l.h(jR));
        this.l.l(true);
        com.unisound.common.ba.a();
        com.unisound.common.bd bdVar = new com.unisound.common.bd();
        bdVar.a(this.l.aZ());
        bdVar.b(com.unisound.c.a.a(this.l.aZ()));
        bdVar.c(this.l.bu());
        bdVar.a(l.d(str));
        bdVar.e(strE);
        bdVar.f(System.currentTimeMillis() + "");
        e();
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
        com.unisound.common.y.c("dropWakeup end wakeupCacheList.size() =>", Integer.valueOf(this.e.size()));
        linkedBlockingQueue.addAll(this.e);
        q();
        if (this.l.c != null && this.l.c.a()) {
            if (!this.l.ab()) {
                if (this.l.c.s()) {
                    this.l.c.g(iA);
                    this.l.c.d(i);
                } else {
                    this.l.c.f(iA);
                    this.l.c.c(i);
                }
            }
            if (!this.l.aa()) {
                this.l.p(1);
            }
            if (this.l.A()) {
                this.l.n(false);
                this.b.g();
                if (!this.l.T() && !this.i.isEmpty()) {
                    this.i.clear();
                }
                this.l.a(this.l.u(), this.l.W());
            }
        }
        if (this.l.A()) {
            this.f = false;
        }
        a(str, z, i, jH, iA, iL, iQ);
    }

    private synchronized void a(List<byte[]> list, byte[] bArr, int i) {
        int i2;
        synchronized (this) {
            list.add(bArr);
            int size = list.size() - 1;
            int i3 = 0;
            while (true) {
                if (size < 0) {
                    i2 = 0;
                    break;
                }
                int length = list.get(size).length + i3;
                if (length >= i) {
                    i2 = size;
                    break;
                } else {
                    size--;
                    i3 = length;
                }
            }
            for (int i4 = 0; i4 < i2; i4++) {
                list.remove(0);
            }
        }
    }

    private void b(boolean z) {
        this.o = true;
        String strL = this.l.L();
        int iA = this.l.A(this.l.ag()) - this.m.q();
        this.g = 0;
        this.f = true;
        if (!this.l.j(strL) || !z) {
            com.unisound.common.y.c("continue wakeup");
            this.l.k(false);
            this.l.e("");
            this.l.l(false);
            o();
            if (this.l.c == null || !this.l.c.a() || this.l.aa()) {
                return;
            }
            this.l.p(0);
            return;
        }
        com.unisound.common.y.c("continue oneshot");
        p();
        this.l.k(true);
        this.l.z(this.u + this.l.O() + iA);
        if (this.t) {
            this.t = false;
            if (!this.r.isEmpty() && this.b != null) {
                this.b.h();
                this.b.a(a(this.r));
            }
        }
        this.l.e(strL);
        this.l.d(this.l.k());
        this.l.h(false);
        if (this.l.c != null && this.l.c.a()) {
            if (this.l.c.s()) {
                this.l.c.g(true);
                this.l.c.l(this.l.A(this.l.c.m()) - (iA + this.l.O()));
            } else {
                this.l.c.f(true);
                this.l.c.k(this.l.A(this.l.c.m()) - (iA + this.l.O()));
            }
            this.l.n(true);
        }
        this.l.F(this.l.ay());
        k();
    }

    private int d(byte[] bArr) throws Throwable {
        if (bArr.length == 1 && (bArr[0] == 100 || bArr[0] == 99)) {
            this.o = true;
            return -1;
        }
        com.unisound.common.y.f("Before recognize");
        long jCurrentTimeMillis = System.currentTimeMillis();
        if (com.unisound.common.y.l) {
            com.unisound.common.y.d("FixRecognitionThread::BF_recognize ; current Time =", Long.valueOf(jCurrentTimeMillis));
        }
        a(1, 3, bArr);
        if (com.unisound.common.y.l) {
            com.unisound.common.y.d("FixRecognitionThread::read size =", Integer.valueOf(bArr.length), " first byte ", Byte.valueOf(bArr[0]));
        }
        this.l.c.e(bArr.length);
        if (com.unisound.common.y.r && com.unisound.common.y.s != null && !com.unisound.common.y.s.equals("")) {
            com.unisound.common.j.a(bArr, "/sdcard/asrtest/" + com.unisound.common.y.s + ".pcm");
        }
        int iB = this.m.b(bArr, bArr.length);
        this.l.w(bArr.length);
        if (this.l.A()) {
            if (!this.l.B() && (this.l.c == null || !this.l.c.a())) {
                this.l.g(bArr.length);
                c(bArr);
            }
            if (!this.l.B() && this.l.D()) {
                this.g += bArr.length;
                int iU = this.m.u();
                com.unisound.common.y.c("Wakeupsuccess check oneshotdata status = " + iU);
                if (iU == 4) {
                    b(true);
                } else if (this.g >= this.l.i(this.l.aK())) {
                    b(SpeechConstants.ASR_EVENT_ONESHOT_CHECK_END_TIMEOUT);
                    b(false);
                }
            }
        }
        com.unisound.common.y.f("queueHeadBuffer = ", Integer.valueOf(bArr.length));
        com.unisound.common.y.f("After recognize ");
        long jCurrentTimeMillis2 = System.currentTimeMillis();
        if (com.unisound.common.y.l) {
            com.unisound.common.y.d("FixRecognitionThread::AF_recognize ; current Time =", Long.valueOf(jCurrentTimeMillis2));
        }
        if (com.unisound.common.y.l) {
            com.unisound.common.y.d("FixRecognitionThread::recognize_process_time =", Long.valueOf(jCurrentTimeMillis2 - jCurrentTimeMillis));
        }
        if (this.w.equals("wakeup")) {
            a(bArr);
        }
        if (iB == 2) {
            if (this.l.z()) {
                com.unisound.common.j.a(false, 16000, this.l.c());
            }
            String strF = this.m.f();
            int iL = this.m.l();
            int iQ = this.m.q();
            if (this.l.A()) {
                if (!this.l.y()) {
                    a(strF, false, iL, iQ);
                } else if (!this.l.D()) {
                    a(strF, false);
                }
            } else if (this.l.y()) {
                a(strF, false);
            } else {
                a(strF, false, iL, iQ);
            }
            if (this.l.A() && this.l.B()) {
                this.c = true;
            }
            com.unisound.common.y.c("FixRecognitionThread:recognize=", Integer.valueOf(iB));
            com.unisound.common.y.c("FixRecognitionThread:partial = ", strF);
            a(3, 2, strF);
        } else if (iB == 3) {
            j();
            com.unisound.common.y.c("FixRecognitionThread:onRecognitionVADTimeout");
        } else if (iB == -6) {
            i();
            com.unisound.common.y.c("FixRecognitionThread:max timeout");
        } else if (iB != 1 && iB < 0) {
            d();
            a(JniAsrFix.a(iB));
            this.d = true;
            return -1;
        }
        if (!this.o) {
        }
        return 0;
    }

    private void q() {
        this.e.clear();
    }

    public synchronized void a(long j) {
        long j2 = 0;
        synchronized (this) {
            com.unisound.common.y.c("dropWakeupPcmFromByteLength =>", Long.valueOf(j));
            com.unisound.common.y.c("dropWakeup wakeupCacheList.size() =>", Integer.valueOf(this.e.size()));
            int size = this.e.size() - 1;
            long length = 0;
            while (true) {
                if (size < 0) {
                    break;
                }
                length += (long) this.e.get(size).length;
                if (length >= j) {
                    j2 = size;
                    break;
                }
                size--;
            }
            com.unisound.common.y.c("dropWakeup enabled = " + j2);
            for (int i = 0; i < j2; i++) {
                this.e.remove(0);
            }
        }
    }

    protected synchronized void a(byte[] bArr) {
        int i;
        synchronized (this) {
            this.e.add(bArr);
            int iBe = (this.l.be() / 1000) * this.v * 2;
            int size = this.e.size() - 1;
            int i2 = 0;
            while (true) {
                if (size < 0) {
                    i = 0;
                    break;
                }
                int length = this.e.get(size).length + i2;
                if (length >= iBe) {
                    i = size;
                    break;
                } else {
                    size--;
                    i2 = length;
                }
            }
            for (int i3 = 0; i3 < i; i3++) {
                this.e.remove(0);
            }
        }
    }

    @Override // com.unisound.sdk.k
    protected boolean a() {
        return (this.i.isEmpty() && (this.b.e.isEmpty() || this.b.b())) ? false : true;
    }

    /* JADX WARN: Removed duplicated region for block: B:115:0x029c A[ADDED_TO_REGION, REMOVE, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:83:0x028e A[Catch: all -> 0x009f, TRY_ENTER, TryCatch #1 {, blocks: (B:8:0x0012, B:10:0x0018, B:12:0x0022, B:13:0x0029, B:15:0x003e, B:17:0x0046, B:19:0x0060, B:20:0x009c, B:26:0x00a3, B:28:0x00ca, B:30:0x00d4, B:32:0x0101, B:72:0x01f6, B:33:0x0117, B:34:0x011d, B:36:0x0123, B:38:0x0129, B:40:0x0133, B:42:0x0141, B:44:0x0147, B:46:0x0151, B:48:0x0159, B:49:0x0161, B:80:0x0285, B:82:0x0289, B:83:0x028e, B:85:0x0292, B:87:0x0296, B:89:0x029c, B:91:0x02a9, B:93:0x02b7, B:95:0x02bd, B:97:0x02c1, B:51:0x0167, B:53:0x019b, B:55:0x01aa, B:101:0x02cd, B:103:0x02d5, B:105:0x02df, B:58:0x01b6, B:60:0x01c9, B:61:0x01cd, B:63:0x01d5, B:65:0x01dd, B:67:0x01e5, B:69:0x01ed, B:109:0x02fd, B:70:0x01f3, B:106:0x02ed, B:108:0x02f7, B:100:0x02c8, B:73:0x020e, B:75:0x0222, B:76:0x025e, B:78:0x0261), top: B:114:0x0012, inners: #0 }] */
    /* JADX WARN: Removed duplicated region for block: B:85:0x0292 A[Catch: all -> 0x009f, TryCatch #1 {, blocks: (B:8:0x0012, B:10:0x0018, B:12:0x0022, B:13:0x0029, B:15:0x003e, B:17:0x0046, B:19:0x0060, B:20:0x009c, B:26:0x00a3, B:28:0x00ca, B:30:0x00d4, B:32:0x0101, B:72:0x01f6, B:33:0x0117, B:34:0x011d, B:36:0x0123, B:38:0x0129, B:40:0x0133, B:42:0x0141, B:44:0x0147, B:46:0x0151, B:48:0x0159, B:49:0x0161, B:80:0x0285, B:82:0x0289, B:83:0x028e, B:85:0x0292, B:87:0x0296, B:89:0x029c, B:91:0x02a9, B:93:0x02b7, B:95:0x02bd, B:97:0x02c1, B:51:0x0167, B:53:0x019b, B:55:0x01aa, B:101:0x02cd, B:103:0x02d5, B:105:0x02df, B:58:0x01b6, B:60:0x01c9, B:61:0x01cd, B:63:0x01d5, B:65:0x01dd, B:67:0x01e5, B:69:0x01ed, B:109:0x02fd, B:70:0x01f3, B:106:0x02ed, B:108:0x02f7, B:100:0x02c8, B:73:0x020e, B:75:0x0222, B:76:0x025e, B:78:0x0261), top: B:114:0x0012, inners: #0 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void b() {
        int iQ;
        int iL = 0;
        int iN = n();
        if (iN != 0) {
            a(iN);
            this.d = true;
            return;
        }
        synchronized (this.m) {
            if (this.l.c != null && this.l.c.a()) {
                this.l.c.g();
            }
            this.l.V();
            this.l.ah();
            q();
            if (!this.l.A() || this.l.B()) {
                this.w = this.p;
                int iA = this.m.a(this.p, this.l.aB());
                if (iA < 0) {
                    com.unisound.common.y.a("FixRecognitionThread:jac::start error=" + iA + ",model=" + this.p + ",modelId=" + this.l.aB());
                    a(JniAsrFix.a(iA));
                    this.d = true;
                    return;
                }
                com.unisound.common.y.c("FixRecognitionThread:jac::start model=", this.p, ",modelId=", Integer.valueOf(this.l.aB()));
            } else {
                this.l.l(false);
                this.w = "wakeup";
                int iA2 = this.m.a("wakeup", this.l.aB());
                if (iA2 < 0) {
                    com.unisound.common.y.a("FixRecognitionThread:jac::start error=" + iA2 + ",model=" + this.p + ",modelId=" + this.l.aB());
                    a(JniAsrFix.a(iA2));
                    this.d = true;
                    return;
                }
                com.unisound.common.y.c("FixRecognitionThread:jac::start model=", "wakeup", ",modelId=", Integer.valueOf(this.l.aB()));
                if (this.l.c != null && this.l.c.a()) {
                    int size = this.b.e.size() * this.l.ai();
                    int size2 = this.i.size() * this.l.ai();
                    this.l.c.n();
                    if (this.l.c.s()) {
                        this.l.c.g(false);
                        this.l.c.j(this.l.A(size + size2));
                    } else {
                        this.l.c.f(false);
                        this.l.c.i(this.l.A(size + size2));
                    }
                }
            }
            a(0, 0, (Object) null);
            while (!l()) {
                try {
                    if (this.l.c != null && this.l.c.a()) {
                        byte[] bArrPoll = this.i.poll(h, TimeUnit.MILLISECONDS);
                        if (bArrPoll != null) {
                            if (this.l.c != null && this.l.c.a() && this.l.A()) {
                                this.l.c.p(bArrPoll.length);
                            }
                            if (d(bArrPoll) == -1) {
                                break;
                            }
                            if (com.unisound.common.y.q) {
                                com.unisound.common.j.a(bArrPoll, "/sdcard/asrtest/oneshotofflineFourMic.pcm");
                            }
                            if (this.o) {
                            }
                            com.unisound.common.y.b("FixRecognitionThread:recog break");
                            break;
                        }
                    } else {
                        byte[] bArrPoll2 = this.i.poll(h, TimeUnit.MILLISECONDS);
                        if (bArrPoll2 != null) {
                            if (d(bArrPoll2) != -1) {
                                if (com.unisound.common.y.q) {
                                    com.unisound.common.j.a(bArrPoll2, "/sdcard/asrtest/offline.pcm");
                                }
                                if (this.o) {
                                }
                                com.unisound.common.y.b("FixRecognitionThread:recog break");
                                break;
                            }
                            break;
                        }
                        if (this.o || (this.n && !a())) {
                            com.unisound.common.y.b("FixRecognitionThread:recog break");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.m.e();
            com.unisound.common.y.b("FixRecognitionThread:recog stopped");
            String strF = this.m.f();
            com.unisound.common.y.b("FixRecognitionThread:partial: ", strF);
            if (this.l.ag() < this.l.g() * 32) {
                com.unisound.common.y.b("FixRecognitionThread:Give Up Result:");
                strF = "";
            }
            if (strF == null || strF.equals("")) {
                strF = "\n-20.0";
                iQ = 0;
            } else if (!this.l.aO() || this.w.equals("wakeup")) {
                iQ = 0;
            } else {
                iL = this.m.l();
                iQ = this.m.q();
            }
            a(4, 2, strF);
            a(2, 0, (Object) null);
            if (this.l.y()) {
                a(strF, true);
            } else if (!this.w.equals("wakeup")) {
                a(strF, false, iL, iQ);
            }
            if (this.l.Y() == 0 && this.l.A() && this.l.B()) {
                if (this.m.t()) {
                    this.m.b(2);
                } else {
                    this.m.b(1);
                }
            }
        }
    }

    @Override // com.unisound.sdk.k
    public void c() {
        super.c();
    }

    @Override // com.unisound.sdk.k
    public void d() {
        super.d();
    }

    public void e() {
        if (this.e.size() > 0) {
            com.unisound.common.y.c("handleWakeupWordBuffer UtteranceTime = " + this.l.O());
            a(this.l.i(this.l.O() + 200));
        }
    }

    @Override // com.unisound.sdk.k, java.lang.Thread, java.lang.Runnable
    public void run() {
        this.t = true;
        this.l.ah();
        this.l.k(false);
        this.c = false;
        com.unisound.common.y.c("modelTag =>", this.p);
        if (this.l.A()) {
            this.m.i(this.l.aK());
        }
        if (this.l.c != null && this.l.c.a() && ((this.p.equals("wakeup") || this.l.A()) && !this.l.aa())) {
            this.l.p(0);
        }
        com.unisound.common.y.g("FixRecognitionThread start");
        if (com.unisound.common.y.l) {
            com.unisound.common.y.d("FixRecognitionThread ::run ThreadName = ", Thread.currentThread().getName(), ", ThreadId = ", Long.valueOf(Thread.currentThread().getId()));
        }
        if (l() || this.m == null) {
            return;
        }
        this.d = false;
        boolean z = true;
        while (!l() && ((!this.n || a()) && !this.d)) {
            if (com.unisound.common.y.r) {
                com.unisound.common.y.s = String.valueOf(System.currentTimeMillis());
            }
            if (!z) {
                this.o = false;
            }
            if (this.l.A() && this.l.B()) {
                this.c = true;
            }
            if (this.l.c == null || !this.l.c.a()) {
                if (this.l.A() && !this.f) {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                b();
                if (z) {
                    z = false;
                }
                if (!this.l.A() || !this.l.B()) {
                }
                if (this.c) {
                    break;
                }
            } else if (this.l.T()) {
                b();
                if (z) {
                    z = false;
                }
                if (!this.l.A() || !this.l.B()) {
                }
                if (this.c) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
        m();
        this.i.clear();
        com.unisound.common.y.g("FixRecognitionThread stop");
    }
}
