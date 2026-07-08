package com.phicomm.speaker.device.custom.handler;

import com.phicomm.speaker.device.custom.xiaozhi.XiaoZhiStreamingSession;
import com.unisound.vui.engine.ANTEngineOption;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.handler.SessionRegister;
import com.unisound.vui.handler.SimpleUserEventInboundHandler;
import com.unisound.vui.util.ExoConstants;
import com.unisound.vui.util.LogMgr;

import nluparser.scheme.NLU;
import nluparser.scheme.SName;

/**
 * XiaoZhi 接入 Handler：
 *
 *   唤醒 → R1 原厂 ASR → XiaoZhiHandler 拦截 ASR 文本
 *        → xiaozhi-server-rs websocket recognize(agent_id=zhuzhu)
 *        → 服务端 Opus binary frame 实时封装 Ogg page
 *        → ExoPlayer 通过阻塞 DataSource 边收边播
 *        → 播完直接 enterASR 连续对话
 */
public class XiaoZhiHandler extends SimpleUserEventInboundHandler<NLU> {
    private static final String TAG = "XiaoZhiHandler";
    private static final String SERVER_BASE_URL = "http://home.lubui.com:3116";
    private static final String AGENT_ID = "zhuzhu";

    private ANTHandlerContext ctx;
    private XiaoZhiStreamingSession session;
    private volatile boolean interrupted;
    private volatile boolean active;
    private volatile boolean playingRemoteAudio;

    public XiaoZhiHandler() {
        this.sessionName = SessionRegister.SESSION_CHAT;
    }

    @Override
    public void initPriority() {
        // 抢在所有原厂业务 Handler 前，避免天气/音乐/闲聊等下游消费。
        setPriority(1000);
    }

    @Override
    public boolean onASREventEngineInitDone(ANTHandlerContext ctx) {
        this.ctx = ctx;
        return super.onASREventEngineInitDone(ctx);
    }

    @Override
    public boolean acceptInboundEvent0(NLU evt) throws Exception {
        if (evt == null || evt.getText() == null || evt.getText().trim().isEmpty()) {
            return false;
        }
        String s = evt.getService();
        // 放行原厂 DefaultSettingHandler 处理的意图(系统设置/音量调节/蓝牙开关/空气检测/健康等)
        // 返回 false = 不消费, 事件继续往后传给下游原厂 handler
        if (SName.SETTING.equals(s) || SName.SETTING_COMMON.equals(s)
                || SName.GLOBAL_CMD.equals(s) || SName.HEALTH_INFO.equals(s)
                || SName.SETTING_AIR.equals(s)) {
            return false;
        }
        // 其余意图(天气/音乐/闲聊/未知等)一律走小智
        return true;
    }

    @Override
    public void eventReceived(final NLU evt, final ANTHandlerContext ctx) throws Exception {
        super.eventReceived(evt, ctx);
        this.ctx = ctx;
        this.interrupted = false;
        this.active = true;
        this.playingRemoteAudio = false;

        final String text = evt.getText().trim();
        LogMgr.d(TAG, "recognize text: \"" + text + "\"");

        cancelCurrentSession();
        ctx.stopWakeup();
        ctx.stopASR();
        ctx.engine().config().setOption(ANTEngineOption.ASR_VAD_TIMEOUT_BACKSIL, 400);

        XiaoZhiStreamingSession next = new XiaoZhiStreamingSession(
                ctx.androidContext(),
                SERVER_BASE_URL,
                AGENT_ID,
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
                    public void onError(String errorMessage) {
                        LogMgr.e(TAG, errorMessage);
                        playingRemoteAudio = false;
                        session = null;
                        if (!interrupted && XiaoZhiHandler.this.ctx != null) {
                            XiaoZhiHandler.this.ctx.playTTS("小智服务暂时不可用，请稍后再试");
                        }
                    }
                }
        );
        this.session = next;
        next.start(text);
    }

    @Override
    public boolean onTTSEventPlayingEnd(ANTHandlerContext ctx) {
        if (!this.eventReceived) {
            return super.onTTSEventPlayingEnd(ctx);
        }
        // 只有错误兜底 TTS 会走这里；正常小智音频由流式播放器 callback 收尾。
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
