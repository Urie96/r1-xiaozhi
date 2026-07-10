package com.unisound.vui.util.upload.user;

import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public interface Uploader {
    void upload(JSONObject jSONObject, UploaderListener uploaderListener);
}
