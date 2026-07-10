package com.unisound.client;

/* JADX INFO: loaded from: classes.dex */
public interface FourMicAudioManagerListener {
    void onError(String str);

    void onEvent(int i);

    void onRecordingData(byte[] bArr, int i);
}
