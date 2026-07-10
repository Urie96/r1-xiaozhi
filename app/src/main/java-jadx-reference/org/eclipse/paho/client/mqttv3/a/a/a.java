package org.eclipse.paho.client.mqttv3.a.a;

import cn.yunzhisheng.asr.JniUscClient;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.logging.Logger;

/* JADX INFO: loaded from: classes.dex */
public class a {
    private static final String C = "{xor}";
    public static final String o = "javax.net.ssl.keyStore";
    public static final String p = "javax.net.ssl.keyStoreType";
    public static final String q = "javax.net.ssl.keyStorePassword";
    public static final String r = "javax.net.ssl.trustStore";
    public static final String s = "javax.net.ssl.trustStoreType";
    public static final String t = "javax.net.ssl.trustStorePassword";
    public static final String u = "ssl.KeyManagerFactory.algorithm";
    public static final String v = "ssl.TrustManagerFactory.algorithm";
    public static final String w = "TLS";
    private static final String x = "org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory";
    private Properties A;
    private Logger D;
    private Hashtable z;

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static final String f469a = "com.ibm.ssl.protocol";
    public static final String b = "com.ibm.ssl.contextProvider";
    public static final String c = "com.ibm.ssl.keyStore";
    public static final String d = "com.ibm.ssl.keyStorePassword";
    public static final String e = "com.ibm.ssl.keyStoreType";
    public static final String f = "com.ibm.ssl.keyStoreProvider";
    public static final String g = "com.ibm.ssl.keyManager";
    public static final String h = "com.ibm.ssl.trustStore";
    public static final String i = "com.ibm.ssl.trustStorePassword";
    public static final String j = "com.ibm.ssl.trustStoreType";
    public static final String k = "com.ibm.ssl.trustStoreProvider";
    public static final String l = "com.ibm.ssl.trustManager";
    public static final String m = "com.ibm.ssl.enabledCipherSuites";
    public static final String n = "com.ibm.ssl.clientAuthentication";
    private static final String[] y = {f469a, b, c, d, e, f, g, h, i, j, k, l, m, n};
    private static final byte[] B = {-99, -89, -39, -128, 5, -72, -119, -100};

    public a() {
        this.D = null;
        this.z = new Hashtable();
    }

    public a(Logger logger) {
        this();
        this.D = logger;
    }

    private String a(String str, String str2) {
        String property;
        Properties properties = str != null ? (Properties) this.z.get(str) : null;
        if (properties != null) {
            property = properties.getProperty(str2);
            if (property == null) {
            }
            return property;
        }
        property = null;
        Properties properties2 = this.A;
        if (properties2 == null || (property = properties2.getProperty(str2)) != null) {
        }
        return property;
    }

    private String a(String str, String str2, String str3) {
        String strA = a(str, str2);
        return (strA == null && str3 != null) ? System.getProperty(str3) : strA;
    }

