package com.unisound.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* JADX INFO: loaded from: classes.dex */
public class az {
    private static File a(String str) {
        File file = new File(str);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            y.c("VprHttpClient : ", "create filedir failure!");
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /* JADX WARN: Removed duplicated region for block: B:36:0x0081  */
    /* JADX WARN: Removed duplicated region for block: B:40:0x008b A[Catch: IOException -> 0x008f, TRY_LEAVE, TryCatch #6 {IOException -> 0x008f, blocks: (B:38:0x0086, B:40:0x008b), top: B:63:0x0086 }] */
    /* JADX WARN: Removed duplicated region for block: B:63:0x0086 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(String str, String str2) throws Throwable {
        InputStream inputStream;
        HttpURLConnection httpURLConnection;
        FileOutputStream fileOutputStream;
        FileOutputStream fileOutputStream2;
        byte[] bArrA;
        FileOutputStream fileOutputStream3 = null;
        fileOutputStream3 = null;
        inputStream = null;
        fileOutputStream3 = null;
        InputStream inputStream2 = null;
        try {
            File fileA = a(str2);
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection2.setConnectTimeout(30000);
                httpURLConnection2.setRequestMethod("GET");
                httpURLConnection2.connect();
                if (httpURLConnection2.getResponseCode() == 200) {
                    inputStream = httpURLConnection2.getInputStream();
                    try {
                        bArrA = a(inputStream);
                        fileOutputStream2 = new FileOutputStream(fileA);
                    } catch (Exception e) {
                        httpURLConnection = httpURLConnection2;
                        e = e;
                        fileOutputStream = null;
                        inputStream2 = inputStream;
                    } catch (Throwable th) {
                        httpURLConnection = httpURLConnection2;
                        th = th;
                    }
                    try {
                        fileOutputStream2.write(bArrA);
                        fileOutputStream2.flush();
                    } catch (Exception e2) {
                        inputStream2 = inputStream;
                        httpURLConnection = httpURLConnection2;
                        e = e2;
                        fileOutputStream = fileOutputStream2;
                        try {
                            y.c("VprHttpClient : ", "getVPRAudio wrong!");
                            e.printStackTrace();
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    return false;
                                }
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            inputStream = inputStream2;
                            fileOutputStream3 = fileOutputStream;
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            if (fileOutputStream3 != null) {
                                try {
                                    fileOutputStream3.close();
                                } catch (IOException e4) {
                                    e4.printStackTrace();
                                    throw th;
                                }
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        fileOutputStream3 = fileOutputStream2;
                        httpURLConnection = httpURLConnection2;
                        th = th3;
                        if (httpURLConnection != null) {
                        }
                        if (fileOutputStream3 != null) {
                        }
                        if (inputStream != null) {
                        }
                        throw th;
                    }
                } else {
                    fileOutputStream2 = null;
                    inputStream = null;
                }
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                return true;
            } catch (Exception e6) {
                httpURLConnection = httpURLConnection2;
                e = e6;
                fileOutputStream = null;
            } catch (Throwable th4) {
                inputStream = null;
                httpURLConnection = httpURLConnection2;
                th = th4;
            }
        } catch (Exception e7) {
            e = e7;
            fileOutputStream = null;
            httpURLConnection = null;
        } catch (Throwable th5) {
            th = th5;
            inputStream = null;
            httpURLConnection = null;
        }
    }

    public static byte[] a(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[1024];
        while (true) {
            int i = inputStream.read(bArr);
            if (i == -1) {
                inputStream.close();
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, i);
        }
    }
}
