package com.unisound.vui.transport;

/* JADX INFO: loaded from: classes.dex */
public interface MessageCodec {
    b decode(String str);

    String encode(b bVar);
}
