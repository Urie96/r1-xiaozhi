package com.baidu.location.d;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* JADX INFO: loaded from: classes.dex */
class f extends Thread {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    final /* synthetic */ e f108a;

    f(e eVar) {
        this.f108a = eVar;
    }

    /* JADX WARN: Removed duplicated region for block: B:105:? A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:51:0x0102  */
    /* JADX WARN: Removed duplicated region for block: B:79:0x0107 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:83:0x010c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @Override // java.lang.Thread, java.lang.Runnable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void run() throws Throwable {
        InputStream inputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        HttpURLConnection httpURLConnection;
        InputStream inputStream2;
        boolean z;
        boolean z2;
        ByteArrayOutputStream byteArrayOutputStream2;
        InputStream inputStream3;
        ByteArrayOutputStream byteArrayOutputStream3;
        ByteArrayOutputStream byteArrayOutputStream4 = null;
        this.f108a.h = j.c();
        this.f108a.b();
        this.f108a.a();
        HttpURLConnection httpURLConnection2 = null;
        int i = this.f108a.i;
        while (i > 0) {
            try {
                HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(this.f108a.h).openConnection();
                try {
                    httpURLConnection3.setRequestMethod("GET");
                    httpURLConnection3.setDoInput(true);
                    httpURLConnection3.setDoOutput(true);
                    httpURLConnection3.setUseCaches(false);
                    httpURLConnection3.setConnectTimeout(a.b);
                    httpURLConnection3.setReadTimeout(a.b);
                    httpURLConnection3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                    httpURLConnection3.setRequestProperty("Accept-Charset", "UTF-8");
                    if (httpURLConnection3.getResponseCode() == 200) {
                        inputStream = httpURLConnection3.getInputStream();
                        try {
                            byteArrayOutputStream3 = new ByteArrayOutputStream();
                        } catch (Exception e) {
                            inputStream2 = inputStream;
                            httpURLConnection = httpURLConnection3;
                            byteArrayOutputStream = null;
                        } catch (Throwable th) {
                            httpURLConnection2 = httpURLConnection3;
                            th = th;
                        }
                        try {
                            byte[] bArr = new byte[1024];
                            while (true) {
                                int i2 = inputStream.read(bArr);
                                if (i2 == -1) {
                                    break;
                                } else {
                                    byteArrayOutputStream3.write(bArr, 0, i2);
                                }
                            }
                            inputStream.close();
                            byteArrayOutputStream3.close();
                            this.f108a.j = new String(byteArrayOutputStream3.toByteArray(), com.unisound.b.f.b);
                            this.f108a.a(true);
                            httpURLConnection3.disconnect();
                            inputStream3 = inputStream;
                            byteArrayOutputStream2 = byteArrayOutputStream3;
                            z2 = true;
                        } catch (Exception e2) {
                            inputStream2 = inputStream;
                            httpURLConnection = httpURLConnection3;
                            byteArrayOutputStream = byteArrayOutputStream3;
                            try {
                                Log.d(a.f101a, "NetworkCommunicationException!");
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                if (inputStream2 != null) {
                                    try {
                                        inputStream2.close();
                                    } catch (Exception e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                if (byteArrayOutputStream != null) {
                                    try {
                                        byteArrayOutputStream.close();
                                        z = false;
                                        httpURLConnection2 = httpURLConnection;
                                    } catch (Exception e4) {
                                        e4.printStackTrace();
                                        z = false;
                                        httpURLConnection2 = httpURLConnection;
                                    }
                                } else {
                                    z = false;
                                    httpURLConnection2 = httpURLConnection;
                                }
                            } catch (Throwable th2) {
                                byteArrayOutputStream4 = byteArrayOutputStream;
                                th = th2;
                                InputStream inputStream4 = inputStream2;
                                httpURLConnection2 = httpURLConnection;
                                inputStream = inputStream4;
                                if (httpURLConnection2 != null) {
                                    httpURLConnection2.disconnect();
                                }
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Exception e5) {
                                        e5.printStackTrace();
                                    }
                                }
                                if (byteArrayOutputStream4 != null) {
                                    throw th;
                                }
                                try {
                                    byteArrayOutputStream4.close();
                                    throw th;
                                } catch (Exception e6) {
                                    e6.printStackTrace();
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            byteArrayOutputStream4 = byteArrayOutputStream3;
                            httpURLConnection2 = httpURLConnection3;
                            th = th3;
                            if (httpURLConnection2 != null) {
                            }
                            if (inputStream != null) {
                            }
                            if (byteArrayOutputStream4 != null) {
                            }
                        }
                    } else {
                        httpURLConnection3.disconnect();
                        z2 = false;
                        byteArrayOutputStream2 = null;
                        inputStream3 = null;
                    }
                    if (httpURLConnection3 != null) {
                        httpURLConnection3.disconnect();
                    }
                    if (inputStream3 != null) {
                        try {
                            inputStream3.close();
                        } catch (Exception e7) {
                            e7.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream2 != null) {
                        try {
                            byteArrayOutputStream2.close();
                            boolean z3 = z2;
                            httpURLConnection2 = httpURLConnection3;
                            z = z3;
                        } catch (Exception e8) {
                            e8.printStackTrace();
                            boolean z4 = z2;
                            httpURLConnection2 = httpURLConnection3;
                            z = z4;
                        }
                    } else {
                        boolean z5 = z2;
                        httpURLConnection2 = httpURLConnection3;
                        z = z5;
                    }
                } catch (Exception e9) {
                    inputStream2 = null;
                    httpURLConnection = httpURLConnection3;
                    byteArrayOutputStream = null;
                } catch (Throwable th4) {
                    inputStream = null;
                    httpURLConnection2 = httpURLConnection3;
                    th = th4;
                }
            } catch (Exception e10) {
                byteArrayOutputStream = null;
                httpURLConnection = httpURLConnection2;
                inputStream2 = null;
            } catch (Throwable th5) {
                th = th5;
                inputStream = null;
            }
            if (z) {
                break;
            } else {
                i--;
            }
        }
        if (i > 0) {
            e.o = 0;
            return;
        }
        e.o++;
        this.f108a.j = null;
        this.f108a.a(false);
    }
}
