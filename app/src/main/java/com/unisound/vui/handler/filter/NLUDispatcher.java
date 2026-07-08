package com.unisound.vui.handler.filter;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.phicomm.speaker.device.R;
import com.unisound.vui.common.config.ANTConfigPreference;
import com.unisound.vui.common.network.NetUtil;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.handler.ANTEventDispatcher;
import com.unisound.vui.util.*;

import java.util.List;
import nluparser.MixtureProcessor;
import nluparser.NluProcessor;
import nluparser.scheme.Intent;
import nluparser.scheme.LocalASR;
import nluparser.scheme.Mixture;
import nluparser.scheme.NLU;
import nluparser.scheme.Result;
import nluparser.scheme.SName;
import org.json.JSONException;
import org.json.JSONObject;

public final class NLUDispatcher extends ANTEventDispatcher {

    /* renamed from: a  reason: collision with root package name */
    private boolean f423a;
    private boolean b;
    private final MixtureProcessor c;
    private NluProcessor d;
    private Handler e = new Handler();
    private Runnable f;

    public NLUDispatcher(MixtureProcessor mixtureProcessor) {
        this.c = mixtureProcessor;
        this.d = new NluProcessor.Builder().registerTypeMapper(SName.ERROR_REPORT, new TypeToken<NLU<Intent.NullIntent, Result.NullResult>>() {
            /* class com.unisound.vui.handler.filter.NLUDispatcher.AnonymousClass1 */
        }.getType()).build();
    }

    private void a() {
        if (this.f != null) {
            LogMgr.d("NLUDispatcher", "stop asr or nlu result timeout task");
            this.e.removeCallbacks(this.f);
            this.f = null;
        }
    }

    private void a(ANTHandlerContext aNTHandlerContext, boolean z) {
        aNTHandlerContext.engine().attr(AttributeKey.valueOf(NLUDispatcher.class, "RECOGNITION_HANDLED")).set(Boolean.valueOf(z));
    }

    private boolean a(float f2) {
        return f2 > ANTConfigPreference.FUNCTION_WAKEUP_BENCHMARK;
    }

    private boolean a(Context context, String str) {
        return UserPerferenceUtil.getCmopetitionWord(context).contains(str);
    }

    private boolean a(ANTHandlerContext aNTHandlerContext) {
        if (b(aNTHandlerContext)) {
            this.f423a = true;
            NLU c2 = c("bluetooth_error");
            c2.setText(aNTHandlerContext.androidContext().getString(R.string.tts_music_change_no_supported));
            c(aNTHandlerContext, c2);
            return true;
        } else if (c(aNTHandlerContext) && !this.b) {
            return false;
        } else {
            this.f423a = true;
            NLU c3 = c("-90002");
            c3.setText("no network");
            c(aNTHandlerContext, c3);
            return true;
        }
    }

    private boolean a(ANTHandlerContext aNTHandlerContext, String str, Mixture<Intent, Result> mixture) {
        NLU<Intent, Result> nlu = mixture.getNluList().get(0);
        if (a(mixture, nlu)) {
            LogMgr.d("NLUDispatcher", "handleNetFilterService return filter service ...");
            return false;
        }
        this.f423a = true;
        nlu.setLocalNLU(false);
        nlu.setAsrResult(str);
        b(aNTHandlerContext, nlu);
        return true;
    }

    private boolean a(ANTHandlerContext aNTHandlerContext, String str, NLU nlu) {
        if (a(aNTHandlerContext, nlu)) {
            return false;
        }
        this.f423a = true;
        nlu.setLocalNLU(true);
        nlu.setAsrResult(str);
        c(aNTHandlerContext, nlu);
        return true;
    }

    private boolean a(ANTHandlerContext aNTHandlerContext, LocalASR localASR) {
        aNTHandlerContext.fireUserEventTriggered(localASR);
        return true;
    }

    private boolean a(ANTHandlerContext aNTHandlerContext, NLU nlu) {
        return a(nlu.getService()) && !this.b && !b(aNTHandlerContext);
    }

    private boolean a(String str) {
        return a.a(str);
    }

    private boolean a(LocalASR localASR, NLU nlu) {
        return nlu.getResponseCode() == 0 && localASR.getScore() > ANTConfigPreference.recognizerScore;
    }

    private boolean a(Mixture<Intent, Result> mixture) {
        return mixture.getNetASRList() != null && "full".equals(mixture.getNetASRList().get(0).getResultType());
    }

    private boolean a(Mixture<Intent, Result> mixture, NLU nlu) {
        return a(nlu.getService()) && !a(mixture);
    }

    private void b(ANTHandlerContext aNTHandlerContext, NLU nlu) {
        LogMgr.d("NLUDispatcher", "preHandleNetNlu nlu :" + nlu);
        if (TextUtils.isEmpty(nlu.getText())) {
            c(aNTHandlerContext, c("-63551"));
        } else {
            c(aNTHandlerContext, nlu);
        }
    }

    private boolean b(float f2) {
        return f2 > ANTConfigPreference.effectWakeupBenchmark;
    }

    private boolean b(Context context, String str) {
        return !UserPerferenceUtil.getMainWakeupWord(context).contains(str);
    }

    private boolean b(ANTHandlerContext aNTHandlerContext) {
        return ((Integer) aNTHandlerContext.engine().unsafe().getOption(1001)).intValue() == 2;
    }

    private boolean b(String str) {
        return a(str);
    }

