package com.phicomm.speaker.device.custom.handler;

import com.unisound.vui.engine.ANTEngineOption;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.handler.SessionRegister;
import com.unisound.vui.handler.SimpleUserEventInboundHandler;
import com.unisound.vui.util.ExoConstants;
import com.unisound.vui.util.LogMgr;

import nluparser.scheme.NLU;

/**
 * 鹦鹉 Handler(唯一魔改点):
 *
 * 拦截 *所有* NLU 事件(即用户说出的每一句话), 取 ASR 识别文本,
 * 直接 ctx.playTTS(text) 原样复述 —— 用户说什么, TTS 就说什么。
 *
 * 优先级 1000, 高于一切业务 Handler(weather/music/chat/...), 因此会先抢到事件并消费,
 * 其余 Handler 永远收不到 NLU 事件, 等价于"只做回声"。
 *
 * 流程(多轮):
 *   唤醒 → ASR → NLU → EchoHandler.eventReceived → stopWakeup/stopASR → playTTS(原话)
 *        → onTTSEventPlayingEnd → ctx.enterASR() 直接再次听写(免唤醒词)
 *        → 循环回声, 形成连续多轮
 */
public class EchoHandler extends SimpleUserEventInboundHandler<NLU> {
    private static final String TAG = "EchoHandler";

    private ANTHandlerContext ctx;

    public EchoHandler() {
        this.sessionName = SessionRegister.SESSION_CHAT;
    }

    @Override
    public void initPriority() {
        // 全局最高优先级, 抢在所有业务 Handler 之前
        setPriority(1000);
    }

    @Override
    public boolean onASREventEngineInitDone(ANTHandlerContext ctx) {
        this.ctx = ctx;
        return super.onASREventEngineInitDone(ctx);
    }

    @Override
    public boolean acceptInboundEvent0(NLU evt) throws Exception {
        // 任何带识别文本的 NLU 都消费(即用户说的每一句话)
        return evt != null && evt.getText() != null && !evt.getText().isEmpty();
    }

    @Override
    public void eventReceived(final NLU evt, final ANTHandlerContext ctx) throws Exception {
        super.eventReceived(evt, ctx);
        final String text = evt.getText();
        LogMgr.d(TAG, "echo: \"" + text + "\"");

        // 接管会话: 停掉唤醒/ASR, 由 TTS 复述
        ctx.stopWakeup();
        ctx.stopASR();
        // 缩短 ASR 后端静默超时(默认800ms→400ms), 减少说话到回声的等待感
        ctx.engine().config().setOption(ANTEngineOption.ASR_VAD_TIMEOUT_BACKSIL, 400);
        ctx.playTTS(text);
    }

    @Override
    public boolean onTTSEventPlayingEnd(ANTHandlerContext ctx) {
        if (!this.eventReceived) {
            return super.onTTSEventPlayingEnd(ctx);
        }
        // 多轮: 复述完毕后直接再次进入 ASR 听写, 无需再说唤醒词(参考 DefaultNoteHandler).
        // 麦克风会持续开着, 下一句说出即可再次被 ASR 捕获并回声, 形成连续多轮.
        ctx.enterASR();
        return true;
    }


    @Override
    public void doInterrupt(ANTHandlerContext ctx, String interruptType) {
        if (this.eventReceived) {
            ctx.cancelTTS();
            if (!ExoConstants.DO_ONE_SHOT_INTERRUPT.equals(interruptType)) {
                ctx.enterWakeup(false);
            }
            reset();
        }
    }

}
