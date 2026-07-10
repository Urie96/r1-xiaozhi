package com.phicomm.speaker.device.ipc;

/* JADX INFO: loaded from: classes.dex */
public interface IpcRegister {
    void registerReceiver(IpcReceiver ipcReceiver, int i);

    void unRegisterReceiver(IpcReceiver ipcReceiver);
}
