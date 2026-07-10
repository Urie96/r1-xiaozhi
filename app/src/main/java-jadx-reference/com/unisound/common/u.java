package com.unisound.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/* JADX INFO: loaded from: classes.dex */
public class u {
    public static String a(String str) {
        return a(str, 30000);
    }

    /* JADX WARN: Unreachable blocks removed: 2, instructions: 4 */
    public static String a(String str, int i) throws Throwable {
        BufferedReader bufferedReader;
        InputStream inputStream;
        String str2;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream2 = null;
        httpURLConnection = null;
        httpURLConnection = null;
        httpURLConnection = null;
        try {
            try {
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
                try {
                    httpURLConnection2.setConnectTimeout(i);
                    httpURLConnection2.setRequestMethod("GET");
                    httpURLConnection2.connect();
                    if (httpURLConnection2.getResponseCode() == 200) {
                        inputStream = httpURLConnection2.getInputStream();
                        try {
                            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, com.unisound.b.f.b));
                            str2 = "";
                            while (true) {
                                try {
                                    String line = bufferedReader.readLine();
                                    if (line == null) {
                                        break;
                                    }
                                    str2 = str2 + line;
                                } catch (MalformedURLException e) {
                                    httpURLConnection = httpURLConnection2;
                                    e = e;
                                    e.printStackTrace();
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e2) {
                                            e2.printStackTrace();
                                            return str2;
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (httpURLConnection != null) {
                                        httpURLConnection.disconnect();
                                    }
                                } catch (IOException e3) {
                                    httpURLConnection = httpURLConnection2;
                                    e = e3;
                                    e.printStackTrace();
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e4) {
                                            e4.printStackTrace();
                                            return str2;
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (httpURLConnection != null) {
                                        httpURLConnection.disconnect();
                                    }
                                } catch (Throwable th) {
                                    httpURLConnection = httpURLConnection2;
                                    th = th;
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e5) {
                                            e5.printStackTrace();
                                            throw th;
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (httpURLConnection != null) {
                                        httpURLConnection.disconnect();
                                    }
                                    throw th;
                                }
                            }
                            inputStream2 = inputStream;
                        } catch (MalformedURLException e6) {
                            bufferedReader = null;
                            httpURLConnection = httpURLConnection2;
                            e = e6;
                            str2 = "";
                        } catch (IOException e7) {
                            bufferedReader = null;
                            httpURLConnection = httpURLConnection2;
                            e = e7;
                            str2 = "";
                        } catch (Throwable th2) {
                            bufferedReader = null;
                            httpURLConnection = httpURLConnection2;
                            th = th2;
                        }
                    } else {
                        str2 = "{}";
                        bufferedReader = null;
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e8) {
                            e8.printStackTrace();
                        }
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                } catch (MalformedURLException e9) {
                    bufferedReader = null;
                    inputStream = null;
                    httpURLConnection = httpURLConnection2;
                    e = e9;
                    str2 = "";
                } catch (IOException e10) {
                    bufferedReader = null;
                    inputStream = null;
                    httpURLConnection = httpURLConnection2;
                    e = e10;
                    str2 = "";
                } catch (Throwable th3) {
                    bufferedReader = null;
                    inputStream = null;
                    httpURLConnection = httpURLConnection2;
                    th = th3;
                }
            } catch (Throwable th4) {
                th = th4;
            }
        } catch (MalformedURLException e11) {
            e = e11;
            str2 = "";
            bufferedReader = null;
            inputStream = null;
        } catch (IOException e12) {
            e = e12;
            str2 = "";
            bufferedReader = null;
            inputStream = null;
        } catch (Throwable th5) {
            th = th5;
            bufferedReader = null;
            inputStream = null;
        }
        return str2;
    }

    public static String a(String str, String str2) {
        return a(str, str2.getBytes());
    }

    public static String a(String str, byte[] bArr) {
        return a(str, bArr, 30000);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1 */
    /* JADX WARN: Type inference failed for: r1v2, types: [java.net.HttpURLConnection] */
    /* JADX WARN: Type inference failed for: r1v28 */
    /* JADX WARN: Type inference failed for: r1v29 */
    /* JADX WARN: Type inference failed for: r1v30 */
    /* JADX WARN: Type inference failed for: r1v31 */
    /* JADX WARN: Type inference failed for: r1v4 */
    /* JADX WARN: Type inference failed for: r1v6 */
    public static String a(String str, byte[] bArr, int i) throws Throwable {
        String str2;
        HttpURLConnection httpURLConnection;
        String str3;
        ?? r1 = 0;
        HttpURLConnection httpURLConnection2 = null;
        try {
            try {
                httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            } catch (Throwable th) {
                th = th;
            }
        } catch (Exception e) {
            e = e;
        }
        try {
            httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(i);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(bArr);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e2) {
            httpURLConnection2 = httpURLConnection;
            e = e2;
            e.printStackTrace();
            String str4 = "Error";
            str2 = str4;
            r1 = httpURLConnection2;
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
                str2 = str4;
                r1 = httpURLConnection2;
            }
        } catch (Throwable th2) {
            r1 = httpURLConnection;
            th = th2;
            if (r1 != 0) {
                r1.disconnect();
            }
            throw th;
        }
        if (httpURLConnection.getResponseCode() == 200) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), com.unisound.b.f.b));
            String str5 = "";
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                str5 = str5 + line;
                return str2;
            }
            bufferedReader.close();
            str3 = str5;
        } else {
            str3 = "{}";
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
            str2 = str3;
            r1 = str3;
        } else {
            str2 = str3;
            r1 = str3;
        }
        return str2;
    }

    public static int b(String str, String str2) {
        return b(str, str2.getBytes(), 30000);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:23:0x005c A[PHI: r1 r2
      0x005c: PHI (r1v2 int) = (r1v0 int), (r1v6 int) binds: [B:13:0x0045, B:8:0x003a] A[DONT_GENERATE, DONT_INLINE]
      0x005c: PHI (r2v4 ??) = (r2v3 ??), (r2v14 ??) binds: [B:13:0x0045, B:8:0x003a] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Type inference failed for: r2v0 */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v14 */
    /* JADX WARN: Type inference failed for: r2v15 */
    /* JADX WARN: Type inference failed for: r2v16 */
    /* JADX WARN: Type inference failed for: r2v17 */
    /* JADX WARN: Type inference failed for: r2v18 */
    /* JADX WARN: Type inference failed for: r2v2, types: [java.net.HttpURLConnection] */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.net.HttpURLConnection] */
    /* JADX WARN: Type inference failed for: r2v4 */
    /* JADX WARN: Type inference failed for: r2v5 */
    /* JADX WARN: Type inference failed for: r2v6 */
    /* JADX WARN: Type inference failed for: r2v8 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static int b(String str, byte[] bArr, int i) throws Throwable {
        int i2;
        HttpURLConnection httpURLConnection;
        ?? r2 = 0;
        ?? r22 = 0;
        int responseCode = -1;
        try {
            try {
                httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            } catch (Throwable th) {
                th = th;
            }
        } catch (Exception e) {
            e = e;
        }
        try {
            httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(i);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(bArr);
            outputStream.flush();
            outputStream.close();
            responseCode = httpURLConnection.getResponseCode();
            r22 = 200;
            r2 = 200;
            if (responseCode == 200) {
                responseCode = 0;
            }
        } catch (Exception e2) {
            r22 = httpURLConnection;
            e = e2;
            e.printStackTrace();
            if (r22 != 0) {
                r22.disconnect();
                i2 = -1;
                r2 = r22;
            }
        } catch (Throwable th2) {
            r2 = httpURLConnection;
            th = th2;
            if (r2 != 0) {
                r2.disconnect();
            }
            throw th;
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
            i2 = responseCode;
        } else {
            i2 = responseCode;
            r2 = r22;
        }
        return i2;
    }
}
