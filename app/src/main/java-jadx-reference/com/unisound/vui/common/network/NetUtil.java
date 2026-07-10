package com.unisound.vui.common.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.unisound.b.f;
import com.unisound.vui.util.LogMgr;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;

/* JADX INFO: loaded from: classes.dex */
public class NetUtil {
    private static final String TAG = "NetUtil";
    private static NetUtil mNetUtil;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private NetworkInfo mNetworkInfo;

    private NetUtil(Context context) {
        this.mContext = context.getApplicationContext();
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
    }

    public static NetUtil getInstante(Context context) {
        if (mNetUtil == null) {
            mNetUtil = new NetUtil(context);
        }
        return mNetUtil;
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x0046  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static String getLocalIpAddress() {
        String string;
        SocketException socketException;
        String str = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            loop0: while (networkInterfaces.hasMoreElements()) {
                try {
                    Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddressNextElement = inetAddresses.nextElement();
                        if (!inetAddressNextElement.isLoopbackAddress()) {
                            string = inetAddressNextElement.getHostAddress().toString();
                            if (!InetAddressUtils.isIPv4Address(string) || string.equals("::1")) {
                                string = str;
                            } else if (string.startsWith("192")) {
                                break loop0;
                            }
                        }
                        str = string;
                    }
                } catch (SocketException e) {
                    string = str;
                    socketException = e;
                    socketException.printStackTrace();
                    LogMgr.e(TAG, "===>>get current ipAddress:" + socketException.toString());
                }
            }
            string = str;
        } catch (SocketException e2) {
            string = null;
            socketException = e2;
        }
        LogMgr.d(TAG, "===>>get current ipAddress:" + string);
        return string;
    }

    public static String getNetWorkType(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            String typeName = activeNetworkInfo.getTypeName();
            if (typeName.equalsIgnoreCase("WIFI") || typeName.equalsIgnoreCase("MOBILE")) {
                return typeName;
            }
        }
        return "UNKNOW";
    }

    public static String getOutNetIp() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://www.cmyip.com/").openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, f.b));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        inputStream.close();
                        return sb.toString();
                    }
                    sb.append(line + "\n");
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return "";
    }

    public static boolean isMobileConnected(Context context) {
        NetworkInfo networkInfo;
        if (context == null || (networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(0)) == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo activeNetworkInfo;
        if (context == null || (activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null) {
            return false;
        }
        return activeNetworkInfo.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        NetworkInfo networkInfo;
        if (context == null || (networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1)) == null) {
            return false;
        }
        return networkInfo.isAvailable();
    }

    public boolean getConnectState() {
        if (this.mConnectivityManager != null) {
            this.mNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
            if (this.mNetworkInfo != null) {
                return this.mNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public void release() {
        this.mContext = null;
        this.mConnectivityManager = null;
        this.mNetworkInfo = null;
    }
}
