package com.tencent.bugly.crashreport.crash.anr;

import cn.yunzhisheng.common.PinyinConverter;
import com.tencent.bugly.proguard.x;
import com.unisound.vui.handler.session.memo.utils.MemoConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: compiled from: BUGLY */
/* JADX INFO: loaded from: classes.dex */
public class TraceFileHelper {

    /* JADX INFO: compiled from: BUGLY */
    public static class a {

        /* JADX INFO: renamed from: a, reason: collision with root package name */
        public long f152a;
        public String b;
        public long c;
        public Map<String, String[]> d;
    }

    /* JADX INFO: compiled from: BUGLY */
    public interface b {
        boolean a(long j);

        boolean a(long j, long j2, String str);

        boolean a(String str, int i, String str2, String str3);
    }

    public static a readTargetDumpInfo(String str, String str2, final boolean z) throws Throwable {
        if (str == null || str2 == null) {
            return null;
        }
        final a aVar = new a();
        readTraceFile(str2, new b() { // from class: com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.1
            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(String str3, int i, String str4, String str5) {
                x.c("new thread %s", str3);
                if (aVar.f152a > 0 && aVar.c > 0 && aVar.b != null) {
                    if (aVar.d == null) {
                        aVar.d = new HashMap();
                    }
                    aVar.d.put(str3, new String[]{str4, str5, new StringBuilder().append(i).toString()});
                }
                return true;
            }

            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(long j, long j2, String str3) {
                x.c("new process %s", str3);
                if (!str3.equals(str3)) {
                    return true;
                }
                aVar.f152a = j;
                aVar.b = str3;
                aVar.c = j2;
                return z;
            }

            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(long j) {
                x.c("process end %d", Long.valueOf(j));
                return aVar.f152a <= 0 || aVar.c <= 0 || aVar.b == null;
            }
        });
        if (aVar.f152a <= 0 || aVar.c <= 0 || aVar.b == null) {
            return null;
        }
        return aVar;
    }

    public static a readFirstDumpInfo(String str, final boolean z) throws Throwable {
        if (str == null) {
            x.e("path:%s", str);
            return null;
        }
        final a aVar = new a();
        readTraceFile(str, new b() { // from class: com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.2
            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(String str2, int i, String str3, String str4) {
                x.c("new thread %s", str2);
                if (aVar.d == null) {
                    aVar.d = new HashMap();
                }
                aVar.d.put(str2, new String[]{str3, str4, new StringBuilder().append(i).toString()});
                return true;
            }

            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(long j, long j2, String str2) {
                x.c("new process %s", str2);
                aVar.f152a = j;
                aVar.b = str2;
                aVar.c = j2;
                return z;
            }

            @Override // com.tencent.bugly.crashreport.crash.anr.TraceFileHelper.b
            public final boolean a(long j) {
                x.c("process end %d", Long.valueOf(j));
                return false;
            }
        });
        if (aVar.f152a > 0 && aVar.c > 0 && aVar.b != null) {
            return aVar;
        }
        x.e("first dump error %s", aVar.f152a + PinyinConverter.PINYIN_SEPARATOR + aVar.c + PinyinConverter.PINYIN_SEPARATOR + aVar.b);
        return null;
    }

