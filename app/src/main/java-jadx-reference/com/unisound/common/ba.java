package com.unisound.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public class ba {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private static final int f252a = 10485760;
    private static final String d = "wakeupkeys";
    private static final String e = "http://asr-wakeup.hivoice.cn:8080/uc/m/upload";
    private static final int f = 5000;
    private static final String g = "WakeupWordCacheAndUpload";
    private static l b = null;
    private static int c = 1024;
    private static ba h = null;
    private static Object i = new Object();

    private ba() {
    }

    public static ba a() {
        if (h == null) {
            synchronized (i) {
                if (h == null) {
                    h = new ba();
                }
            }
        }
        return h;
    }

    public static File a(Context context, String str) {
        return new File((("mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath()) + File.separator + str);
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x018e  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static String a(String str, bd bdVar, BlockingQueue<byte[]> blockingQueue, int i2) throws Throwable {
        String str2;
        HttpURLConnection httpURLConnection;
        Exception e2;
        String str3;
        y.c("WakeupWordCacheAndUpload httpPost params : K = " + bdVar.a() + ",UI = " + bdVar.b() + ",U = " + bdVar.c() + ",Text = " + bdVar.e() + bdVar.f() + ",Timestamp = " + bdVar.g() + ",AC = " + bdVar.d());
        HttpURLConnection httpURLConnection2 = null;
        String str4 = "";
        try {
            HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection3.setRequestProperty("K", URLEncoder.encode(bdVar.a(), "UTF-8"));
                httpURLConnection3.setRequestProperty("UI", URLEncoder.encode(bdVar.b(), "UTF-8"));
                httpURLConnection3.setRequestProperty("U", URLEncoder.encode(bdVar.c(), "UTF-8"));
                httpURLConnection3.setRequestProperty("Text", URLEncoder.encode(bdVar.e() + bdVar.f(), "UTF-8"));
                httpURLConnection3.setRequestProperty("AC", URLEncoder.encode(bdVar.d(), "UTF-8"));
                httpURLConnection3.setRequestProperty("Timestamp", URLEncoder.encode(bdVar.g(), "UTF-8"));
                httpURLConnection3.setRequestProperty("Accept-Charset", "UTF-8");
                httpURLConnection3.setRequestMethod("POST");
                httpURLConnection3.setDoInput(true);
                httpURLConnection3.setDoOutput(true);
                httpURLConnection3.setConnectTimeout(i2);
                OutputStream outputStream = httpURLConnection3.getOutputStream();
                while (true) {
                    byte[] bArrPoll = blockingQueue.poll();
                    if (bArrPoll == null) {
                        break;
                    }
                    outputStream.write(bArrPoll);
                    if (y.p) {
                        j.a(bArrPoll, "/sdcard/asrtest/uploadwakeup.pcm");
                    }
                }
                outputStream.flush();
                outputStream.close();
                int responseCode = httpURLConnection3.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection3.getInputStream(), com.unisound.b.f.b));
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        str4 = str4 + line;
                    }
                    bufferedReader.close();
                    str3 = str4;
                } else {
                    y.a(g, "responseCode = " + responseCode);
                    str3 = "";
                }
                if (httpURLConnection3 == null) {
                    return str3;
                }
                httpURLConnection3.disconnect();
                return str3;
            } catch (Exception e3) {
                e2 = e3;
                httpURLConnection = httpURLConnection3;
                str2 = "";
                try {
                    e2.printStackTrace();
                    if (httpURLConnection == null) {
                        return str2;
                    }
                    httpURLConnection.disconnect();
                    return str2;
                } catch (Throwable th) {
                    th = th;
                    httpURLConnection2 = httpURLConnection;
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                httpURLConnection2 = httpURLConnection3;
                th = th2;
                if (httpURLConnection2 != null) {
                }
                throw th;
            }
        } catch (Exception e4) {
            str2 = "";
            httpURLConnection = null;
            e2 = e4;
        } catch (Throwable th3) {
            th = th3;
        }
    }

    private static String a(String str, Map<String, String> map) {
        String strEncode;
        y.c("WakeupWordCacheAndUpload buildGetUrl");
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("?");
        String str2 = "";
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (true) {
            String str3 = str2;
            if (!it.hasNext()) {
                return sb.toString();
            }
            Map.Entry<String, String> next = it.next();
            sb.append(str3);
            sb.append(next.getKey());
            sb.append("=");
            try {
                strEncode = URLEncoder.encode(next.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e2) {
                strEncode = "";
            }
            sb.append(strEncode);
            str2 = "&";
        }
    }

    private static void a(Context context, String str, bd bdVar) {
        y.c("WakeupWordCacheAndUpload saveParamsValue");
        String strA = bdVar.a();
        String strB = bdVar.b();
        String strC = bdVar.c();
        String str2 = bdVar.e() + "_" + bdVar.f();
        String strD = bdVar.d();
        String strG = bdVar.g();
        StringBuilder sb = new StringBuilder();
        sb.append(strA).append(MqttTopic.MULTI_LEVEL_WILDCARD).append(strB).append(MqttTopic.MULTI_LEVEL_WILDCARD).append(strC).append(MqttTopic.MULTI_LEVEL_WILDCARD).append(str2).append(MqttTopic.MULTI_LEVEL_WILDCARD).append(strG).append(MqttTopic.MULTI_LEVEL_WILDCARD).append(strD).append(MqttTopic.MULTI_LEVEL_WILDCARD).append("end");
        SharedPreferences.Editor editorEdit = context.getSharedPreferences(d, 0).edit();
        y.c("WakeupWordCacheAndUpload saveUploadKey : key = " + str + ", value = " + sb.toString());
        editorEdit.putString(str, sb.toString());
        editorEdit.commit();
    }

    /* JADX WARN: Removed duplicated region for block: B:54:0x00bd A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static boolean a(Context context, String str, BlockingQueue<byte[]> blockingQueue) throws Throwable {
        OutputStream outputStream;
        Exception e2;
        n nVar;
        OutputStream outputStreamC;
        Throwable th;
        OutputStream outputStream2 = null;
        y.c("WakeupWordCacheAndUpload saveData2Cache");
        try {
            try {
                if (b == null) {
                    b = l.a(a(context, "locationData"), 1, 1, 10485760L);
                }
                n nVarB = b.b(str);
                try {
                    if (nVarB != null) {
                        outputStreamC = nVarB.c(0);
                        try {
                            y.c("WakeupWordCacheAndUpload saveData2Cache: write BlockingQueue size :" + blockingQueue.size());
                            while (true) {
                                byte[] bArrPoll = blockingQueue.poll(50L, TimeUnit.MILLISECONDS);
                                if (bArrPoll == null) {
                                    break;
                                }
                                byte[] bArr = new byte[bArrPoll.length];
                                System.arraycopy(bArrPoll, 0, bArr, 0, bArr.length);
                                outputStreamC.write(bArr);
                            }
                            outputStreamC.flush();
                            nVarB.a();
                            b.e();
                            outputStream2 = outputStreamC;
                        } catch (Exception e3) {
                            e2 = e3;
                            outputStream = outputStreamC;
                            nVar = nVarB;
                            try {
                                e2.printStackTrace();
                                if (nVar != null) {
                                    try {
                                        nVar.b();
                                    } catch (IOException e4) {
                                        e4.printStackTrace();
                                    }
                                }
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (IOException e5) {
                                        e5.printStackTrace();
                                        return false;
                                    }
                                }
                                return false;
                            } catch (Throwable th2) {
                                th = th2;
                                outputStreamC = outputStream;
                                if (outputStreamC != null) {
                                    try {
                                        outputStreamC.close();
                                    } catch (IOException e6) {
                                        e6.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (outputStreamC != null) {
                            }
                            throw th;
                        }
                    } else {
                        y.a("WakeupWordCacheAndUpload DiskLruCacheWrapper: edit key:" + str + " , editor id null!");
                    }
                    if (outputStream2 != null) {
                        try {
                            outputStream2.close();
                        } catch (IOException e7) {
                            e7.printStackTrace();
                            return true;
                        }
                    }
                    return true;
                } catch (Exception e8) {
                    outputStream = outputStream2;
                    e2 = e8;
                    nVar = nVarB;
                }
            } catch (Throwable th4) {
                outputStreamC = null;
                th = th4;
            }
        } catch (Exception e9) {
            outputStream = null;
            e2 = e9;
            nVar = null;
        }
    }

    public static void b() {
        if (b != null) {
            try {
                b.close();
                b = null;
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void b(Context context, bd bdVar, BlockingQueue<byte[]> blockingQueue) {
        y.c("WakeupWordCacheAndUpload saveParamAndCache");
        String string = Long.toString(System.currentTimeMillis());
        if (a(context, string, blockingQueue)) {
            a(context, string, bdVar);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean b(ConnectivityManager connectivityManager) {
        if (connectivityManager == null) {
            return false;
        }
        for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) {
            if (NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ArrayList<String> c(Context context) {
        ArrayList<String> arrayList;
        Exception exc;
        ArrayList<String> arrayListG;
        y.c("WakeupWordCacheAndUpload getCacheTimekeyList");
        ArrayList<String> arrayList2 = new ArrayList<>();
        try {
            if (b == null) {
                b = l.a(a(context, "locationData"), 1, 1, 10485760L);
            }
            arrayListG = b.g();
        } catch (Exception e2) {
            arrayList = arrayList2;
            exc = e2;
        }
        try {
            y.c("KEY", "key size is " + arrayListG.size());
            Iterator<String> it = arrayListG.iterator();
            while (it.hasNext()) {
                y.c("KEY", "key is " + it.next());
            }
            return arrayListG;
        } catch (Exception e3) {
            arrayList = arrayListG;
            exc = e3;
            exc.printStackTrace();
            return arrayList;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static BlockingQueue<byte[]> c(Context context, String str) {
        y.c("WakeupWordCacheAndUpload getCacheData");
        InputStream inputStreamA = null;
        LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
        try {
            try {
                if (b == null) {
                    b = l.a(a(context, "locationData"), 1, 1, 10485760L);
                }
                q qVarA = b.a(str);
                if (qVarA != null) {
                    inputStreamA = qVarA.a(0);
                    byte[] bArr = new byte[c];
                    while (true) {
                        int i2 = inputStreamA.read(bArr);
                        if (i2 == -1) {
                            break;
                        }
                        byte[] bArr2 = new byte[i2];
                        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
                        linkedBlockingQueue.add(bArr2);
                    }
                }
                if (inputStreamA != null) {
                    try {
                        inputStreamA.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (Exception e3) {
                e3.printStackTrace();
                if (inputStreamA != null) {
                    try {
                        inputStreamA.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
            }
            return linkedBlockingQueue;
        } catch (Throwable th) {
            if (inputStreamA != null) {
                try {
                    inputStreamA.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean c(Context context, bd bdVar, BlockingQueue<byte[]> blockingQueue) throws Throwable {
        boolean z;
        y.c("WakeupWordCacheAndUpload uploadWakeupContent");
        if (!b((ConnectivityManager) context.getSystemService("connectivity"))) {
            return false;
        }
        String strA = a(e, bdVar, blockingQueue, 5000);
        y.c("WakeupWordCacheAndUpload uploadWakeup result = " + strA);
        if (strA.equals("")) {
            return false;
        }
        try {
            String string = new JSONObject(strA).getString("rc");
            if (string.equals("0")) {
                z = true;
            } else {
                if (string.equals("-1002")) {
                    y.a("WakeupWordCacheAndUpload uploadWakeup error ! result = " + strA);
                }
                z = false;
            }
            return z;
        } catch (JSONException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private static boolean d(Context context, String str) {
        y.c("WakeupWordCacheAndUpload removeCache");
        try {
            if (b == null) {
                b = l.a(a(context, "locationData"), 1, 1, 10485760L);
            }
            return b.c(str);
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private static void e(Context context, String str) {
        y.c("WakeupWordCacheAndUpload removeParamsValue");
        if (TextUtils.isEmpty(str)) {
            return;
        }
        SharedPreferences.Editor editorEdit = context.getSharedPreferences(d, 0).edit();
        y.c("WakeupWordCacheAndUpload removeUploadKey : key = " + str);
        editorEdit.remove(str);
        editorEdit.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void f(Context context, String str) {
        e(context, str);
        d(context, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public bd g(Context context, String str) {
        y.c("WakeupWordCacheAndUpload getWakeupwordParam");
        bd bdVar = new bd();
        String string = context.getSharedPreferences(d, 0).getString(str, "");
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        String[] strArrSplit = string.split(MqttTopic.MULTI_LEVEL_WILDCARD);
        y.c("WakeupWordCacheAndUpload getWakeupwordParam length = " + strArrSplit.length);
        for (String str2 : strArrSplit) {
            y.c("WakeupWordCacheAndUpload getWakeupwordParam value = " + str2);
        }
        bdVar.a(strArrSplit[0]);
        bdVar.b(strArrSplit[1]);
        bdVar.c(strArrSplit[2]);
        bdVar.e(strArrSplit[3].split("_")[0]);
        bdVar.a(Float.parseFloat(strArrSplit[3].split("_")[1]));
        bdVar.f(strArrSplit[4]);
        bdVar.d(strArrSplit[5]);
        return bdVar;
    }

    public void a(Context context) {
        new bc(this, context).start();
    }

    public void a(Context context, bd bdVar, BlockingQueue<byte[]> blockingQueue) {
        new bb(this, blockingQueue, context, bdVar).start();
    }
}
