package com.baidu.b.a;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Base64;
import com.unisound.client.SpeechConstants;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
class e {

    static class a {
        public static String a(byte[] bArr) {
            char[] cArr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            StringBuilder sb = new StringBuilder(bArr.length * 2);
            for (int i = 0; i < bArr.length; i++) {
                sb.append(cArr[(bArr[i] & 240) >> 4]);
                sb.append(cArr[bArr[i] & 15]);
            }
            return sb.toString();
        }
    }

    static String a() {
        return Locale.getDefault().getLanguage();
    }

    protected static String a(Context context) {
        String packageName = context.getPackageName();
        return a(context, packageName) + ";" + packageName;
    }

    private static String a(Context context, String str) {
        String strA;
        try {
            strA = a((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(context.getPackageManager().getPackageInfo(str, 64).signatures[0].toByteArray())));
        } catch (PackageManager.NameNotFoundException e) {
            strA = "";
        } catch (CertificateException e2) {
            strA = "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < strA.length(); i++) {
            stringBuffer.append(strA.charAt(i));
            if (i > 0 && i % 2 == 1 && i < strA.length() - 1) {
                stringBuffer.append(":");
            }
        }
        return stringBuffer.toString();
    }

    static String a(X509Certificate x509Certificate) {
        try {
            return a.a(a(x509Certificate.getEncoded()));
        } catch (CertificateEncodingException e) {
            return null;
        }
    }

    static byte[] a(byte[] bArr) {
        try {
            return MessageDigest.getInstance("SHA1").digest(bArr);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    protected static String[] b(Context context) {
        String packageName = context.getPackageName();
        String[] strArrB = b(context, packageName);
        if (strArrB == null || strArrB.length <= 0) {
            return null;
        }
        String[] strArr = new String[strArrB.length];
        for (int i = 0; i < strArr.length; i++) {
            strArr[i] = strArrB[i] + ";" + packageName;
            if (d.f40a) {
                d.a("mcode" + strArr[i]);
            }
        }
        return strArr;
    }

    private static String[] b(Context context, String str) {
        String[] strArr;
        String[] strArr2;
        String[] strArr3;
        String[] strArr4 = null;
        try {
            Signature[] signatureArr = context.getPackageManager().getPackageInfo(str, 64).signatures;
            if (signatureArr == null || signatureArr.length <= 0) {
                strArr3 = null;
            } else {
                strArr = new String[signatureArr.length];
                for (int i = 0; i < signatureArr.length; i++) {
                    try {
                        strArr[i] = a((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(signatureArr[i].toByteArray())));
                    } catch (PackageManager.NameNotFoundException e) {
                        strArr2 = strArr;
                    } catch (CertificateException e2) {
                        strArr2 = strArr;
                    }
                }
                strArr3 = strArr;
            }
            strArr2 = strArr3;
        } catch (PackageManager.NameNotFoundException e3) {
            strArr = null;
        } catch (CertificateException e4) {
            strArr = null;
        }
        if (strArr2 != null && strArr2.length > 0) {
            strArr4 = new String[strArr2.length];
            for (int i2 = 0; i2 < strArr2.length; i2++) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i3 = 0; i3 < strArr2[i2].length(); i3++) {
                    stringBuffer.append(strArr2[i2].charAt(i3));
                    if (i3 > 0 && i3 % 2 == 1 && i3 < strArr2[i2].length() - 1) {
                        stringBuffer.append(":");
                    }
                }
                strArr4[i2] = stringBuffer.toString();
            }
        }
        return strArr4;
    }

    static String c(Context context) {
        String string = null;
        if ((0 == 0 || "".equals(null)) && (string = context.getSharedPreferences("mac", 0).getString("macaddr", null)) == null) {
            String strD = d(context);
            if (strD != null) {
                string = Base64.encodeToString(strD.getBytes(), 0);
                if (!TextUtils.isEmpty(string)) {
                    context.getSharedPreferences("mac", 0).edit().putString("macaddr", string).commit();
                }
            } else {
                string = "";
            }
        }
        if (d.f40a) {
            d.a("getMacID mac_adress: " + string);
        }
        return string;
    }

    private static boolean c(Context context, String str) {
        boolean z = context.checkCallingOrSelfPermission(str) != -1;
        if (d.f40a) {
            d.a("hasPermission " + z + " | " + str);
        }
        return z;
    }

    static String d(Context context) {
        String macAddress;
        Exception e;
        try {
            if (!c(context, SpeechConstants.PERMISSION_ACCESS_WIFI_STATE)) {
                if (d.f40a) {
                    d.a("You need the android.Manifest.permission.ACCESS_WIFI_STATE permission. Open AndroidManifest.xml and just before the final </manifest> tag add:android.permission.ACCESS_WIFI_STATE");
                }
                return null;
            }
            WifiInfo connectionInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
            macAddress = connectionInfo.getMacAddress();
            try {
                if (!TextUtils.isEmpty(macAddress)) {
                    Base64.encode(macAddress.getBytes(), 0);
                }
                if (!d.f40a) {
                    return macAddress;
                }
                d.a(String.format("ssid=%s mac=%s", connectionInfo.getSSID(), connectionInfo.getMacAddress()));
                return macAddress;
            } catch (Exception e2) {
                e = e2;
                if (!d.f40a) {
                    return macAddress;
                }
                d.a(e.toString());
                return macAddress;
            }
        } catch (Exception e3) {
            macAddress = null;
            e = e3;
        }
    }
}
