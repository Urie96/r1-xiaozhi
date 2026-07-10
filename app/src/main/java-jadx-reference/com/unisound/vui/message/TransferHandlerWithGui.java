package com.unisound.vui.message;

/* JADX INFO: loaded from: classes.dex */
public interface TransferHandlerWithGui {
    void preReceiveMsg(MessageBeanHandlerGui<?> messageBeanHandlerGui);

    void receiveMsg(MessageBeanHandlerGui<?> messageBeanHandlerGui);

    void registerMessageHandlerWithGui(TransferHandlerWithGui transferHandlerWithGui);

    void sendMsg(MessageBeanHandlerGui<?> messageBeanHandlerGui, TransferHandlerWithGui transferHandlerWithGui);
}
