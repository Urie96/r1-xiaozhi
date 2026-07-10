package org.eclipse.paho.client.mqttv3.b;

import java.io.File;
import java.io.FilenameFilter;

/* JADX INFO: loaded from: classes.dex */
class c implements FilenameFilter {
    c() {
    }

    @Override // java.io.FilenameFilter
    public boolean accept(File file, String str) {
        return str.endsWith(".msg");
    }
}
