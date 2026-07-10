package com.kaolafm.sdk.client;

import android.content.ComponentName;

/* JADX INFO: loaded from: classes.dex */
public interface IServiceConnection {
    void onServiceConnected(ComponentName componentName);

    void onServiceDisconnected(ComponentName componentName);
}
