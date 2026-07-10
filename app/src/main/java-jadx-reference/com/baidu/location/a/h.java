package com.baidu.location.a;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import com.baidu.location.Jni;
import com.google.android.exoplayer2.text.ttml.TtmlNode;
import java.io.File;
import java.util.HashMap;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class h {
    private static Object c = new Object();
    private static h d = null;
    private static final String e = com.baidu.location.d.j.h() + "/hst.db";
    private SQLiteDatabase f = null;
    private boolean g = false;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    a f67a = null;
    a b = null;

    class a extends com.baidu.location.d.e {
        private String b = null;
        private String c = null;
        private boolean d = true;
        private boolean e = false;

        a() {
            this.k = new HashMap();
        }

        @Override // com.baidu.location.d.e
        public void a() {
            this.i = 1;
            this.h = com.baidu.location.d.j.c();
            String strD = Jni.d(this.c);
            this.c = null;
            this.k.put("bloc", strD);
        }

        public void a(String str, String str2) {
            if (h.this.g) {
                return;
            }
            h.this.g = true;
            this.b = str;
            this.c = str2;
            b(com.baidu.location.d.j.f);
        }

        @Override // com.baidu.location.d.e
        public void a(boolean z) {
            if (z && this.j != null) {
                try {
                    String str = this.j;
                    if (this.d) {
                        JSONObject jSONObject = new JSONObject(str);
                        JSONObject jSONObject2 = jSONObject.has("content") ? jSONObject.getJSONObject("content") : null;
                        if (jSONObject2 != null && jSONObject2.has("imo")) {
                            Long lValueOf = Long.valueOf(jSONObject2.getJSONObject("imo").getString("mac"));
                            int i = jSONObject2.getJSONObject("imo").getInt("mv");
                            if (Jni.c(this.b).longValue() == lValueOf.longValue()) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(TtmlNode.TAG_TT, Integer.valueOf((int) (System.currentTimeMillis() / 1000)));
                                contentValues.put("hst", Integer.valueOf(i));
                                try {
                                    if (h.this.f.update("hstdata", contentValues, "id = \"" + lValueOf + "\"", null) <= 0) {
                                        contentValues.put(TtmlNode.ATTR_ID, lValueOf);
                                        h.this.f.insert("hstdata", null, contentValues);
                                    }
                                } catch (Exception e) {
                                }
                                Bundle bundle = new Bundle();
                                bundle.putByteArray("mac", this.b.getBytes());
                                bundle.putInt("hotspot", i);
                                h.this.a(bundle);
                            }
                        }
                    }
                } catch (Exception e2) {
                }
            } else if (this.d) {
                h.this.f();
            }
            if (this.k != null) {
                this.k.clear();
            }
            h.this.g = false;
        }
    }

    public static h a() {
        h hVar;
        synchronized (c) {
            if (d == null) {
                d = new h();
            }
            hVar = d;
        }
        return hVar;
    }

    private String a(boolean z) {
        com.baidu.location.b.a aVarF = com.baidu.location.b.b.a().f();
        com.baidu.location.b.f fVarO = com.baidu.location.b.g.a().o();
        StringBuffer stringBuffer = new StringBuffer(1024);
        if (aVarF != null && aVarF.b()) {
            stringBuffer.append(aVarF.g());
        }
        if (fVarO != null && fVarO.a() > 1) {
            stringBuffer.append(fVarO.a(15));
        } else if (com.baidu.location.b.g.a().l() != null) {
            stringBuffer.append(com.baidu.location.b.g.a().l());
        }
        if (z) {
            stringBuffer.append("&imo=1");
        }
        stringBuffer.append(com.baidu.location.d.b.a().a(false));
        stringBuffer.append(com.baidu.location.a.a.a().c());
        return stringBuffer.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void a(Bundle bundle) {
        com.baidu.location.a.a.a().a(bundle, 406);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void f() {
        Bundle bundle = new Bundle();
        bundle.putInt("hotspot", -1);
        a(bundle);
    }

    public void a(String str) {
        if (this.g) {
            return;
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONObject jSONObject2 = jSONObject.has("content") ? jSONObject.getJSONObject("content") : null;
            if (jSONObject2 == null || !jSONObject2.has("imo")) {
                return;
            }
            Long lValueOf = Long.valueOf(jSONObject2.getJSONObject("imo").getString("mac"));
            int i = jSONObject2.getJSONObject("imo").getInt("mv");
            ContentValues contentValues = new ContentValues();
            contentValues.put(TtmlNode.TAG_TT, Integer.valueOf((int) (System.currentTimeMillis() / 1000)));
            contentValues.put("hst", Integer.valueOf(i));
            try {
                if (this.f.update("hstdata", contentValues, "id = \"" + lValueOf + "\"", null) <= 0) {
                    contentValues.put(TtmlNode.ATTR_ID, lValueOf);
                    this.f.insert("hstdata", null, contentValues);
                }
            } catch (Exception e2) {
            }
        } catch (Exception e3) {
        }
    }

    public void b() {
        try {
            File file = new File(e);
            if (!file.exists()) {
                file.createNewFile();
            }
            if (file.exists()) {
                this.f = SQLiteDatabase.openOrCreateDatabase(file, (SQLiteDatabase.CursorFactory) null);
                this.f.execSQL("CREATE TABLE IF NOT EXISTS hstdata(id Long PRIMARY KEY,hst INT,tt INT);");
                this.f.setVersion(1);
            }
        } catch (Exception e2) {
            this.f = null;
        }
    }

    public void c() {
        if (this.f != null) {
            try {
                this.f.close();
            } catch (Exception e2) {
            } finally {
                this.f = null;
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:24:0x0066  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public int d() throws Throwable {
        WifiInfo wifiInfoK;
        Cursor cursor;
        Throwable th;
        Cursor cursor2 = null;
        int i = -3;
        if (!this.g) {
            try {
                if (com.baidu.location.b.g.i() && this.f != null && (wifiInfoK = com.baidu.location.b.g.a().k()) != null && wifiInfoK.getBSSID() != null) {
                    try {
                        try {
                            Cursor cursorRawQuery = this.f.rawQuery("select * from hstdata where id = \"" + Jni.c(wifiInfoK.getBSSID().replace(":", "")) + "\";", null);
                            if (cursorRawQuery != null) {
                                try {
                                    i = cursorRawQuery.moveToFirst() ? cursorRawQuery.getInt(1) : -2;
                                    if (cursorRawQuery != null) {
                                        try {
                                            cursorRawQuery.close();
                                        } catch (Exception e2) {
                                        }
                                    }
                                } catch (Throwable th2) {
                                    cursor = cursorRawQuery;
                                    th = th2;
                                    if (cursor == null) {
                                        throw th;
                                    }
                                    try {
                                        cursor.close();
                                        throw th;
                                    } catch (Exception e3) {
                                        throw th;
                                    }
                                }
                            }
                        } catch (Throwable th3) {
                            cursor = null;
                            th = th3;
                        }
                    } catch (Exception e4) {
                        if (0 != 0) {
                            try {
                                cursor2.close();
                            } catch (Exception e5) {
                            }
                        }
                    }
                }
            } catch (Exception e6) {
            }
        }
        return i;
    }

    /* JADX WARN: Removed duplicated region for block: B:37:0x00b2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void e() {
        Cursor cursor;
        Cursor cursor2 = null;
        boolean z = true;
        if (this.g) {
            return;
        }
        try {
            if (!com.baidu.location.b.g.i() || this.f == null) {
                f();
                return;
            }
            WifiInfo wifiInfoK = com.baidu.location.b.g.a().k();
            if (wifiInfoK == null || wifiInfoK.getBSSID() == null) {
                f();
                return;
            }
            String strReplace = wifiInfoK.getBSSID().replace(":", "");
            boolean z2 = false;
            try {
                try {
                    Cursor cursorRawQuery = this.f.rawQuery("select * from hstdata where id = \"" + Jni.c(strReplace) + "\";", null);
                    if (cursorRawQuery != null) {
                        try {
                            if (cursorRawQuery.moveToFirst()) {
                                int i = cursorRawQuery.getInt(1);
                                if ((System.currentTimeMillis() / 1000) - ((long) cursorRawQuery.getInt(2)) <= 259200) {
                                    Bundle bundle = new Bundle();
                                    bundle.putByteArray("mac", strReplace.getBytes());
                                    bundle.putInt("hotspot", i);
                                    a(bundle);
                                    z = false;
                                }
                                z2 = z;
                            } else {
                                z2 = true;
                            }
                            if (cursorRawQuery != null) {
                                try {
                                    cursorRawQuery.close();
                                } catch (Exception e2) {
                                }
                            }
                        } catch (Exception e3) {
                            cursor = cursorRawQuery;
                            if (cursor != null) {
                                try {
                                    cursor.close();
                                } catch (Exception e4) {
                                }
                            }
                        }
                    }
                } catch (Exception e5) {
                    cursor = null;
                }
                if (z2) {
                    if (this.f67a == null) {
                        this.f67a = new a();
                    }
                    if (this.f67a != null) {
                        this.f67a.a(strReplace, a(true));
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        cursor2.close();
                    } catch (Exception e6) {
                    }
                }
                throw th;
            }
        } catch (Exception e7) {
        }
    }
}
