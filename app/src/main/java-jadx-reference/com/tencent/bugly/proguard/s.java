package com.tencent.bugly.proguard;

import android.content.Context;
import android.os.Process;
import android.os.SystemClock;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public final class s {
    private static s b;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public Map<String, String> f201a = null;
    private Context c;

    private s(Context context) {
        this.c = context;
    }

    public static s a(Context context) {
        if (b == null) {
            b = new s(context);
        }
        return b;
    }

    /* JADX WARN: Removed duplicated region for block: B:74:0x0177 A[Catch: all -> 0x0189, TRY_LEAVE, TryCatch #5 {all -> 0x0189, blocks: (B:22:0x009e, B:24:0x00a6, B:27:0x00b6, B:30:0x00c1, B:47:0x00e3, B:49:0x00eb, B:58:0x0116, B:60:0x012f, B:63:0x0151, B:72:0x0171, B:74:0x0177), top: B:107:0x009e }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final byte[] a(String str, byte[] bArr, v vVar, Map<String, String> map) {
        int i;
        int i2;
        int responseCode;
        if (str == null) {
            x.e("Failed for no URL.", new Object[0]);
            return null;
        }
        int i3 = 0;
        int i4 = 0;
        long length = bArr == null ? 0L : bArr.length;
        x.c("request: %s, send: %d (pid=%d | tid=%d)", str, Long.valueOf(length), Integer.valueOf(Process.myPid()), Integer.valueOf(Process.myTid()));
        boolean z = false;
        String str2 = str;
        while (i3 <= 0 && i4 <= 0) {
            if (z) {
                z = false;
            } else {
                i3++;
                if (i3 > 1) {
                    x.c("try time: " + i3, new Object[0]);
                    SystemClock.sleep(((long) new Random(System.currentTimeMillis()).nextInt(10000)) + 10000);
                }
            }
            String strF = com.tencent.bugly.crashreport.common.info.b.f(this.c);
            if (strF == null) {
                x.d("Failed to request for network not avail", new Object[0]);
            } else {
                vVar.a(length);
                HttpURLConnection httpURLConnectionA = a(str2, bArr, strF, map);
                if (httpURLConnectionA != null) {
                    try {
                        try {
                            responseCode = httpURLConnectionA.getResponseCode();
                        } finally {
                            try {
                                httpURLConnectionA.disconnect();
                            } catch (Throwable th) {
                                if (!x.a(th)) {
                                    th.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e = e;
                        int i5 = i4;
                        i = i3;
                        i2 = i5;
                    }
                    if (responseCode == 200) {
                        this.f201a = a(httpURLConnectionA);
                        byte[] bArrB = b(httpURLConnectionA);
                        vVar.b(bArrB == null ? 0L : bArrB.length);
                        return bArrB;
                    }
                    if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) {
                        try {
                            String headerField = httpURLConnectionA.getHeaderField("Location");
                            if (headerField == null) {
                                x.e("Failed to redirect: %d" + responseCode, new Object[0]);
                                try {
                                    httpURLConnectionA.disconnect();
                                } catch (Throwable th2) {
                                    if (!x.a(th2)) {
                                        th2.printStackTrace();
                                    }
                                }
                                return null;
                            }
                            int i6 = i4 + 1;
                            try {
                                x.c("redirect code: %d ,to:%s", Integer.valueOf(responseCode), headerField);
                                z = true;
                                str2 = headerField;
                                i2 = i6;
                                i = 0;
                            } catch (IOException e2) {
                                str2 = headerField;
                                e = e2;
                                z = true;
                                i2 = i6;
                                i = 0;
                            }
                        } catch (IOException e3) {
                            z = true;
                            e = e3;
                            int i7 = i4;
                            i = i3;
                            i2 = i7;
                        }
                        if (!x.a(e)) {
                            e.printStackTrace();
                        }
                        try {
                            httpURLConnectionA.disconnect();
                        } catch (Throwable th3) {
                            if (!x.a(th3)) {
                                th3.printStackTrace();
                            }
                        }
                    } else {
                        int i8 = i4;
                        i = i3;
                        i2 = i8;
                    }
                    try {
                        x.d("response code " + responseCode, new Object[0]);
                        long contentLength = httpURLConnectionA.getContentLength();
                        if (contentLength < 0) {
                            contentLength = 0;
                        }
                        vVar.b(contentLength);
                        try {
                            httpURLConnectionA.disconnect();
                        } catch (Throwable th4) {
                            if (!x.a(th4)) {
                                th4.printStackTrace();
                            }
                        }
                    } catch (IOException e4) {
                        e = e4;
                        if (!x.a(e)) {
                        }
                        httpURLConnectionA.disconnect();
                    }
                } else {
                    x.c("Failed to execute post.", new Object[0]);
                    vVar.b(0L);
                    int i9 = i4;
                    i = i3;
                    i2 = i9;
                }
                int i10 = i2;
                i3 = i;
                i4 = i10;
            }
        }
        return null;
    }

    private static Map<String, String> a(HttpURLConnection httpURLConnection) {
        HashMap map = new HashMap();
        Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
        if (headerFields == null || headerFields.size() == 0) {
            return null;
        }
        for (String str : headerFields.keySet()) {
            List<String> list = headerFields.get(str);
            if (list.size() > 0) {
                map.put(str, list.get(0));
            }
        }
        return map;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [java.io.BufferedInputStream] */
    /* JADX WARN: Type inference failed for: r0v10 */
    /* JADX WARN: Type inference failed for: r0v6, types: [byte[]] */
    /* JADX WARN: Type inference failed for: r0v8 */
    /* JADX WARN: Type inference failed for: r0v9 */
    private static byte[] b(HttpURLConnection httpURLConnection) throws Throwable {
        BufferedInputStream bufferedInputStream;
        byte[] byteArray = 0;
        byteArray = 0;
        byteArray = 0;
        byteArray = 0;
        try {
            if (httpURLConnection != null) {
                try {
                    bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bArr = new byte[1024];
                        while (true) {
                            int i = bufferedInputStream.read(bArr);
                            if (i <= 0) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, i);
                        }
                        byteArrayOutputStream.flush();
                        byteArray = byteArrayOutputStream.toByteArray();
                        try {
                            bufferedInputStream.close();
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (!x.a(th)) {
                            th.printStackTrace();
                        }
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (Throwable th3) {
                                th3.printStackTrace();
                            }
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    bufferedInputStream = null;
                }
            }
            return byteArray;
        } catch (Throwable th5) {
            th = th5;
        }
    }

    private HttpURLConnection a(String str, byte[] bArr, String str2, Map<String, String> map) {
        if (str == null) {
            x.e("destUrl is null.", new Object[0]);
            return null;
        }
        HttpURLConnection httpURLConnectionA = a(str2, str);
        if (httpURLConnectionA == null) {
            x.e("Failed to get HttpURLConnection object.", new Object[0]);
            return null;
        }
        try {
            httpURLConnectionA.setRequestProperty("wup_version", "3.0");
            if (map != null && map.size() > 0) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    httpURLConnectionA.setRequestProperty(entry.getKey(), URLEncoder.encode(entry.getValue(), com.unisound.b.f.b));
                }
            }
            httpURLConnectionA.setRequestProperty("A37", URLEncoder.encode(str2, com.unisound.b.f.b));
            httpURLConnectionA.setRequestProperty("A38", URLEncoder.encode(str2, com.unisound.b.f.b));
            OutputStream outputStream = httpURLConnectionA.getOutputStream();
            if (bArr == null) {
                outputStream.write(0);
            } else {
                outputStream.write(bArr);
            }
            return httpURLConnectionA;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            x.e("Failed to upload, please check your network.", new Object[0]);
            return null;
        }
    }

    private static HttpURLConnection a(String str, String str2) {
        HttpURLConnection httpURLConnection;
        try {
            URL url = new URL(str2);
            if (str != null && str.toLowerCase(Locale.US).contains("wap")) {
                httpURLConnection = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")))));
            } else {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            }
            httpURLConnection.setConnectTimeout(30000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(false);
            return httpURLConnection;
        } catch (Throwable th) {
            if (!x.a(th)) {
                th.printStackTrace();
            }
            return null;
        }
    }
}
