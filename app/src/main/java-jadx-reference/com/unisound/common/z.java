package com.unisound.common;

import java.io.File;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
final class z implements Runnable {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ String f274a;

    z(String str) {
        this.f274a = str;
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x00cc A[Catch: all -> 0x00a0, DONT_GENERATE, EDGE_INSN: B:38:0x00cc->B:32:0x00cc BREAK  A[LOOP:0: B:12:0x0025->B:20:0x003e], TRY_ENTER, TRY_LEAVE, TryCatch #0 {, blocks: (B:5:0x0007, B:7:0x0017, B:9:0x001d, B:12:0x0025, B:14:0x0028, B:16:0x002e, B:18:0x0036, B:21:0x0042, B:23:0x008d, B:30:0x00a3, B:26:0x009c, B:32:0x00cc), top: B:34:0x0007, inners: #1 }] */
    @Override // java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() {
        String strB;
        int i = 0;
        synchronized (y.T) {
            boolean unused = y.R = true;
            File file = new File(this.f274a);
            if (file.exists() && file.isDirectory()) {
                File[] fileArrListFiles = file.listFiles();
                if (fileArrListFiles.length > 0) {
                    while (true) {
                        int i2 = i;
                        if (i2 >= fileArrListFiles.length) {
                            break;
                        }
                        if (y.R && (strB = y.b(fileArrListFiles[i2])) != null && !strB.equals("")) {
                            try {
                                JSONObject jSONObject = new JSONObject(strB);
                                if (new w().a(new x(jSONObject.getString(x.f272a), jSONObject.getString(x.b), jSONObject.getString(x.c), jSONObject.getString(x.e), jSONObject.getString(x.f), jSONObject.getInt("status"), jSONObject.getString(x.h), jSONObject.getString(x.i), jSONObject.getString(x.j))) == 0) {
                                    int unused2 = y.S = 0;
                                    fileArrListFiles[i2].delete();
                                    boolean unused3 = y.R = true;
                                } else {
                                    y.d();
                                    y.a("postLogError " + y.S + " times");
                                    boolean unused4 = y.R = false;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        i = i2 + 1;
                    }
                }
            }
        }
    }
}
