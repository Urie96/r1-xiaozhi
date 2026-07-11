package com.phicomm.speaker.device.custom.handler;

import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiBridgeManager;
import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiSettings;
import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiStreamingSession;
import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiTurn;
import com.unisound.vui.engine.ANTEngineOption;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.handler.SessionRegister;
import com.unisound.vui.handler.SimpleUserEventInboundHandler;
import com.unisound.vui.util.ExoConstants;
import com.unisound.vui.util.LogMgr;

import java.util.concurrent.atomic.AtomicInteger;

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
    private static final int CONTINUOUS_ASR_MAX_DURATION_MS = 60000;

    private ANTHandlerContext ctx;
    private XiaoZhiBridgeManager bridgeManager;
    private XiaoZhiSettings settings;
    private XiaoZhiStreamingSession session;
    private volatile boolean interrupted;
    private volatile boolean active;
    private volatile boolean playingRemoteAudio;
    private final AtomicInteger enterAsrDelayGeneration = new AtomicInteger();

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
        this.settings = this.bridgeManager.settings();
        this.bridgeManager.registerAntContext(ctx);
        this.bridgeManager.startLocalHttpServer();
        com.unisound.vui.common.config.ANTConfigPreference.asrVadTimeoutFrontSil = this.settings.vadFrontSilenceMs;
        com.unisound.vui.common.config.ANTConfigPreference.asrVadTimeoutBackSil = this.settings.vadBackSilenceMs;
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
    public void userEventTriggered(Object evt, ANTHandlerContext ctx) throws Exception {
        if (evt instanceof NLU) {
            NLU nlu = (NLU) evt;
            if (!SName.ERROR_REPORT.equals(nlu.getService())) {
                String sessionId = sessionIdFrom(nlu);
                if (!isValidSessionId(sessionId)) {
                    LogMgr.e(TAG, "swallow local nlu without interrupt: " + sessionId + ", service=" + nlu.getService());
                    if (!active && !playingRemoteAudio && session == null) {
                        enterWakeup(ctx);
                        reset();
                    }
                    return;
                }
            }
        }
        super.userEventTriggered(evt, ctx);
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
            return false;
        }
        return true;
    }

    @Override
    public void eventReceived(final NLU evt, final ANTHandlerContext ctx) throws Exception {
        final String uuid = sessionIdFrom(evt);
        if (!isValidSessionId(uuid)) {
            LogMgr.d(TAG, "local nlu ignored: " + uuid + ", service=" + evt.getService());
            return;
        }

        super.eventReceived(evt, ctx);
        this.ctx = ctx;
        this.interrupted = false;
        this.active = true;
        this.playingRemoteAudio = false;

        LogMgr.d(TAG, "session uuid: " + uuid);

        cancelCurrentSession();
        ctx.stopWakeup();
        ctx.stopASR();
        configureContinuousAsrTimeouts(ctx);

        if (bridgeManager == null) {
            bridgeManager = XiaoZhiBridgeManager.get(ctx.androidContext());
            bridgeManager.startLocalHttpServer();
        }
        final int serverCloseGeneration = bridgeManager.serverCloseGeneration();
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
                        if (settings != null && settings.multiTurnEnabled) {
                            enterASRAfterPlaybackSettled(serverCloseGeneration);
                        } else {
                            LogMgr.d(TAG, "single-turn mode, enter wakeup");
                            if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                                enterWakeup(XiaoZhiHandler.this.ctx);
                            }
                            reset();
                        }
                    }

                    @Override
                    public void onNoAudio() {
                        LogMgr.d(TAG, "xiaozhi no tts audio, enter wakeup");
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            enterWakeup(XiaoZhiHandler.this.ctx);
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
                            enterWakeup(XiaoZhiHandler.this.ctx);
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
        if (settings != null && settings.multiTurnEnabled) {
            LogMgr.d(TAG, "fallback tts end, delay enter ASR");
            enterASRAfterPlaybackSettled(bridgeManager == null
                    ? 0
                    : bridgeManager.serverCloseGeneration());
            return true;
        }
        LogMgr.d(TAG, "fallback tts end, enter wakeup");
        enterWakeup(ctx);
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
                enterWakeup(ctx);
            }
            reset();
        }
    }

    @Override
    public void reset() {
        this.active = false;
        this.playingRemoteAudio = false;
        this.enterAsrDelayGeneration.incrementAndGet();
        cancelCurrentSession();
        super.reset();
    }

    private void enterASRAfterPlaybackSettled(final int serverCloseGeneration) {
        final ANTHandlerContext targetCtx = this.ctx;
        if (this.interrupted || targetCtx == null) {
            reset();
            return;
        }
        if (this.bridgeManager != null && this.bridgeManager.hasServerCloseSince(serverCloseGeneration)) {
            LogMgr.d(TAG, "server closed during playback, stay wakeup");
            enterWakeup(targetCtx);
            reset();
            return;
        }
        final int generation = this.enterAsrDelayGeneration.incrementAndGet();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(settings == null
                            ? XiaoZhiSettings.DEFAULT_ENTER_ASR_DELAY_MS
                            : settings.enterAsrDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (enterAsrDelayGeneration.get() != generation || interrupted || XiaoZhiHandler.this.ctx != targetCtx) {
                    return;
                }
                if (bridgeManager != null && bridgeManager.hasServerCloseSince(serverCloseGeneration)) {
                    LogMgr.d(TAG, "server closed before enter ASR, stay wakeup");
                    enterWakeup(targetCtx);
                    reset();
                    return;
                }
                configureContinuousAsrTimeouts(targetCtx);
                targetCtx.enterASR();
                reset();
            }
        }, "xiaozhi-enter-asr-delay");
        thread.setDaemon(true);
        thread.start();
    }

    private void enterWakeup(ANTHandlerContext ctx) {
        if (this.bridgeManager != null) {
            this.bridgeManager.closeUpstreamConnection();
        }
        ctx.enterWakeup(false);
    }

    private void configureContinuousAsrTimeouts(ANTHandlerContext ctx) {
        int frontSilenceMs = settings == null
                ? XiaoZhiSettings.DEFAULT_VAD_FRONT_SILENCE_MS
                : settings.vadFrontSilenceMs;
        int backSilenceMs = settings == null
                ? XiaoZhiSettings.DEFAULT_VAD_BACK_SILENCE_MS
                : settings.vadBackSilenceMs;
        ctx.engine().config().setOption(
                ANTEngineOption.ASR_VAD_TIMEOUT_FRONTSIL,
                frontSilenceMs);
        ctx.engine().config().setOption(
                ANTEngineOption.ASR_VAD_TIMEOUT_BACKSIL,
                backSilenceMs);
        ctx.engine().config().setOption(
                ANTEngineOption.ASR_NET_TIMEOUT,
                CONTINUOUS_ASR_MAX_DURATION_MS);
    }

    private void cancelCurrentSession() {
        XiaoZhiStreamingSession current = this.session;
        this.session = null;
        if (current != null) {
            current.cancel();
        }
    }
}
