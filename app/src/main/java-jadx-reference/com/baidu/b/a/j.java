package com.baidu.b.a;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

/* JADX INFO: loaded from: classes.dex */
public class j {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private Context f45a;
    private String b = null;
    private HashMap<String, String> c = null;
    private String d = null;

    public j(Context context) {
        this.f45a = context;
    }

    private String a(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager == null) {
                return null;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isAvailable()) {
                return null;
            }
            String extraInfo = activeNetworkInfo.getExtraInfo();
            return (extraInfo == null || !(extraInfo.trim().toLowerCase().equals("cmwap") || extraInfo.trim().toLowerCase().equals("uniwap") || extraInfo.trim().toLowerCase().equals("3gwap") || extraInfo.trim().toLowerCase().equals("ctwap"))) ? "wifi" : extraInfo.trim().toLowerCase().equals("ctwap") ? "ctwap" : "cmwap";
        } catch (Exception e) {
            if (d.f40a) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:107:0x0207  */
    /* JADX WARN: Removed duplicated region for block: B:108:0x0216  */
    /* JADX WARN: Removed duplicated region for block: B:145:0x0275 A[PHI: r3
      0x0275: PHI (r3v6 int) = (r3v3 int), (r3v4 int), (r3v7 int) binds: [B:88:0x01df, B:74:0x01a3, B:56:0x015d] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:149:0x01f6 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:162:0x00d3 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x0131 A[Catch: MalformedURLException -> 0x0135, all -> 0x0230, Exception -> 0x023c, IOException -> 0x0247, TryCatch #13 {all -> 0x0230, blocks: (B:8:0x0031, B:38:0x0115, B:84:0x01b9, B:86:0x01bd, B:87:0x01c0, B:70:0x017d, B:72:0x0181, B:73:0x0184, B:40:0x011d, B:26:0x00c5, B:28:0x00cd, B:46:0x0129, B:48:0x0131, B:49:0x0134), top: B:157:0x002d }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void a(HttpsURLConnection httpsURLConnection) throws Throwable {
        OutputStream outputStream;
        int i;
        OutputStream outputStream2;
        InputStream inputStream;
        int responseCode;
        BufferedReader bufferedReader;
        StringBuffer stringBuffer;
        OutputStream outputStream3 = null;
        bufferedReader = null;
        bufferedReader = null;
        bufferedReader = null;
        bufferedReader = null;
        bufferedReader = null;
        BufferedReader bufferedReader2 = null;
        d.a("https Post start,url:" + this.b);
        if (this.c == null) {
            this.d = a.a("httpsPost request paramters is null.");
            return;
        }
        boolean z = true;
        try {
            try {
                outputStream2 = httpsURLConnection.getOutputStream();
                try {
                    try {
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
                        bufferedWriter.write(b(this.c));
                        d.a(b(this.c));
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        httpsURLConnection.connect();
                        try {
                            inputStream = httpsURLConnection.getInputStream();
                            try {
                                responseCode = httpsURLConnection.getResponseCode();
                            } catch (IOException e) {
                                e = e;
                                i = -1;
                            } catch (Throwable th) {
                                th = th;
                            }
                        } catch (IOException e2) {
                            e = e2;
                            inputStream = null;
                            i = -1;
                        } catch (Throwable th2) {
                            th = th2;
                            inputStream = null;
                        }
                    } catch (MalformedURLException e3) {
                        e = e3;
                        i = -1;
                        outputStream3 = outputStream2;
                    } catch (IOException e4) {
                        e = e4;
                        i = -1;
                    } catch (Exception e5) {
                        e = e5;
                        i = -1;
                    }
                } catch (MalformedURLException e6) {
                    e = e6;
                    outputStream3 = outputStream2;
                } catch (IOException e7) {
                    e = e7;
                } catch (Exception e8) {
                    e = e8;
                }
            } catch (Throwable th3) {
                th = th3;
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e9) {
                        if (d.f40a) {
                            e9.printStackTrace();
                        }
                    }
                }
                throw th;
            }
        } catch (MalformedURLException e10) {
            e = e10;
            i = -1;
        } catch (IOException e11) {
            e = e11;
            i = -1;
            outputStream2 = null;
        } catch (Exception e12) {
            e = e12;
            i = -1;
            outputStream2 = null;
        } catch (Throwable th4) {
            th = th4;
            outputStream = null;
            if (outputStream != null) {
            }
            throw th;
        }
        if (200 == responseCode) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                try {
                    stringBuffer = new StringBuffer();
                } catch (IOException e13) {
                    e = e13;
                    bufferedReader2 = bufferedReader;
                    i = responseCode;
                    try {
                        if (d.f40a) {
                            e.printStackTrace();
                            d.a("httpsPost parse failed;" + e.getMessage());
                        }
                        this.d = a.a(-11, "httpsPost failed,IOException:" + e.getMessage());
                        if (inputStream != null && bufferedReader2 != null) {
                            bufferedReader2.close();
                            inputStream.close();
                        }
                        if (httpsURLConnection != null) {
                            httpsURLConnection.disconnect();
                            z = false;
                        } else {
                            z = false;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        if (inputStream != null && bufferedReader2 != null) {
                            bufferedReader2.close();
                            inputStream.close();
                        }
                        if (httpsURLConnection != null) {
                            httpsURLConnection.disconnect();
                        }
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    bufferedReader2 = bufferedReader;
                    if (inputStream != null) {
                        bufferedReader2.close();
                        inputStream.close();
                    }
                    if (httpsURLConnection != null) {
                    }
                    throw th;
                }
            } catch (IOException e14) {
                e = e14;
                i = responseCode;
            } catch (Throwable th7) {
                th = th7;
            }
            while (true) {
                int i2 = bufferedReader.read();
                if (i2 == -1) {
                    break;
                } else {
                    stringBuffer.append((char) i2);
                }
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                    } catch (IOException e15) {
                        if (d.f40a) {
                            e15.printStackTrace();
                        }
                    }
                }
                if (!z && 200 != i) {
                    d.a("httpsPost failed,statusCode:" + i);
                    this.d = a.a(-11, "httpsPost failed,statusCode:" + i);
                    return;
                } else if (this.d == null) {
                    d.a("httpsPost success end,parse result = " + this.d);
                    return;
                } else {
                    d.a("httpsPost failed,mResult is null");
                    this.d = a.a(-1, "httpsPost failed,internal error");
                    return;
                }
            }
            this.d = stringBuffer.toString();
        } else {
            bufferedReader = null;
        }
        if (inputStream != null && bufferedReader != null) {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (MalformedURLException e16) {
                e = e16;
                i = responseCode;
                outputStream3 = outputStream2;
                try {
                    if (d.f40a) {
                        e.printStackTrace();
                    }
                    this.d = a.a(-11, "httpsPost failed,MalformedURLException:" + e.getMessage());
                    if (outputStream3 != null) {
                        try {
                            outputStream3.close();
                            z = false;
                        } catch (IOException e17) {
                            if (d.f40a) {
                                e17.printStackTrace();
                            }
                            z = false;
                        }
                    } else {
                        z = false;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    outputStream = outputStream3;
                    if (outputStream != null) {
                    }
                    throw th;
                }
            } catch (IOException e18) {
                e = e18;
                i = responseCode;
                if (d.f40a) {
                    e.printStackTrace();
                }
                this.d = a.a(-11, "httpsPost failed,IOException:" + e.getMessage());
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                        z = false;
                    } catch (IOException e19) {
                        if (d.f40a) {
                            e19.printStackTrace();
                        }
                        z = false;
                    }
                }
            } catch (Exception e20) {
                e = e20;
                i = responseCode;
                if (d.f40a) {
                    e.printStackTrace();
                }
                this.d = a.a(-11, "httpsPost failed,Exception:" + e.getMessage());
                if (outputStream2 != null) {
                    try {
                        outputStream2.close();
                        z = false;
                    } catch (IOException e21) {
                        if (d.f40a) {
                            e21.printStackTrace();
                        }
                        z = false;
                    }
                }
            }
        }
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
            i = responseCode;
        } else {
            i = responseCode;
        }
        if (outputStream2 != null) {
        }
        if (!z) {
        }
        if (this.d == null) {
        }
    }

    private static String b(HashMap<String, String> map) {
        boolean z;
        StringBuilder sb = new StringBuilder();
        boolean z2 = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (z2) {
                z = false;
            } else {
                sb.append("&");
                z = z2;
            }
            sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            z2 = z;
        }
        return sb.toString();
    }

    private HttpsURLConnection b() {
        try {
            URL url = new URL(this.b);
            d.a("https URL: " + this.b);
            String strA = a(this.f45a);
            if (strA == null || strA.equals("")) {
                d.c("Current network is not available.");
                this.d = a.a(-10, "Current network is not available.");
                return null;
            }
            d.a("checkNetwork = " + strA);
            HttpsURLConnection httpsURLConnection = strA.equals("cmwap") ? (HttpsURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.172", 80))) : strA.equals("ctwap") ? (HttpsURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80))) : (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(50000);
            httpsURLConnection.setReadTimeout(50000);
            return httpsURLConnection;
        } catch (MalformedURLException e) {
            if (d.f40a) {
                e.printStackTrace();
                d.a(e.getMessage());
            }
            this.d = a.a(-11, "Auth server could not be parsed as a URL.");
            return null;
        } catch (Exception e2) {
            if (d.f40a) {
                e2.printStackTrace();
                d.a(e2.getMessage());
            }
            this.d = a.a(-11, "Init httpsurlconnection failed.");
            return null;
        }
    }

    private HashMap<String, String> c(HashMap<String, String> map) {
        HashMap<String, String> map2 = new HashMap<>();
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String string = it.next().toString();
            map2.put(string, map.get(string));
        }
        return map2;
    }

    protected String a(HashMap<String, String> map) throws Throwable {
        this.c = c(map);
        this.b = this.c.get("url");
        HttpsURLConnection httpsURLConnectionB = b();
        if (httpsURLConnectionB == null) {
            d.c("syncConnect failed,httpsURLConnection is null");
            return this.d;
        }
        a(httpsURLConnectionB);
        return this.d;
    }

    protected boolean a() {
        d.a("checkNetwork start");
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.f45a.getSystemService("connectivity");
            if (connectivityManager == null) {
                return false;
            }
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isAvailable()) {
                    d.a("checkNetwork end");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            if (d.f40a) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
