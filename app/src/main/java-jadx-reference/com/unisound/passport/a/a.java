package com.unisound.passport.a;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class a {
    /* JADX WARN: Removed duplicated region for block: B:55:0x00a9 A[Catch: IOException -> 0x00b2, TryCatch #7 {IOException -> 0x00b2, blocks: (B:53:0x00a4, B:55:0x00a9, B:57:0x00ae), top: B:87:0x00a4 }] */
    /* JADX WARN: Removed duplicated region for block: B:57:0x00ae A[Catch: IOException -> 0x00b2, TRY_LEAVE, TryCatch #7 {IOException -> 0x00b2, blocks: (B:53:0x00a4, B:55:0x00a9, B:57:0x00ae), top: B:87:0x00a4 }] */
    /* JADX WARN: Removed duplicated region for block: B:87:0x00a4 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String a(String str, int i) throws Throwable {
        BufferedReader bufferedReader;
        InputStream inputStream;
        IOException e;
        String str2;
        MalformedURLException e2;
        InputStream inputStream2;
        String str3;
        HttpURLConnection httpURLConnection;
        String str4;
        HttpURLConnection httpURLConnection2 = null;
        bufferedReader = null;
        bufferedReader = null;
        BufferedReader bufferedReader2 = null;
        httpURLConnection2 = null;
        String str5 = "";
        try {
            HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection3.setConnectTimeout(i);
                httpURLConnection3.setRequestMethod("GET");
                if (httpURLConnection3.getResponseCode() == 200) {
                    inputStream = httpURLConnection3.getInputStream();
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        while (true) {
                            try {
                                String line = bufferedReader.readLine();
                                if (line == null) {
                                    break;
                                }
                                str5 = str5 + line;
                            } catch (MalformedURLException e3) {
                                e2 = e3;
                                bufferedReader2 = bufferedReader;
                                inputStream2 = inputStream;
                                String str6 = str5;
                                httpURLConnection = httpURLConnection3;
                                str3 = str6;
                                try {
                                    e2.printStackTrace();
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (IOException e4) {
                                            e4.printStackTrace();
                                            return str3;
                                        }
                                    }
                                    if (bufferedReader2 != null) {
                                        bufferedReader2.close();
                                    }
                                    if (httpURLConnection == null) {
                                        return str3;
                                    }
                                    httpURLConnection.disconnect();
                                    return str3;
                                } catch (Throwable th) {
                                    th = th;
                                    inputStream = inputStream2;
                                    bufferedReader = bufferedReader2;
                                    httpURLConnection2 = httpURLConnection;
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
                                    if (httpURLConnection2 != null) {
                                        httpURLConnection2.disconnect();
                                    }
                                    throw th;
                                }
                            } catch (IOException e6) {
                                e = e6;
                                httpURLConnection2 = httpURLConnection3;
                                str2 = str5;
                                try {
                                    e.printStackTrace();
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e7) {
                                            e7.printStackTrace();
                                            return str2;
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (httpURLConnection2 == null) {
                                        return str2;
                                    }
                                    httpURLConnection2.disconnect();
                                    return str2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (inputStream != null) {
                                    }
                                    if (bufferedReader != null) {
                                    }
                                    if (httpURLConnection2 != null) {
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                httpURLConnection2 = httpURLConnection3;
                                th = th3;
                                if (inputStream != null) {
                                }
                                if (bufferedReader != null) {
                                }
                                if (httpURLConnection2 != null) {
                                }
                                throw th;
                            }
                        }
                        str4 = str5;
                    } catch (MalformedURLException e8) {
                        e2 = e8;
                        inputStream2 = inputStream;
                        str3 = "";
                        httpURLConnection = httpURLConnection3;
                    } catch (IOException e9) {
                        e = e9;
                        bufferedReader = null;
                        httpURLConnection2 = httpURLConnection3;
                        str2 = "";
                    } catch (Throwable th4) {
                        bufferedReader = null;
                        httpURLConnection2 = httpURLConnection3;
                        th = th4;
                    }
                } else {
                    str4 = "{}";
                    bufferedReader = null;
                    inputStream = null;
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                        e10.printStackTrace();
                        return str4;
                    }
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (httpURLConnection3 != null) {
                    httpURLConnection3.disconnect();
                }
                return str4;
            } catch (MalformedURLException e11) {
                e2 = e11;
                inputStream2 = null;
                str3 = "";
                httpURLConnection = httpURLConnection3;
            } catch (IOException e12) {
                e = e12;
                bufferedReader = null;
                inputStream = null;
                httpURLConnection2 = httpURLConnection3;
                str2 = "";
            } catch (Throwable th5) {
                bufferedReader = null;
                inputStream = null;
                httpURLConnection2 = httpURLConnection3;
                th = th5;
            }
        } catch (MalformedURLException e13) {
            e2 = e13;
            inputStream2 = null;
            str3 = "";
            httpURLConnection = null;
        } catch (IOException e14) {
            e = e14;
            bufferedReader = null;
            inputStream = null;
            str2 = "";
        } catch (Throwable th6) {
            th = th6;
            bufferedReader = null;
            inputStream = null;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:51:0x00c0 A[Catch: IOException -> 0x00c9, TryCatch #4 {IOException -> 0x00c9, blocks: (B:49:0x00bb, B:51:0x00c0, B:53:0x00c5), top: B:77:0x00bb }] */
    /* JADX WARN: Removed duplicated region for block: B:53:0x00c5 A[Catch: IOException -> 0x00c9, TRY_LEAVE, TryCatch #4 {IOException -> 0x00c9, blocks: (B:49:0x00bb, B:51:0x00c0, B:53:0x00c5), top: B:77:0x00bb }] */
    /* JADX WARN: Removed duplicated region for block: B:77:0x00bb A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String a(String str, Map<String, String> map, int i) throws Throwable {
        InputStream inputStream;
        OutputStream outputStream;
        BufferedReader bufferedReader;
        OutputStream outputStream2;
        BufferedReader bufferedReader2 = null;
        bufferedReader2 = null;
        inputStream = null;
        bufferedReader2 = null;
        InputStream inputStream2 = null;
        byte[] bytes = a(map).toString().getBytes();
        InputStream inputStream3 = null;
        BufferedReader bufferedReader3 = null;
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
                if (httpURLConnection.getResponseCode() != 200) {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return "{}";
                        }
                    }
                    if (0 != 0) {
                        inputStream3.close();
                    }
                    if (0 == 0) {
                        return "{}";
                    }
                    bufferedReader3.close();
                    return "{}";
                }
                inputStream = httpURLConnection.getInputStream();
                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    try {
                        StringBuffer stringBuffer = new StringBuffer();
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            stringBuffer.append(line);
                        }
                        String string = stringBuffer.toString();
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                                return string;
                            }
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (bufferedReader == null) {
                            return string;
                        }
                        bufferedReader.close();
                        return string;
                    } catch (IOException e3) {
                        e = e3;
                        inputStream2 = inputStream;
                        outputStream2 = outputStream;
                    } catch (Throwable th) {
                        th = th;
                        bufferedReader2 = bufferedReader;
                        if (outputStream != null) {
                        }
                        if (inputStream != null) {
                        }
                        if (bufferedReader2 != null) {
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e = e4;
                    bufferedReader = null;
                    inputStream2 = inputStream;
                    outputStream2 = outputStream;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (IOException e5) {
                e = e5;
                bufferedReader = null;
                outputStream2 = outputStream;
            } catch (Throwable th3) {
                th = th3;
                inputStream = null;
            }
        } catch (IOException e6) {
            e = e6;
            bufferedReader = null;
            outputStream2 = null;
        } catch (Throwable th4) {
            th = th4;
            inputStream = null;
            outputStream = null;
        }
        try {
            e.printStackTrace();
            if (outputStream2 != null) {
                try {
                    outputStream2.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            return "-1";
        } catch (Throwable th5) {
            th = th5;
            outputStream = outputStream2;
            inputStream = inputStream2;
            bufferedReader2 = bufferedReader;
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e8) {
                    e8.printStackTrace();
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
    }

    public static String a(Map<String, String> map) {
        String strEncode;
        StringBuilder sb = new StringBuilder();
        String str = "";
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (true) {
            String str2 = str;
            if (!it.hasNext()) {
                Log.e("TEMPLOG", "POST params is " + sb.toString());
                return sb.toString();
            }
            Map.Entry<String, String> next = it.next();
            sb.append(str2);
            sb.append(next.getKey());
            sb.append("=");
            try {
                strEncode = URLEncoder.encode(next.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                strEncode = "";
            }
            sb.append(strEncode);
            str = "&";
        }
    }
}
