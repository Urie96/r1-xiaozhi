package com.phicomm.speaker.device.custom.handler;

import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiBridgeManager;
import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiStreamingSession;
import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiTurn;
import com.unisound.vui.engine.ANTEngineOption;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.handler.SessionRegister;
import com.unisound.vui.handler.SimpleUserEventInboundHandler;
import com.unisound.vui.util.ExoConstants;
import com.unisound.vui.util.LogMgr;

import nluparser.scheme.NLU;
import nluparser.scheme.SName;

/**
 * XiaoZhi 本机桥接 Handler：
 *
 *   唤醒 → R1 native ASR 音频 POST 到 127.0.0.1:8089
 *        → APK 内本地 HTTP 服务转发 Opus 到 xiaozhi-server-rs WebSocket
 *        → 本地 HTTP 服务返回携带 UUID 的假 NLU
 *        → XiaoZhiHandler 用 UUID 取得本轮 TTS 队列
 *        → ExoPlayer 通过自定义 DataSource 边收边播 Ogg Opus
 *        → 普通 TTS 结束后 enterASR 连续对话；服务端 WebSocket close 才退回未唤醒
 */
public class XiaoZhiHandler extends SimpleUserEventInboundHandler<NLU> {
    private static final String TAG = "XiaoZhiHandler";

    private ANTHandlerContext ctx;
    private XiaoZhiBridgeManager bridgeManager;
    private XiaoZhiStreamingSession session;
    private volatile boolean interrupted;
    private volatile boolean active;
    private volatile boolean playingRemoteAudio;

    public XiaoZhiHandler() {
        this.sessionName = SessionRegister.SESSION_CHAT;
    }

    @Override
    public void initPriority() {
        setPriority(1000);
    }

    @Override
    public boolean onASREventEngineInitDone(ANTHandlerContext ctx) {
        this.ctx = ctx;
        this.bridgeManager = XiaoZhiBridgeManager.get(ctx.androidContext());
        this.bridgeManager.registerAntContext(ctx);
        this.bridgeManager.startLocalHttpServer();
        com.unisound.vui.common.config.ANTConfigPreference.asrVadTimeoutBackSil = 400;
        setNativeTRUrl(XiaoZhiBridgeManager.localTrAddr());
        return super.onASREventEngineInitDone(ctx);
    }

    private static String sessionIdFrom(NLU evt) {
        if (evt == null) {
            return null;
        }
        String text = evt.getText();
        if (text != null && !(text = text.trim()).isEmpty()) {
            return text;
        }
        String responseId = evt.getResponseId();
        if (responseId != null && !(responseId = responseId.trim()).isEmpty()) {
            return responseId;
        }
        return null;
    }

