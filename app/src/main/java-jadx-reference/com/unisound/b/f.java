package com.unisound.b;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class f {
    public static final String b = "utf-8";
    public static HashMap<String, String> c = new HashMap<>();
    private static final int d = 10000;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public String f221a = null;

    /* JADX WARN: Removed duplicated region for block: B:42:0x00d1 A[Catch: IOException -> 0x00da, TryCatch #1 {IOException -> 0x00da, blocks: (B:40:0x00cc, B:42:0x00d1, B:44:0x00d6), top: B:62:0x00cc }] */
    /* JADX WARN: Removed duplicated region for block: B:44:0x00d6 A[Catch: IOException -> 0x00da, TRY_LEAVE, TryCatch #1 {IOException -> 0x00da, blocks: (B:40:0x00cc, B:42:0x00d1, B:44:0x00d6), top: B:62:0x00cc }] */
    /* JADX WARN: Removed duplicated region for block: B:62:0x00cc A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static l a(String str, Map<String, String> map, int i) throws Throwable {
        InputStream inputStream;
        OutputStream outputStream;
        BufferedReader bufferedReader;
        OutputStream outputStream2;
        String str2;
        IOException iOException;
        BufferedReader bufferedReader2 = null;
        bufferedReader2 = null;
        inputStream = null;
        bufferedReader2 = null;
        InputStream inputStream2 = null;
        byte[] bytes = a(map).toString().getBytes();
        String str3 = "";
        l lVar = new l();
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setConnectTimeout(i);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            outputStream = httpURLConnection.getOutputStream();
            try {
                outputStream.write(bytes);
                int responseCode = httpURLConnection.getResponseCode();
                lVar.a(j.a(httpURLConnection.getHeaderField("Date")));
                lVar.b(System.currentTimeMillis());
                if (responseCode == 200) {
                    inputStream = httpURLConnection.getInputStream();
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        while (true) {
                            try {
                                String line = bufferedReader.readLine();
                                if (line == null) {
                                    break;
                                }
                                str3 = str3 + line;
                            } catch (IOException e) {
                                inputStream2 = inputStream;
                                outputStream2 = outputStream;
                                str2 = str3;
                                iOException = e;
                                try {
                                    iOException.printStackTrace();
                                    if (outputStream2 != null) {
                                        try {
                                            outputStream2.close();
                                        } catch (IOException e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (inputStream2 != null) {
                                        inputStream2.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    outputStream = outputStream2;
                                    inputStream = inputStream2;
                                    bufferedReader2 = bufferedReader;
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (IOException e3) {
                                            e3.printStackTrace();
                                            throw th;
                                        }
                                    }
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    if (bufferedReader2 != null) {
                                        bufferedReader2.close();
                                    }
                                    throw th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                bufferedReader2 = bufferedReader;
                                if (outputStream != null) {
                                }
                                if (inputStream != null) {
                                }
                                if (bufferedReader2 != null) {
                                }
                                throw th;
                            }
                        }
                        str2 = str3;
                    } catch (IOException e4) {
                        bufferedReader = null;
                        inputStream2 = inputStream;
                        outputStream2 = outputStream;
                        iOException = e4;
                        str2 = "";
                    } catch (Throwable th3) {
                        th = th3;
                    }
                } else {
                    str2 = "{}";
                    bufferedReader = null;
                    inputStream = null;
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e6) {
                bufferedReader = null;
                outputStream2 = outputStream;
                str2 = "";
                iOException = e6;
            } catch (Throwable th4) {
                th = th4;
                inputStream = null;
            }
        } catch (IOException e7) {
            bufferedReader = null;
            outputStream2 = null;
            str2 = "";
            iOException = e7;
        } catch (Throwable th5) {
            th = th5;
            inputStream = null;
            outputStream = null;
        }
        lVar.a(str2);
        return lVar;
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x0077 A[Catch: Exception -> 0x00bd, TRY_LEAVE, TryCatch #8 {Exception -> 0x00bd, blocks: (B:16:0x0072, B:18:0x0077), top: B:74:0x0072 }] */
    /* JADX WARN: Removed duplicated region for block: B:20:0x007c  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00af A[Catch: Exception -> 0x00b8, TRY_LEAVE, TryCatch #3 {Exception -> 0x00b8, blocks: (B:32:0x00aa, B:34:0x00af), top: B:68:0x00aa }] */
    /* JADX WARN: Removed duplicated region for block: B:36:0x00b4  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x00cc A[Catch: Exception -> 0x00d5, TRY_LEAVE, TryCatch #5 {Exception -> 0x00d5, blocks: (B:44:0x00c7, B:46:0x00cc), top: B:70:0x00c7 }] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00d1  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x00aa A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:70:0x00c7 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:74:0x0072 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:87:? A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String a(String str, String str2, String str3) throws Throwable {
        PrintWriter printWriter;
        HttpURLConnection httpURLConnection;
        BufferedReader bufferedReader;
        HttpURLConnection httpURLConnection2;
        BufferedReader bufferedReader2;
        BufferedReader bufferedReader3 = null;
        bufferedReader3 = null;
        printWriter = null;
        printWriter = null;
        bufferedReader3 = null;
        PrintWriter printWriter2 = null;
        StringBuilder sb = new StringBuilder();
        try {
            httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection2.setRequestProperty("accept", "*/*");
                httpURLConnection2.setRequestProperty("connection", "Keep-Alive");
                httpURLConnection2.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                httpURLConnection2.setConnectTimeout(10000);
                httpURLConnection2.setRequestMethod(str2);
                httpURLConnection2.setDoOutput(true);
                httpURLConnection2.setDoInput(true);
            } catch (Exception e) {
                httpURLConnection = httpURLConnection2;
                e = e;
                bufferedReader = null;
            } catch (Throwable th) {
                printWriter = null;
                httpURLConnection = httpURLConnection2;
                th = th;
            }
        } catch (Exception e2) {
            e = e2;
            bufferedReader = null;
            httpURLConnection = null;
        } catch (Throwable th2) {
            th = th2;
            printWriter = null;
            httpURLConnection = null;
        }
        if (!str2.equals("POST")) {
            httpURLConnection2.connect();
            bufferedReader2 = new BufferedReader(new InputStreamReader(httpURLConnection2.getInputStream(), "UTF-8"));
            while (true) {
                try {
                    String line = bufferedReader2.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                } catch (Exception e3) {
                    bufferedReader = bufferedReader2;
                    httpURLConnection = httpURLConnection2;
                    e = e3;
                    e.printStackTrace();
                    if (printWriter2 != null) {
                    }
                    if (bufferedReader != null) {
                    }
                    if (httpURLConnection != null) {
                    }
                } catch (Throwable th3) {
                    printWriter = null;
                    bufferedReader3 = bufferedReader2;
                    httpURLConnection = httpURLConnection2;
                    th = th3;
                    if (printWriter != null) {
                    }
                    if (bufferedReader3 != null) {
                    }
                    if (httpURLConnection != null) {
                    }
                }
            }
            printWriter = null;
            if (printWriter != null) {
            }
            if (bufferedReader2 != null) {
            }
            if (httpURLConnection2 != null) {
            }
            return sb.toString();
        }
        printWriter = new PrintWriter(httpURLConnection2.getOutputStream());
        try {
            printWriter.print(str3);
            printWriter.flush();
            bufferedReader2 = new BufferedReader(new InputStreamReader(httpURLConnection2.getInputStream(), "UTF-8"));
            while (true) {
                try {
                    String line2 = bufferedReader2.readLine();
                    if (line2 == null) {
                        break;
                    }
                    sb.append(line2);
                } catch (Exception e4) {
                    printWriter2 = printWriter;
                    httpURLConnection = httpURLConnection2;
                    e = e4;
                    bufferedReader = bufferedReader2;
                    try {
                        e.printStackTrace();
                        if (printWriter2 != null) {
                            try {
                                printWriter2.close();
                            } catch (Exception e5) {
                                e5.printStackTrace();
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                return sb.toString();
                            }
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (httpURLConnection != null) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        printWriter = printWriter2;
                        bufferedReader3 = bufferedReader;
                        if (printWriter != null) {
                            try {
                                printWriter.close();
                            } catch (Exception e6) {
                                e6.printStackTrace();
                                if (httpURLConnection != null) {
                                    throw th;
                                }
                                httpURLConnection.disconnect();
                                throw th;
                            }
                        }
                        if (bufferedReader3 != null) {
                            bufferedReader3.close();
                        }
                        if (httpURLConnection != null) {
                        }
                    }
                } catch (Throwable th5) {
                    bufferedReader3 = bufferedReader2;
                    httpURLConnection = httpURLConnection2;
                    th = th5;
                    if (printWriter != null) {
                    }
                    if (bufferedReader3 != null) {
                    }
                    if (httpURLConnection != null) {
                    }
                }
            }
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Exception e7) {
                    e7.printStackTrace();
                }
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
        } catch (Exception e8) {
            httpURLConnection = httpURLConnection2;
            e = e8;
            bufferedReader = null;
            printWriter2 = printWriter;
        } catch (Throwable th6) {
            httpURLConnection = httpURLConnection2;
            th = th6;
        }
        return sb.toString();
    }

    private static String a(Map<String, String> map) {
        String strEncode;
        StringBuilder sb = new StringBuilder();
        String str = "";
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                i.d("requestData : POST params is " + sb.toString());
                return sb.toString();
            }
            Map.Entry<String, String> next = it.next();
            sb.append(str2);
            sb.append(next.getKey());
            sb.append("=");
            try {
                strEncode = URLEncoder.encode(next.getValue(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                i.b("encode error, key = " + next.getKey());
                strEncode = "";
            }
            sb.append(strEncode);
            str = "&";
        }
    }

    public static void a(HashMap<String, String> map) {
        c = map;
    }
}
