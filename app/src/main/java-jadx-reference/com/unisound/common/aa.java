package com.unisound.common;

import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* JADX INFO: loaded from: classes.dex */
public class aa {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    private static final String f233a = "29376c08408c089912816e40a05d31df";

    public static String a() {
        return f233a;
    }

    public static String a(File file) {
        String strReplace = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[8192];
                while (true) {
                    try {
                        try {
                            int i = fileInputStream.read(bArr);
                            if (i <= 0) {
                                break;
                            }
                            messageDigest.update(bArr, 0, i);
                        } finally {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                                y.a("MD5 checkMD5: Exception on closing MD5 input stream: " + e);
                            }
                        }
                    } catch (IOException e2) {
                        throw new RuntimeException("Unable to process file for MD5", e2);
                    }
                }
                strReplace = String.format("%32s", new BigInteger(1, messageDigest.digest()).toString(16)).replace(' ', '0');
            } catch (FileNotFoundException e3) {
                y.a("MD5 checkMD5: Exception while getting FileInputStream: " + e3);
                e3.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e4) {
            y.a("MD5 checkMD5: Exception while getting Digest: " + e4);
            e4.printStackTrace();
        }
        return strReplace;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(5:(3:28|4|(1:6)(5:33|17|24|18|35))|11|30|12|13) */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x0082, code lost:
    
        r1 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:23:0x0083, code lost:
    
        r1.printStackTrace();
        com.unisound.common.y.a("MD5 checkMD5: Exception on closing MD5 input stream: " + r1);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String a(InputStream inputStream) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bArr = new byte[8192];
            while (true) {
                try {
                    try {
                        int i = inputStream.read(bArr);
                        if (i <= 0) {
                            String strReplace = String.format("%32s", new BigInteger(1, messageDigest.digest()).toString(16)).replace(' ', '0');
                            try {
                                inputStream.close();
                                return strReplace;
                            } catch (IOException e) {
                                e.printStackTrace();
                                y.a("MD5 checkMD5: Exception on closing MD5 input stream: " + e);
                                return strReplace;
                            }
                        }
                        messageDigest.update(bArr, 0, i);
                    } catch (IOException e2) {
                        throw new RuntimeException("Unable to process file for MD5", e2);
                    }
                } catch (Throwable th) {
                    inputStream.close();
                    throw th;
                }
                inputStream.close();
                throw th;
            }
        } catch (NoSuchAlgorithmException e3) {
            y.a("MD5 checkMD5: Exception while getting Digest: " + e3);
            e3.printStackTrace();
            return null;
        }
    }

    public static String a(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        byte[] bArrDigest = messageDigest.digest(str.getBytes());
        StringBuilder sb = new StringBuilder(40);
        for (byte b : bArrDigest) {
            if (((b & 255) >> 4) == 0) {
                sb.append("0").append(Integer.toHexString(b & 255));
            } else {
                sb.append(Integer.toHexString(b & 255));
            }
        }
        return sb.toString();
    }

    public static boolean a(String str, File file) {
        if (TextUtils.isEmpty(str) || file == null) {
            y.a("MD5 checkMD5: md5 String NULL or File NULL");
            return false;
        }
        String strA = a(file);
        if (strA == null) {
            y.a("MD5 checkMD5: calculatedDigest NULL");
            return false;
        }
        y.b("MD5 checkMD5: Calculated digest: ", strA);
        y.b("MD5 checkMD5: Provided digest: ", str);
        return strA.equalsIgnoreCase(str);
    }
}