    private static boolean isValidSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8 || sessionId.length() > 128) {
            return false;
        }
        for (int i = 0; i < sessionId.length(); i++) {
            char c = sessionId.charAt(i);
            if (!((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '-' || c == '_' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    private void setNativeTRUrl(String addr) {
        try {
            com.unisound.vui.common.config.ANTConfigPreference.sVersionType = "develop";
            java.lang.reflect.Field f = com.unisound.vui.common.config.ANTConfigPreference.class
                    .getDeclaredField("sDevTRServer");
            f.setAccessible(true);
            f.set(null, addr);
            LogMgr.d(TAG, "ANTConfigPreference TR URL set to " + addr);
        } catch (Exception e) {
            LogMgr.e(TAG, "failed to set ANTConfigPreference TR URL: " + e.getMessage());
        }

        try {
            java.lang.reflect.Field cField = com.unisound.vui.engine.NativeANTEngine.class
                    .getDeclaredField("c");
            cField.setAccessible(true);
            String oldUrl = (String) cField.get(null);
            cField.set(null, addr);
            LogMgr.d(TAG, "NativeANTEngine.c updated: " + oldUrl + " → " + addr);
        } catch (Exception e) {
            LogMgr.e(TAG, "failed to set NativeANTEngine.c: " + e.getMessage());
        }
    }

    @Override
    public boolean acceptInboundEvent0(NLU evt) throws Exception {
        if (evt == null) {
            return false;
        }
        if (SName.ERROR_REPORT.equals(evt.getService())) {
            LogMgr.e(TAG, "ignore ASR error nlu, code=" + evt.getCode() + ", text=" + evt.getText());
            return false;
        }
        String sessionId = sessionIdFrom(evt);
        if (!isValidSessionId(sessionId)) {
            LogMgr.e(TAG, "ignore invalid xiaozhi session id: " + sessionId + ", service=" + evt.getService());
            return false;
        }
        return true;
    }

    @Override
    public void eventReceived(final NLU evt, final ANTHandlerContext ctx) throws Exception {
        super.eventReceived(evt, ctx);
        this.ctx = ctx;
        this.interrupted = false;
        this.active = true;
        this.playingRemoteAudio = false;

        final String uuid = sessionIdFrom(evt);
        LogMgr.d(TAG, "session uuid: " + uuid);

        cancelCurrentSession();
        ctx.stopWakeup();
        ctx.stopASR();
        ctx.engine().config().setOption(ANTEngineOption.ASR_VAD_TIMEOUT_BACKSIL, 400);

        if (bridgeManager == null) {
            bridgeManager = XiaoZhiBridgeManager.get(ctx.androidContext());
            bridgeManager.startLocalHttpServer();
        }
        XiaoZhiTurn turn = bridgeManager.takeTurn(uuid);
        if (turn == null) {
            LogMgr.e(TAG, "missing xiaozhi turn: " + uuid);
            ctx.playTTS("小智服务暂时不可用，请稍后再试");
            return;
        }

        XiaoZhiStreamingSession next = new XiaoZhiStreamingSession(
                ctx.androidContext(),
                bridgeManager,
                new XiaoZhiStreamingSession.Callback() {
                    @Override
                    public void onPlaybackStarted() {
                        LogMgr.d(TAG, "xiaozhi playback started");
                        playingRemoteAudio = true;
                    }

                    @Override
                    public void onPlaybackEnded() {
                        LogMgr.d(TAG, "xiaozhi playback ended");
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            XiaoZhiHandler.this.ctx.enterASR();
                        }
                        reset();
                    }

                    @Override
                    public void onNoAudio() {
                        LogMgr.d(TAG, "xiaozhi no tts audio, enter ASR");
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            XiaoZhiHandler.this.ctx.enterASR();
                        }
                        reset();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        LogMgr.e(TAG, errorMessage);
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            XiaoZhiHandler.this.ctx.playTTS("小智服务暂时不可用，请稍后再试");
                        }
                    }

                    @Override
                    public void onConversationClosed() {
                        LogMgr.d(TAG, "xiaozhi conversation closed, enter wakeup");
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            XiaoZhiHandler.this.ctx.enterWakeup(false);
                        }
                        reset();
                    }
                }
        );
        this.session = next;
        next.start(turn);
    }

    @Override
    public boolean onTTSEventPlayingEnd(ANTHandlerContext ctx) {
        if (!this.eventReceived) {
            return super.onTTSEventPlayingEnd(ctx);
        }
        LogMgr.d(TAG, "fallback tts end, enter ASR");
        ctx.enterASR();
        reset();
        return true;
    }

    @Override
    public void doInterrupt(ANTHandlerContext ctx, String interruptType) {
        if (this.eventReceived || this.active || this.playingRemoteAudio) {
            LogMgr.d(TAG, "doInterrupt: " + interruptType);
            this.interrupted = true;
            ctx.cancelTTS();
            cancelCurrentSession();
            if (!ExoConstants.DO_ONE_SHOT_INTERRUPT.equals(interruptType)) {
                ctx.enterWakeup(false);
            }
            reset();
        }
    }

    @Override
    public void reset() {
        this.active = false;
        this.playingRemoteAudio = false;
        cancelCurrentSession();
        super.reset();
    }

    private void cancelCurrentSession() {
        XiaoZhiStreamingSession current = this.session;
        this.session = null;
        if (current != null) {
            current.cancel();
        }
    }
}