    private boolean b(Mixture<Intent, Result> mixture) {
        List<NLU<Intent, Result>> nluList = mixture.getNluList();
        return (nluList == null || nluList.size() == 0) ? false : true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NLU c(String str) {
        NLU nlu = new NLU();
        nlu.setService(SName.ERROR_REPORT);
        nlu.setCode(str);
        return nlu;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void c(ANTHandlerContext aNTHandlerContext, NLU nlu) {
        a();
        aNTHandlerContext.pipeline().fireASREvent(1102);
        a(aNTHandlerContext, true);
        aNTHandlerContext.cancelEngine();
        aNTHandlerContext.fireUserEventTriggered(nlu);
        this.b = false;
        this.f423a = false;
    }

    private boolean c(Context context, String str) {
        return UserPerferenceUtil.getMainWakeupWord(context).contains(str);
    }

    private boolean c(ANTHandlerContext aNTHandlerContext) {
        return NetUtil.isNetworkConnected(aNTHandlerContext.androidContext());
    }

    private void d(final ANTHandlerContext aNTHandlerContext) {
        if (this.f == null) {
            LogMgr.d("NLUDispatcher", "start asr or nlu result timeout task");
            this.f = new Runnable() {
                /* class com.unisound.vui.handler.filter.NLUDispatcher.AnonymousClass2 */

                @Override
                public void run() {
                    LogMgr.d("NLUDispatcher", "process asr or nlu result timeout, dispatch network error nlu ");
                    NLU c =    c("-90002");
                    c.setText("no network");
                       c(aNTHandlerContext, c);
                }
            };
            this.e.postDelayed(this.f, 15000);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onASRError(ANTHandlerContext ctx, String error) {
        d(ctx);
        LogMgr.i("NLUDispatcher", "-onASRError-" + error);
        JSONObject parseToJSONObject = JsonTool.parseToJSONObject(error);
        String jsonValue = JsonTool.getJsonValue(parseToJSONObject, "errorCode");
        String jsonValue2 = JsonTool.getJsonValue(parseToJSONObject, "errorMsg");
        if (b(jsonValue)) {
            this.b = true;
            return false;
        }
        this.f423a = true;
        NLU c2 = c(jsonValue);
        c2.setText(jsonValue2);
        c(ctx, c2);
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onASREventCancel(ANTHandlerContext ctx) {
        a();
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onASREventRecordingStart(ANTHandlerContext ctx) {
        LogMgr.i("NLUDispatcher", "onASREventRecordingStart");
        a();
        a(ctx, false);
        return super.onASREventRecordingStart(ctx);
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onASRResultLocal(ANTHandlerContext ctx, String result) {
        LogMgr.d("NLUDispatcher", "onASRResultLocal:" + result);
        if (this.f423a) {
            LogMgr.e("NLUDispatcher", "result has handled, local nlu handle return");
            return true;
        }
        d(ctx);
        LocalASR localASR = this.c.from(result).getLocalASRList().get(0);
        NLU from = this.d.from(localASR.getRecognitionResult());
        if (!a(localASR, from)) {
            return a(ctx);
        }
        if (a(ctx.androidContext(), from.getText())) {
            return false;
        }
        return a(ctx, result, from);
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onASRResultNet(ANTHandlerContext ctx, String result) {
        String str;
        LogMgr.d("NLUDispatcher", "onASRResultNet:" + result);
        if (this.f423a) {
            LogMgr.e("NLUDispatcher", "result has handled, net nlu handle return");
            return true;
        }
        d(ctx);
        Mixture<Intent, Result> from = this.c.from(result);
        if (from == null) {
            try {
                str = (String) ((JSONObject) JsonTool.parseToJSONObject(result).getJSONArray("net_nlu").get(0)).get("text");
            } catch (JSONException e2) {
                LogMgr.e("NLUDispatcher", "unsupported domain 解析用户说的话出错 : " + e2.getMessage());
                str = result;
            }
            LogMgr.w("NLUDispatcher", "unsupported domain " + result);
            this.f423a = true;
            NLU c2 = c("unsupportedDomain");
            c2.setText(str);
            b(ctx, c2);
            return true;
        } else if (b(from)) {
            return a(ctx, result, from);
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.unisound.vui.handler.ANTEventDispatcher
    public boolean onWakeupResult(ANTHandlerContext ctx, String result) {
        LogMgr.d("NLUDispatcher", "onWakeupResult:" + result);
        LocalASR localASR = this.c.from(result).getLocalASRList().get(0);
        String trim = localASR.getRecognitionResult().trim();
        if (a(ctx.androidContext(), trim)) {
            LogMgr.d("NLUDispatcher", trim + " is CompetitionWord, return");
            return true;
        } else if (b(ctx.androidContext(), trim) && a(localASR.getScore())) {
            LogMgr.d("NLUDispatcher", "isFunctionWakeupWord : wakeupResult:" + trim + ";success core:" + localASR.getScore());
            return a(ctx, localASR);
        } else if (!c(ctx.androidContext(), trim) || !b(localASR.getScore())) {
            return true;
        } else {
            LogMgr.d("NLUDispatcher", "isMainWakeupWord : wakeupResult:" + trim + ";success core:" + localASR.getScore());
            return false;
        }
    }

    @Override // com.unisound.vui.engine.ANTInboundHandler, com.unisound.vui.engine.ANTInboundHandlerAdapter
    public void userEventTriggered(Object evt, ANTHandlerContext ctx) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }
}