    public static String a(String[] strArr) {
        if (strArr == null) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < strArr.length; i2++) {
            stringBuffer.append(strArr[i2]);
            if (i2 < strArr.length - 1) {
                stringBuffer.append(',');
            }
        }
        return stringBuffer.toString();
    }

    private void a(Properties properties) {
        for (String str : properties.keySet()) {
            if (!t(str)) {
                throw new IllegalArgumentException(new StringBuffer(String.valueOf(str)).append(" is not a valid IBM SSL property key.").toString());
            }
        }
    }

    public static boolean a() {
        try {
            Class.forName("javax.net.ssl.SSLServerSocketFactory");
            return true;
        } catch (ClassNotFoundException e2) {
            return false;
        }
    }

    public static byte[] a(char[] cArr) {
        int i2 = 0;
        if (cArr == null) {
            return null;
        }
        byte[] bArr = new byte[cArr.length * 2];
        int i3 = 0;
        while (i2 < cArr.length) {
            int i4 = i3 + 1;
            bArr[i3] = (byte) (cArr[i2] & 255);
            bArr[i4] = (byte) ((cArr[i2] >> '\b') & 255);
            i2++;
            i3 = i4 + 1;
        }
        return bArr;
    }

    public static char[] a(String str) {
        if (str == null) {
            return null;
        }
        try {
            byte[] bArrA = b.a(str.substring(C.length()));
            for (int i2 = 0; i2 < bArrA.length; i2++) {
                bArrA[i2] = (byte) ((bArrA[i2] ^ B[i2 % B.length]) & 255);
            }
            return a(bArrA);
        } catch (Exception e2) {
            return null;
        }
    }

    public static char[] a(byte[] bArr) {
        int i2 = 0;
        if (bArr == null) {
            return null;
        }
        char[] cArr = new char[bArr.length / 2];
        int i3 = 0;
        while (i3 < bArr.length) {
            int i4 = i3 + 1;
            int i5 = bArr[i3] & 255;
            i3 = i4 + 1;
            cArr[i2] = (char) (((bArr[i4] & 255) << 8) + i5);
            i2++;
        }
        return cArr;
    }

    public static String b(char[] cArr) {
        if (cArr == null) {
            return null;
        }
        byte[] bArrA = a(cArr);
        for (int i2 = 0; i2 < bArrA.length; i2++) {
            bArrA[i2] = (byte) ((bArrA[i2] ^ B[i2 % B.length]) & 255);
        }
        return new StringBuffer(C).append(new String(b.a(bArrA))).toString();
    }

    private void b(Properties properties) {
        String property = properties.getProperty(d);
        if (property != null && !property.startsWith(C)) {
            properties.put(d, b(property.toCharArray()));
        }
        String property2 = properties.getProperty(i);
        if (property2 == null || property2.startsWith(C)) {
            return;
        }
        properties.put(i, b(property2.toCharArray()));
    }

    public static String[] b(String str) {
        if (str == null) {
            return null;
        }
        Vector vector = new Vector();
        int iIndexOf = str.indexOf(44);
        int i2 = 0;
        while (iIndexOf > -1) {
            vector.add(str.substring(i2, iIndexOf));
            i2 = iIndexOf + 1;
            iIndexOf = str.indexOf(44, i2);
        }
        vector.add(str.substring(i2));
        String[] strArr = new String[vector.size()];
        vector.toArray(strArr);
        return strArr;
    }

    private boolean t(String str) {
        int i2 = 0;
        while (i2 < y.length && !y[i2].equals(str)) {
            i2++;
        }
        return i2 < y.length;
    }

    /* JADX WARN: Removed duplicated region for block: B:192:0x02d7  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private SSLContext u(String str) throws MqttSecurityException {
        KeyManager[] keyManagers;
        TrustManager[] trustManagers;
        String strE = e(str);
        if (strE == null) {
            strE = w;
        }
        if (this.D != null) {
            Logger logger = this.D;
            Object[] objArr = new Object[2];
            objArr[0] = str != null ? str : "null (broker defaults)";
            objArr[1] = strE;
            logger.fine(x, "getSSLContext", "12000", objArr);
        }
        String strF = f(str);
        try {
            SSLContext sSLContext = strF == null ? SSLContext.getInstance(strE) : SSLContext.getInstance(strE, strF);
            if (this.D != null) {
                Logger logger2 = this.D;
                Object[] objArr2 = new Object[2];
                objArr2[0] = str != null ? str : "null (broker defaults)";
                objArr2[1] = sSLContext.getProvider().getName();
                logger2.fine(x, "getSSLContext", "12001", objArr2);
            }
            String strA = a(str, c, null);
            if (0 != 0) {
                keyManagers = null;
            } else {
                if (strA == null) {
                    strA = a(str, c, o);
                }
                if (this.D != null) {
                    Logger logger3 = this.D;
                    Object[] objArr3 = new Object[2];
                    objArr3[0] = str != null ? str : "null (broker defaults)";
                    objArr3[1] = strA != null ? strA : JniUscClient.az;
                    logger3.fine(x, "getSSLContext", "12004", objArr3);
                }
                char[] cArrH = h(str);
                if (this.D != null) {
                    Logger logger4 = this.D;
                    Object[] objArr4 = new Object[2];
                    objArr4[0] = str != null ? str : "null (broker defaults)";
                    objArr4[1] = cArrH != null ? b(cArrH) : JniUscClient.az;
                    logger4.fine(x, "getSSLContext", "12005", objArr4);
                }
                String strI = i(str);
                if (strI == null) {
                    strI = KeyStore.getDefaultType();
                }
                if (this.D != null) {
                    Logger logger5 = this.D;
                    Object[] objArr5 = new Object[2];
                    objArr5[0] = str != null ? str : "null (broker defaults)";
                    objArr5[1] = strI != null ? strI : JniUscClient.az;
                    logger5.fine(x, "getSSLContext", "12006", objArr5);
                }
                String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                String strJ = j(str);
                String strK = k(str);
                if (strK == null) {
                    strK = defaultAlgorithm;
                }
                if (strA != null && strI != null && strK != null) {
                    try {
                        KeyStore keyStore = KeyStore.getInstance(strI);
                        keyStore.load(new FileInputStream(strA), cArrH);
                        KeyManagerFactory keyManagerFactory = strJ != null ? KeyManagerFactory.getInstance(strK, strJ) : KeyManagerFactory.getInstance(strK);
                        if (this.D != null) {
                            Logger logger6 = this.D;
                            Object[] objArr6 = new Object[2];
                            objArr6[0] = str != null ? str : "null (broker defaults)";
                            if (strK == null) {
                                strK = JniUscClient.az;
                            }
                            objArr6[1] = strK;
                            logger6.fine(x, "getSSLContext", "12010", objArr6);
                            Logger logger7 = this.D;
                            Object[] objArr7 = new Object[2];
                            objArr7[0] = str != null ? str : "null (broker defaults)";
                            objArr7[1] = keyManagerFactory.getProvider().getName();
                            logger7.fine(x, "getSSLContext", "12009", objArr7);
                        }
                        keyManagerFactory.init(keyStore, cArrH);
                        keyManagers = keyManagerFactory.getKeyManagers();
                    } catch (FileNotFoundException e2) {
                        throw new MqttSecurityException(e2);
                    } catch (IOException e3) {
                        throw new MqttSecurityException(e3);
                    } catch (KeyStoreException e4) {
                        throw new MqttSecurityException(e4);
                    } catch (UnrecoverableKeyException e5) {
                        throw new MqttSecurityException(e5);
                    } catch (CertificateException e6) {
                        throw new MqttSecurityException(e6);
                    }
                }
            }
            String strL = l(str);
            if (this.D != null) {
                Logger logger8 = this.D;
                Object[] objArr8 = new Object[2];
                objArr8[0] = str != null ? str : "null (broker defaults)";
                objArr8[1] = strL != null ? strL : JniUscClient.az;
                logger8.fine(x, "getSSLContext", "12011", objArr8);
            }
            char[] cArrM = m(str);
            if (this.D != null) {
                Logger logger9 = this.D;
                Object[] objArr9 = new Object[2];
                objArr9[0] = str != null ? str : "null (broker defaults)";
                objArr9[1] = cArrM != null ? b(cArrM) : JniUscClient.az;
                logger9.fine(x, "getSSLContext", "12012", objArr9);
            }
            String strN = n(str);
            if (strN == null) {
                strN = KeyStore.getDefaultType();
            }
            if (this.D != null) {
                Logger logger10 = this.D;
                Object[] objArr10 = new Object[2];
                objArr10[0] = str != null ? str : "null (broker defaults)";
                objArr10[1] = strN != null ? strN : JniUscClient.az;
                logger10.fine(x, "getSSLContext", "12013", objArr10);
            }
            String defaultAlgorithm2 = TrustManagerFactory.getDefaultAlgorithm();
            String strO = o(str);
            String strP = p(str);
            if (strP == null) {
                strP = defaultAlgorithm2;
            }
            if (strL == null || strN == null || strP == null) {
                trustManagers = null;
            } else {
                try {
                    KeyStore keyStore2 = KeyStore.getInstance(strN);
                    keyStore2.load(new FileInputStream(strL), cArrM);
                    TrustManagerFactory trustManagerFactory = strO != null ? TrustManagerFactory.getInstance(strP, strO) : TrustManagerFactory.getInstance(strP);
                    if (this.D != null) {
                        Logger logger11 = this.D;
                        Object[] objArr11 = new Object[2];
                        objArr11[0] = str != null ? str : "null (broker defaults)";
                        if (strP == null) {
                            strP = JniUscClient.az;
                        }
                        objArr11[1] = strP;
                        logger11.fine(x, "getSSLContext", "12017", objArr11);
                        Logger logger12 = this.D;
                        Object[] objArr12 = new Object[2];
                        if (str == null) {
                            str = "null (broker defaults)";
                        }
                        objArr12[0] = str;
                        objArr12[1] = trustManagerFactory.getProvider().getName();
                        logger12.fine(x, "getSSLContext", "12016", objArr12);
                    }
                    trustManagerFactory.init(keyStore2);
                    trustManagers = trustManagerFactory.getTrustManagers();
                } catch (FileNotFoundException e7) {
                    throw new MqttSecurityException(e7);
                } catch (IOException e8) {
                    throw new MqttSecurityException(e8);
                } catch (KeyStoreException e9) {
                    throw new MqttSecurityException(e9);
                } catch (CertificateException e10) {
                    throw new MqttSecurityException(e10);
                }
            }
            sSLContext.init(keyManagers, trustManagers, null);
            return sSLContext;
        } catch (KeyManagementException e11) {
            throw new MqttSecurityException(e11);
        } catch (NoSuchAlgorithmException e12) {
            throw new MqttSecurityException(e12);
        } catch (NoSuchProviderException e13) {
            throw new MqttSecurityException(e13);
        }
    }

    public void a(Properties properties, String str) {
        a(properties);
        Properties properties2 = new Properties();
        properties2.putAll(properties);
        b(properties2);
        if (str != null) {
            this.z.put(str, properties2);
        } else {
            this.A = properties2;
        }
    }

    public void b(Properties properties, String str) {
        a(properties);
        Properties properties2 = this.A;
        if (str != null) {
            properties2 = (Properties) this.z.get(str);
        }
        if (properties2 == null) {
            properties2 = new Properties();
        }
        b(properties);
        properties2.putAll(properties);
        if (str != null) {
            this.z.put(str, properties2);
        } else {
            this.A = properties2;
        }
    }

    public boolean c(String str) {
        if (str != null) {
            return this.z.remove(str) != null;
        }
        if (this.A == null) {
            return false;
        }
        this.A = null;
        return true;
    }

    public Properties d(String str) {
        return (Properties) (str == null ? this.A : this.z.get(str));
    }

    public String e(String str) {
        return a(str, f469a, null);
    }

    public String f(String str) {
        return a(str, b, null);
    }

    public String g(String str) {
        String strA = a(str, c);
        return (strA == null && o != 0) ? System.getProperty(o) : strA;
    }

    public char[] h(String str) {
        String strA = a(str, d, q);
        if (strA != null) {
            return strA.startsWith(C) ? a(strA) : strA.toCharArray();
        }
        return null;
    }

    public String i(String str) {
        return a(str, e, p);
    }

    public String j(String str) {
        return a(str, f, null);
    }

    public String k(String str) {
        return a(str, g, u);
    }

    public String l(String str) {
        return a(str, h, r);
    }

    public char[] m(String str) {
        String strA = a(str, i, t);
        if (strA != null) {
            return strA.startsWith(C) ? a(strA) : strA.toCharArray();
        }
        return null;
    }

    public String n(String str) {
        return a(str, j, null);
    }

    public String o(String str) {
        return a(str, k, null);
    }

    public String p(String str) {
        return a(str, l, v);
    }

    public String[] q(String str) {
        return b(a(str, m, null));
    }

    public boolean r(String str) {
        String strA = a(str, n, null);
        if (strA != null) {
            return Boolean.valueOf(strA).booleanValue();
        }
        return false;
    }

    public SSLSocketFactory s(String str) throws MqttSecurityException {
        SSLContext sSLContextU = u(str);
        if (this.D != null) {
            Logger logger = this.D;
            Object[] objArr = new Object[2];
            objArr[0] = str != null ? str : "null (broker defaults)";
            objArr[1] = q(str) != null ? a(str, m, null) : "null (using platform-enabled cipher suites)";
            logger.fine(x, "createSocketFactory", "12020", objArr);
        }
        return sSLContextU.getSocketFactory();
    }
}