    /* JADX WARN: Removed duplicated region for block: B:85:0x01c1 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static void readTraceFile(String str, b bVar) throws Throwable {
        BufferedReader bufferedReader;
        if (str == null || bVar == null) {
            return;
        }
        File file = new File(str);
        if (!file.exists()) {
            return;
        }
        file.lastModified();
        file.length();
        BufferedReader bufferedReader2 = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            try {
                Pattern patternCompile = Pattern.compile("-{5}\\spid\\s\\d+\\sat\\s\\d+-\\d+-\\d+\\s\\d{2}:\\d{2}:\\d{2}\\s-{5}");
                Pattern patternCompile2 = Pattern.compile("-{5}\\send\\s\\d+\\s-{5}");
                Pattern patternCompile3 = Pattern.compile("Cmd\\sline:\\s(\\S+)");
                Pattern patternCompile4 = Pattern.compile("\".+\"\\s(daemon\\s){0,1}prio=\\d+\\stid=\\d+\\s.*");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MemoConstants.DATE_FORMATE_YMDHMS, Locale.US);
                while (true) {
                    Object[] objArrA = a(bufferedReader, patternCompile);
                    if (objArrA == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e) {
                            if (x.a(e)) {
                                return;
                            }
                            e.printStackTrace();
                            return;
                        }
                    }
                    String[] strArrSplit = objArrA[1].toString().split("\\s");
                    long j = Long.parseLong(strArrSplit[2]);
                    long time = simpleDateFormat.parse(strArrSplit[4] + PinyinConverter.PINYIN_SEPARATOR + strArrSplit[5]).getTime();
                    Object[] objArrA2 = a(bufferedReader, patternCompile3);
                    if (objArrA2 == null) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e2) {
                            if (x.a(e2)) {
                                return;
                            }
                            e2.printStackTrace();
                            return;
                        }
                    }
                    Matcher matcher = patternCompile3.matcher(objArrA2[1].toString());
                    matcher.find();
                    matcher.group(1);
                    if (!bVar.a(j, time, matcher.group(1))) {
                        try {
                            bufferedReader.close();
                            return;
                        } catch (IOException e3) {
                            if (x.a(e3)) {
                                return;
                            }
                            e3.printStackTrace();
                            return;
                        }
                    }
                    while (true) {
                        Object[] objArrA3 = a(bufferedReader, patternCompile4, patternCompile2);
                        if (objArrA3 == null) {
                            break;
                        }
                        if (objArrA3[0] == patternCompile4) {
                            String string = objArrA3[1].toString();
                            Matcher matcher2 = Pattern.compile("\".+\"").matcher(string);
                            matcher2.find();
                            String strSubstring = matcher2.group().substring(1, r2.length() - 1);
                            string.contains("NATIVE");
                            Matcher matcher3 = Pattern.compile("tid=\\d+").matcher(string);
                            matcher3.find();
                            String strGroup = matcher3.group();
                            bVar.a(strSubstring, Integer.parseInt(strGroup.substring(strGroup.indexOf("=") + 1)), a(bufferedReader), b(bufferedReader));
                        } else if (!bVar.a(Long.parseLong(objArrA3[1].toString().split("\\s")[2]))) {
                            try {
                                bufferedReader.close();
                                return;
                            } catch (IOException e4) {
                                if (x.a(e4)) {
                                    return;
                                }
                                e4.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e5) {
                e = e5;
                bufferedReader2 = bufferedReader;
                try {
                    if (!x.a(e)) {
                        e.printStackTrace();
                    }
                    x.d("trace open fail:%s : %s", e.getClass().getName(), e.getMessage());
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e6) {
                            if (x.a(e6)) {
                                return;
                            }
                            e6.printStackTrace();
                        }
                    }
                } catch (Throwable th) {
                    th = th;
                    bufferedReader = bufferedReader2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e7) {
                            if (!x.a(e7)) {
                                e7.printStackTrace();
                            }
                        }
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                if (bufferedReader != null) {
                }
                throw th;
            }
        } catch (Exception e8) {
            e = e8;
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
        }
    }

    private static Object[] a(BufferedReader bufferedReader, Pattern... patternArr) throws IOException {
        if (bufferedReader == null || patternArr == null) {
            return null;
        }
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                return null;
            }
            for (Pattern pattern : patternArr) {
                if (pattern.matcher(line).matches()) {
                    return new Object[]{pattern, line};
                }
            }
        }
    }

    private static String a(BufferedReader bufferedReader) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < 3; i++) {
            String line = bufferedReader.readLine();
            if (line == null) {
                return null;
            }
            stringBuffer.append(line + "\n");
        }
        return stringBuffer.toString();
    }

    private static String b(BufferedReader bufferedReader) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null || line.trim().length() <= 0) {
                break;
            }
            stringBuffer.append(line + "\n");
        }
        return stringBuffer.toString();
    }
}
