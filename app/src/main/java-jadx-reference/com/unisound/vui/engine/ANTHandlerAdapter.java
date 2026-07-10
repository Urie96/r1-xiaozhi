package com.unisound.vui.engine;

/* JADX INFO: loaded from: classes.dex */
public abstract class ANTHandlerAdapter implements ANTHandler {
    @Override // com.unisound.vui.engine.ANTHandler
    public void exceptionCaught(ANTHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
