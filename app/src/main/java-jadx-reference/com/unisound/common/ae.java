package com.unisound.common;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/* JADX INFO: loaded from: classes.dex */
public class ae {

    /* JADX INFO: renamed from: a, reason: collision with root package name */
    public static final String f236a = "Network";
    public static final String b = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String c = "cn.yunzhisheng.intent.action.CONNECTIVITY_CHANGE";
    public static final String d = "TYPE_NULL_POINT";
    public static final String e = "TYPE_UNCONNECT";
    public static final String f = "TYPE_WIFI";
    public static final String g = "TYPE_GPRS";
    private static final int h = 0;
    private static final int i = 1;
    private static int j = 1;

    private static String a(int i2) {
        return (i2 & 255) + "." + ((i2 >> 8) & 255) + "." + ((i2 >> 16) & 255) + "." + ((i2 >> 24) & 255);
    }

    public static String a(Context context) {
        try {
            int ipAddress = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getIpAddress();
            return ipAddress == 0 ? b() : a(ipAddress);
        } catch (Exception e2) {
            y.c("Network getIp error");
            return "";
        }
    }

    public static boolean a() {
        return j > 0;
    }

    public static String b() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        while (networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddressNextElement = inetAddresses.nextElement();
                if (!inetAddressNextElement.isLoopbackAddress() && (inetAddressNextElement instanceof Inet4Address)) {
                    return inetAddressNextElement.getHostAddress().toString();
                }
                return "0.0.0.0";
            }
        }
        return "0.0.0.0";
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x001c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static boolean b(Context context) {
        boolean z;
        if (context == null) {
            return false;
        }
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                z = activeNetworkInfo.isConnected();
            }
            return z;
        } catch (Exception e2) {
            y.c("Network:isNetworkConnected error");
            return false;
        }
    }

    public static boolean c(Context context) {
        int i2 = j;
        if (d(context)) {
            j = 1;
        } else {
            j = 0;
        }
        if (i2 != j && context != null) {
            context.sendBroadcast(new Intent("cn.yunzhisheng.intent.action.CONNECTIVITY_CHANGE"));
        }
        return j > 0;
    }

    public static boolean d(Context context) {
        if (context == null) {
            return false;
        }
        try {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.isConnected()) {
                    return true;
                }
            }
        } catch (Exception e2) {
            y.c("Network:isNetworkConnected error");
        }
        return false;
    }

    public static String e(Context context) {
        NetworkInfo activeNetworkInfo;
        String str;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null) {
                return "TYPE_NULL_POINT";
            }
            if (!activeNetworkInfo.isConnected()) {
                return "TYPE_UNCONNECT";
            }
            switch (activeNetworkInfo.getType()) {
                case 0:
                    str = "TYPE_GPRS";
                    break;
                case 1:
                    str = "TYPE_WIFI";
                    break;
                default:
                    str = "TYPE_NULL_POINT";
                    break;
            }
            return str;
        } catch (Exception e2) {
            y.c("Network:judgeCurrentNetTpe error");
            return "TYPE_NULL_POINT";
        }
    }
}
