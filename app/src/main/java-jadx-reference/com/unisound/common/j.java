package com.unisound.common;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class j {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static byte[] f262a = null;

    /* JADX WARN: Can't wrap try/catch for region: R(19:0|2|72|3|70|4|5|74|6|(5:7|(1:9)(1:78)|(2:68|14)|(2:66|16)|17)|18|19|76|20|21|(2:58|23)|(2:60|25)|17|(1:(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:31:0x004f, code lost:
    
        if (r3 != null) goto L64;
     */
    /* JADX WARN: Code restructure failed: missing block: B:32:0x0051, code lost:
    
        r3.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:33:0x0054, code lost:
    
        if (r1 != null) goto L62;
     */
    /* JADX WARN: Code restructure failed: missing block: B:34:0x0056, code lost:
    
        r1.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:35:0x0059, code lost:
    
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:50:0x0069, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:51:0x006a, code lost:
    
        r3 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:79:?, code lost:
    
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:80:?, code lost:
    
        throw r0;
     */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0032 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x002d A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static boolean a(Context context, String str, File file) throws Throwable {
        FileOutputStream fileOutputStream;
        InputStream inputStreamOpen;
        byte[] bArr;
        boolean z = false;
        InputStream inputStream = null;
        d(file.getAbsolutePath());
        try {
            inputStreamOpen = context.getAssets().open(str);
            try {
                fileOutputStream = new FileOutputStream(file);
                try {
                    bArr = new byte[10240];
                } catch (Exception e) {
                    inputStream = inputStreamOpen;
                } catch (Throwable th) {
                    th = th;
                }
            } catch (Exception e2) {
                fileOutputStream = null;
                inputStream = inputStreamOpen;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = null;
            }
        } catch (Exception e3) {
            fileOutputStream = null;
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            inputStreamOpen = null;
        }
        while (true) {
            int i = inputStreamOpen.read(bArr, 0, 10240);
            if (i == -1) {
                break;
            }
            fileOutputStream.write(bArr, 0, i);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e5) {
                }
            }
            return z;
        }
        inputStreamOpen.close();
        InputStream inputStream2 = null;
        fileOutputStream.close();
        OutputStream outputStream = null;
        z = true;
        if (0 != 0) {
            try {
                inputStream2.close();
            } catch (IOException e6) {
            }
        }
        if (0 != 0) {
            try {
                outputStream.close();
            } catch (IOException e7) {
            }
        }
        return z;
    }

    public static boolean a(Context context, String str, String str2, String str3) {
        File file = new File(str2);
        if (file.exists() && aa.a(str3, file)) {
            return true;
        }
        return a(context, str, file);
    }

    public static boolean a(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return f(str);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v10 */
    /* JADX WARN: Type inference failed for: r2v11 */
    /* JADX WARN: Type inference failed for: r2v12 */
    /* JADX WARN: Type inference failed for: r2v13 */
    /* JADX WARN: Type inference failed for: r2v14 */
    /* JADX WARN: Type inference failed for: r2v15 */
    /* JADX WARN: Type inference failed for: r2v16 */
    /* JADX WARN: Type inference failed for: r2v6 */
    /* JADX WARN: Type inference failed for: r2v8 */
    /* JADX WARN: Type inference failed for: r2v9 */
    public static boolean a(String str, int i, int i2) throws Throwable {
        RandomAccessFile randomAccessFile;
        boolean z = false;
        File file = new File(str);
        RandomAccessFile randomAccessFileExists = file.exists();
        try {
            if (randomAccessFileExists != 0) {
                try {
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    try {
                        int length = (int) randomAccessFile.length();
                        randomAccessFile.seek(0L);
                        byte[] bArrA = be.a(length, i, i2);
                        randomAccessFileExists = randomAccessFile;
                        if (bArrA != null) {
                            randomAccessFile.write(bArrA);
                            z = true;
                            randomAccessFileExists = randomAccessFile;
                            if (randomAccessFile != null) {
                                try {
                                    randomAccessFile.close();
                                    randomAccessFileExists = randomAccessFile;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    randomAccessFileExists = randomAccessFile;
                                }
                            }
                        } else if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                                randomAccessFileExists = randomAccessFile;
                            } catch (IOException e2) {
                                e2.printStackTrace();
                                randomAccessFileExists = randomAccessFile;
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        e.printStackTrace();
                        randomAccessFileExists = randomAccessFile;
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                                randomAccessFileExists = randomAccessFile;
                            } catch (IOException e4) {
                                e4.printStackTrace();
                                randomAccessFileExists = randomAccessFile;
                            }
                        }
                    }
                } catch (IOException e5) {
                    e = e5;
                    randomAccessFile = null;
                } catch (Throwable th) {
                    th = th;
                    randomAccessFileExists = 0;
                    if (randomAccessFileExists != 0) {
                        try {
                            randomAccessFileExists.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
            return z;
        } catch (Throwable th2) {
            th = th2;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:42:0x0054 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(boolean z, int i, String str) throws Throwable {
        RandomAccessFile randomAccessFile;
        if (str != null && !"".equals(str) && a(str)) {
            d(str);
            byte[] bArrA = z ? a((short) i) : a((short) (-i));
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArrA);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:38:0x0055 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(boolean z, String str) {
        RandomAccessFile randomAccessFile;
        if (str != null && !"".equals(str) && a(str)) {
            d(str);
            byte[] bArrA = z ? a(Short.MAX_VALUE) : a((short) -32767);
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArrA);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:35:0x0046 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean a(byte[] bArr, String str) {
        RandomAccessFile randomAccessFile;
        if (str != null && !"".equals(str) && a(str)) {
            d(str);
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArr);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    public static byte[] a(Context context) {
        if (f262a == null) {
            f262a = Arrays.copyOfRange(a(context, "empty"), 0, 6400);
        }
        return f262a;
    }

    /* JADX WARN: Removed duplicated region for block: B:45:0x003c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static byte[] a(Context context, String str) throws Throwable {
        InputStream inputStreamOpen;
        byte[] bArr;
        byte[] bArr2;
        int i;
        InputStream inputStream = null;
        try {
            inputStreamOpen = context.getAssets().open(str);
            try {
                try {
                    bArr = new byte[6400];
                    try {
                        int i2 = inputStreamOpen.read(bArr, 0, bArr.length);
                        InputStream inputStream2 = null;
                        if (0 != 0) {
                            try {
                                inputStream2.close();
                                i = i2;
                                bArr2 = bArr;
                            } catch (IOException e) {
                                i = i2;
                                bArr2 = bArr;
                            }
                        } else {
                            i = i2;
                            bArr2 = bArr;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        inputStream = inputStreamOpen;
                        try {
                            e.printStackTrace();
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                    bArr2 = bArr;
                                    i = 0;
                                } catch (IOException e3) {
                                    bArr2 = bArr;
                                    i = 0;
                                }
                            } else {
                                bArr2 = bArr;
                                i = 0;
                            }
                        } catch (Throwable th) {
                            th = th;
                            inputStreamOpen = inputStream;
                            if (inputStreamOpen != null) {
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamOpen != null) {
                        try {
                            inputStreamOpen.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                bArr = null;
                inputStream = inputStreamOpen;
            }
        } catch (Exception e6) {
            e = e6;
            bArr = null;
        } catch (Throwable th3) {
            th = th3;
            inputStreamOpen = null;
        }
        return i > 0 ? bArr2 : new byte[6400];
    }

    public static byte[] a(short s) {
        byte[] bArr = new byte[2];
        for (int i = 0; i < 2; i++) {
            bArr[i] = (byte) ((s >>> 8) & 255);
        }
        return bArr;
    }

    public static boolean b(String str) {
        return e(str) == k.WAV_EXTENSION_NAME;
    }

    /* JADX WARN: Removed duplicated region for block: B:38:0x0055 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean b(boolean z, String str) throws Throwable {
        RandomAccessFile randomAccessFile;
        if (str != null && !"".equals(str) && a(str)) {
            d(str);
            byte[] bArrA = z ? a((short) 28000) : a((short) -28000);
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArrA);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:35:0x0046 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean b(byte[] bArr, String str) throws Throwable {
        RandomAccessFile randomAccessFile;
        if (str != null && !"".equals(str) && a(str)) {
            d(str);
            try {
                randomAccessFile = new RandomAccessFile(str, "rw");
                try {
                    try {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(bArr);
                        if (randomAccessFile == null) {
                            return true;
                        }
                        try {
                            randomAccessFile.close();
                            return true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        e.printStackTrace();
                        if (randomAccessFile != null) {
                            try {
                                randomAccessFile.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                        return false;
                    }
                } catch (Throwable th) {
                    th = th;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                randomAccessFile = null;
            } catch (Throwable th2) {
                th = th2;
                randomAccessFile = null;
                if (randomAccessFile != null) {
                }
                throw th;
            }
        }
        return false;
    }

    public static boolean c(String str) {
        if (new File(str).exists()) {
            return f(str);
        }
        return false;
    }

    private static void d(String str) {
        int iLastIndexOf;
        if (str != null && (iLastIndexOf = str.lastIndexOf(47)) >= 0) {
            new File(str.substring(0, iLastIndexOf)).mkdirs();
        }
    }

    private static k e(String str) {
        int iLastIndexOf;
        if (TextUtils.isEmpty(str) || (iLastIndexOf = str.lastIndexOf(".")) <= 0) {
            return null;
        }
        String strSubstring = str.substring(iLastIndexOf + 1);
        return TextUtils.isEmpty(strSubstring) ? k.NO_EXTENSION_NAME : strSubstring.equalsIgnoreCase(com.unisound.sdk.c.b) ? k.PCM_EXTENSION_NAME : strSubstring.equalsIgnoreCase("wav") ? k.WAV_EXTENSION_NAME : k.OTHER_EXTENSION_NAME;
    }

    private static boolean f(String str) {
        k kVarE = e(str);
        if (kVarE == k.PCM_EXTENSION_NAME || kVarE == k.WAV_EXTENSION_NAME) {
            return true;
        }
        y.a("fileName illegal");
        return false;
    }
}
