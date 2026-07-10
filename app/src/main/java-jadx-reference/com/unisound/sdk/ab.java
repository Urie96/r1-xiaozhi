package com.unisound.sdk;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import cn.yunzhisheng.asrfix.JniAsrFix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import nluparser.scheme.ASR;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class ab {
    public static final String f = "ml";
    public static final int i = 0;
    public static final int j = -100;
    public static final int k = -200;
    public static final int l = -300;
    public static final int n = 1000;
    public static final int o = 1001;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public String f293a;
    private static String q = "assetsModelsMD5";
    public static int m = 20;
    private static g t = new g();
    public String[] b = {"tri", "l", "wid", "am", "digit", "wseg", "stat"};
    public String c = "am";
    public String d = ASR.NET;
    private String p = ".dat";
    public String e = "main";
    public boolean g = false;
    private boolean r = false;
    private List<String> s = new ArrayList();
    public boolean h = false;
    private ac u = null;

    public ab() {
        t.a(this);
    }

    private void a(Map<String, String> map) throws Throwable {
        JSONObject jSONObject;
        RandomAccessFile randomAccessFile;
        RandomAccessFile randomAccessFile2 = null;
        try {
            try {
                jSONObject = new JSONObject();
                for (String str : map.keySet()) {
                    jSONObject.put(str, map.get(str));
                }
                randomAccessFile = new RandomAccessFile(this.f293a + q, "rw");
            } catch (Throwable th) {
                th = th;
            }
        } catch (Exception e) {
            e = e;
        }
        try {
            randomAccessFile.write(jSONObject.toString().getBytes());
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (Exception e3) {
            e = e3;
            randomAccessFile2 = randomAccessFile;
            e.printStackTrace();
            if (randomAccessFile2 != null) {
                try {
                    randomAccessFile2.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        } catch (Throwable th2) {
            th = th2;
            randomAccessFile2 = randomAccessFile;
            if (randomAccessFile2 != null) {
                try {
                    randomAccessFile2.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    private boolean a(AssetManager assetManager, String str) {
        boolean z;
        this.r = false;
        try {
            File file = new File(str);
            if (!file.exists()) {
                return true;
            }
            InputStream inputStreamOpen = assetManager.open("version/data");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamOpen));
            String line = bufferedReader.readLine();
            inputStreamOpen.close();
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(file));
            String line2 = bufferedReader2.readLine();
            z = (line2 == null || line2.equals(line)) ? false : true;
            bufferedReader.close();
            bufferedReader2.close();
            FileReader fileReader = new FileReader(new File(this.f293a + q));
            BufferedReader bufferedReader3 = new BufferedReader(fileReader);
            String line3 = bufferedReader3.readLine();
            JSONObject jSONObject = line3 != null ? new JSONObject(line3) : null;
            for (String str2 : this.b) {
                String strK = k(this.f293a + str2 + ".dat");
                String string = jSONObject != null ? jSONObject.getString(str2) : "";
                if (TextUtils.isEmpty(string) || TextUtils.isEmpty(strK)) {
                    this.r = true;
                    this.s.add(str2);
                    z = true;
                } else if (!string.equalsIgnoreCase(strK)) {
                    this.r = true;
                    this.s.add(str2);
                    z = true;
                }
            }
            bufferedReader3.close();
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            z = true;
        }
        com.unisound.common.y.c("ModelData isUpdateModel = " + z);
        return z;
    }

    /* JADX WARN: Removed duplicated region for block: B:51:0x008d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(AssetManager assetManager, String str, String str2) throws Throwable {
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2 = null;
        try {
            try {
                fileOutputStream = new FileOutputStream(new File(str2));
                try {
                    byte[] bArr = new byte[10240];
                    if (m == 1) {
                        InputStream inputStreamOpen = assetManager.open(str + "/data");
                        while (true) {
                            int i2 = inputStreamOpen.read(bArr, 0, bArr.length);
                            if (i2 <= 0) {
                                break;
                            }
                            fileOutputStream.write(bArr, 0, i2);
                        }
                        inputStreamOpen.close();
                    } else {
                        for (String str3 : assetManager.list(str)) {
                            InputStream inputStreamOpen2 = assetManager.open(str + MqttTopic.TOPIC_LEVEL_SEPARATOR + str3);
                            while (true) {
                                int i3 = inputStreamOpen2.read(bArr, 0, bArr.length);
                                if (i3 <= 0) {
                                    break;
                                }
                                fileOutputStream.write(bArr, 0, i3);
                            }
                            inputStreamOpen2.close();
                        }
                    }
                    if (fileOutputStream == null) {
                        return true;
                    }
                    try {
                        fileOutputStream.close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return true;
                    }
                } catch (Exception e2) {
                    e = e2;
                    com.unisound.common.y.a("init asr model error");
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                            return false;
                        }
                    }
                    return false;
                }
            } catch (Throwable th) {
                th = th;
                if (0 != 0) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            fileOutputStream = null;
        } catch (Throwable th2) {
            th = th2;
            if (0 != 0) {
            }
            throw th;
        }
    }

    public static boolean a(AssetManager assetManager, String str, String str2, boolean z, boolean z2) {
        if (!z && new File(str2).exists()) {
            if (!z2 || JniAsrFix.a(str2)) {
                return true;
            }
            com.unisound.common.y.a("reset model file " + str2);
        }
        return a(assetManager, str, str2);
    }

    private String b(AssetManager assetManager, String str) {
        try {
            return com.unisound.common.aa.a(assetManager.open(str + "/data"));
        } catch (Exception e) {
            com.unisound.common.y.a("getMd5FromAssets from assets error!");
            e.printStackTrace();
            return "";
        }
    }

    public static boolean d(String str) {
        return JniAsrFix.a(str);
    }

    private boolean i(String str) {
        File file = new File(str);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    private static void j(String str) {
        com.unisound.common.y.a(str);
    }

    private String k(String str) {
        try {
            return com.unisound.common.aa.a(new File(str));
        } catch (Exception e) {
            com.unisound.common.y.a("getMd5FromAssets from file error!");
            e.printStackTrace();
            return "";
        }
    }

    public int a(int i2) {
        return t.a(i2);
    }

    public int a(String str, String str2) {
        return t.a(str, str2);
    }

    public int a(String str, String str2, String str3) {
        return t.a(str, str2, str3);
    }

    public int a(String str, String str2, String str3, String str4) {
        File file = new File(str4);
        if (file.exists()) {
            file.delete();
        } else {
            File file2 = new File(file.getParent());
            if (!file2.exists()) {
                file2.mkdirs();
            }
        }
        return t.a() ? t.a(str, str2, str3, str4) : t.a(str, str2, str3, this.f293a, str4);
    }

    public int a(boolean z) {
        return z ? t.b(1) : t.b(0);
    }

    public String a() {
        return this.f293a + this.d + ".dat";
    }

    public String a(String str, List<String> list) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<" + str + ">").append("\n");
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            stringBuffer.append(it.next()).append("\n");
        }
        stringBuffer.append("</" + str + ">");
        com.unisound.common.y.c("ModelData : ", "getVocabString --> vocab = ", stringBuffer.toString());
        return stringBuffer.toString();
    }

    public void a(ac acVar) {
        this.u = acVar;
    }

    public void a(String str) {
        this.f293a = str + MqttTopic.TOPIC_LEVEL_SEPARATOR;
    }

    public boolean a(Context context) {
        return a(context, false);
    }

    public boolean a(Context context, String str) {
        try {
            InputStream inputStreamOpen = context.getAssets().open(str);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamOpen));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    inputStreamOpen.close();
                    return true;
                }
                String[] strArrSplit = line.split("=");
                if (strArrSplit.length == 2) {
                    String strTrim = strArrSplit[0].trim();
                    String strTrim2 = strArrSplit[1].trim();
                    if ("models".equals(strTrim)) {
                        this.b = strTrim2.split(",");
                    } else if ("am".equals(strTrim)) {
                        this.c = strTrim2;
                    } else if ("custom".equals(strTrim)) {
                        this.d = strTrim2;
                    } else if ("domain".equals(strTrim)) {
                        this.e = strTrim2;
                    }
                }
            }
        } catch (Exception e) {
            com.unisound.common.y.a("model list error");
            e.printStackTrace();
            return false;
        }
    }

    public boolean a(Context context, boolean z) {
        synchronized (this) {
            if (this.g) {
                return true;
            }
            AssetManager assets = context.getAssets();
            File file = new File(this.f293a);
            if (!file.exists()) {
                file.mkdirs();
            }
            boolean zA = a(assets, this.f293a + "version");
            if (zA) {
                com.unisound.common.y.a("init asr models..");
                HashMap map = new HashMap();
                if (this.r) {
                    for (String str : this.s) {
                        com.unisound.common.y.c("ModelData , partCopy : model = " + str);
                        if (!a(assets, str, this.f293a + str + ".dat", zA, this.h)) {
                            return false;
                        }
                    }
                }
                for (String str2 : this.b) {
                    if (!this.r && !a(assets, str2, this.f293a + str2 + ".dat", zA, this.h)) {
                        return false;
                    }
                    map.put(str2, b(assets, str2));
                }
                com.unisound.common.y.a("init asr models ok");
                a(assets, "version", this.f293a + "version");
                a(map);
                this.s.clear();
            } else {
                com.unisound.common.y.a("init not overwrite models..");
            }
            if (!t.a() && z && t.a(this.f293a)) {
                this.u.a(1000);
            }
            this.g = true;
            return true;
        }
    }

    public String b(String str) {
        return this.f293a + str + "_partialFile";
    }

    public boolean b() {
        String str = this.f293a + this.d + this.p;
        if (JniAsrFix.a(str)) {
            return true;
        }
        i(str);
        return false;
    }

    public boolean b(Context context, String str) {
        try {
            for (String str2 : context.getAssets().list("")) {
                if (str2.equals(str)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean c() {
        boolean zA;
        synchronized (this) {
            zA = JniAsrFix.a(this.f293a + this.d + this.p);
        }
        return zA;
    }

    public boolean c(String str) {
        return false;
    }

    public int d() {
        try {
        } catch (Exception e) {
            e.printStackTrace();
            com.unisound.common.y.a("setAMFile error");
        }
        synchronized (this) {
            File file = new File(this.f293a + this.c + this.p);
            if (!file.exists()) {
                return 0;
            }
            return (int) file.length();
        }
    }

    public int e(String str) {
        int iCompileDecodeNet = JniAsrFix.compileDecodeNet(this.f293a, str);
        if (iCompileDecodeNet == 0) {
            return 0;
        }
        j("setUserData DecodeNet error:" + iCompileDecodeNet);
        return l;
    }

    public boolean e() {
        return JniAsrFix.a(this.f293a + this.c);
    }

    public String f(String str) {
        String str2 = "#JSGF V1.0 utf-8 cn;\ngrammar " + str + ";\npublic <" + str + "> =( \"<s>\" (\n<NAME>\n) \"</s>\");";
        com.unisound.common.y.c("ModelData : ", "getJsgf --> jsgf = ", str2);
        return str2;
    }

    public void f() {
        t.b();
    }

    public String g() {
        return t.c();
    }

    public String g(String str) {
        return this.f293a + "jsgf_model/" + str + ".dat";
    }

    public int h(String str) {
        return t.b(str);
    }

    public g h() {
        return t;
    }
}
