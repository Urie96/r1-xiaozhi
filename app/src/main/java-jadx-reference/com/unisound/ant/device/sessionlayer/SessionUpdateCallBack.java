package com.unisound.ant.device.sessionlayer;

import com.unisound.ant.device.bean.SessionData;
import com.unisound.ant.device.service.ActionResponse;

/* JADX INFO: loaded from: classes.dex */
public interface SessionUpdateCallBack {
    void onCloudSessionResponse(ActionResponse actionResponse);

    void onSessionDataUpdate(String str, SessionData sessionData);

    void onSessionToSceneControl(String str);
}
