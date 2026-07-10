package com.google.android.exoplayer2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class DefaultRenderersFactory implements RenderersFactory {
    public static final long DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS = 5000;
    public static final int EXTENSION_RENDERER_MODE_OFF = 0;
    public static final int EXTENSION_RENDERER_MODE_ON = 1;
    public static final int EXTENSION_RENDERER_MODE_PREFER = 2;
    protected static final int MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY = 50;
    private static final String TAG = "DefaultRenderersFactory";
    private final long allowedVideoJoiningTimeMs;
    private final Context context;
    private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;
    private final int extensionRendererMode;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtensionRendererMode {
    }

    public DefaultRenderersFactory(Context context) {
        this(context, null);
    }

    public DefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this(context, drmSessionManager, 0);
    }

    public DefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode) {
        this(context, drmSessionManager, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    public DefaultRenderersFactory(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        this.context = context;
        this.drmSessionManager = drmSessionManager;
        this.extensionRendererMode = extensionRendererMode;
        this.allowedVideoJoiningTimeMs = allowedVideoJoiningTimeMs;
    }

    @Override // com.google.android.exoplayer2.RenderersFactory
    public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener, AudioRendererEventListener audioRendererEventListener, TextRenderer.Output textRendererOutput, MetadataRenderer.Output metadataRendererOutput) {
        ArrayList<Renderer> renderersList = new ArrayList<>();
        buildVideoRenderers(this.context, this.drmSessionManager, this.allowedVideoJoiningTimeMs, eventHandler, videoRendererEventListener, this.extensionRendererMode, renderersList);
        buildAudioRenderers(this.context, this.drmSessionManager, buildAudioProcessors(), eventHandler, audioRendererEventListener, this.extensionRendererMode, renderersList);
        buildTextRenderers(this.context, textRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        buildMetadataRenderers(this.context, metadataRendererOutput, eventHandler.getLooper(), this.extensionRendererMode, renderersList);
        buildMiscellaneousRenderers(this.context, eventHandler, this.extensionRendererMode, renderersList);
        return (Renderer[]) renderersList.toArray(new Renderer[renderersList.size()]);
    }

    protected void buildVideoRenderers(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, long allowedVideoJoiningTimeMs, Handler eventHandler, VideoRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        Renderer renderer;
        out.add(new MediaCodecVideoRenderer(context, MediaCodecSelector.DEFAULT, allowedVideoJoiningTimeMs, drmSessionManager, false, eventHandler, eventListener, 50));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex = out.size();
            int extensionRendererIndex2 = extensionRendererMode == 2 ? extensionRendererIndex - 1 : extensionRendererIndex;
            try {
                Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
                Constructor<?> constructor = clazz.getConstructor(Boolean.TYPE, Long.TYPE, Handler.class, VideoRendererEventListener.class, Integer.TYPE);
                renderer = (Renderer) constructor.newInstance(true, Long.valueOf(allowedVideoJoiningTimeMs), eventHandler, eventListener, 50);
                int i = extensionRendererIndex2 + 1;
            } catch (ClassNotFoundException e) {
                return;
            } catch (Exception e2) {
                e = e2;
            }
            try {
                out.add(extensionRendererIndex2, renderer);
                Log.i(TAG, "Loaded LibvpxVideoRenderer.");
            } catch (ClassNotFoundException e3) {
            } catch (Exception e4) {
                e = e4;
                throw new RuntimeException(e);
            }
        }
    }

    protected void buildAudioRenderers(Context context, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, AudioProcessor[] audioProcessors, Handler eventHandler, AudioRendererEventListener eventListener, int extensionRendererMode, ArrayList<Renderer> out) {
        int extensionRendererIndex;
        int extensionRendererIndex2;
        int extensionRendererIndex3;
        int extensionRendererIndex4;
        Renderer renderer;
        out.add(new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT, drmSessionManager, true, eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors));
        if (extensionRendererMode != 0) {
            int extensionRendererIndex5 = out.size();
            int extensionRendererIndex6 = extensionRendererMode == 2 ? extensionRendererIndex5 - 1 : extensionRendererIndex5;
            try {
                Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer");
                Constructor<?> constructor = clazz.getConstructor(Handler.class, AudioRendererEventListener.class, AudioProcessor[].class);
                Renderer renderer2 = (Renderer) constructor.newInstance(eventHandler, eventListener, audioProcessors);
                extensionRendererIndex = extensionRendererIndex6 + 1;
                try {
                    out.add(extensionRendererIndex6, renderer2);
                    Log.i(TAG, "Loaded LibopusAudioRenderer.");
                    extensionRendererIndex2 = extensionRendererIndex;
                } catch (ClassNotFoundException e) {
                    extensionRendererIndex2 = extensionRendererIndex;
                } catch (Exception e2) {
                    e = e2;
                    throw new RuntimeException(e);
                }
            } catch (ClassNotFoundException e3) {
                extensionRendererIndex = extensionRendererIndex6;
            } catch (Exception e4) {
                e = e4;
            }
            try {
                Class<?> clazz2 = Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer");
                Constructor<?> constructor2 = clazz2.getConstructor(Handler.class, AudioRendererEventListener.class, AudioProcessor[].class);
                Renderer renderer3 = (Renderer) constructor2.newInstance(eventHandler, eventListener, audioProcessors);
                extensionRendererIndex3 = extensionRendererIndex2 + 1;
                try {
                    out.add(extensionRendererIndex2, renderer3);
                    Log.i(TAG, "Loaded LibflacAudioRenderer.");
                    extensionRendererIndex4 = extensionRendererIndex3;
                } catch (ClassNotFoundException e5) {
                    extensionRendererIndex4 = extensionRendererIndex3;
                } catch (Exception e6) {
                    e = e6;
                    throw new RuntimeException(e);
                }
            } catch (ClassNotFoundException e7) {
                extensionRendererIndex3 = extensionRendererIndex2;
            } catch (Exception e8) {
                e = e8;
            }
            try {
                Class<?> clazz3 = Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer");
                Constructor<?> constructor3 = clazz3.getConstructor(Handler.class, AudioRendererEventListener.class, AudioProcessor[].class);
                renderer = (Renderer) constructor3.newInstance(eventHandler, eventListener, audioProcessors);
                int i = extensionRendererIndex4 + 1;
            } catch (ClassNotFoundException e9) {
                return;
            } catch (Exception e10) {
                e = e10;
            }
            try {
                out.add(extensionRendererIndex4, renderer);
                Log.i(TAG, "Loaded FfmpegAudioRenderer.");
            } catch (ClassNotFoundException e11) {
            } catch (Exception e12) {
                e = e12;
                throw new RuntimeException(e);
            }
        }
    }

    protected void buildTextRenderers(Context context, TextRenderer.Output output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new TextRenderer(output, outputLooper));
    }

    protected void buildMetadataRenderers(Context context, MetadataRenderer.Output output, Looper outputLooper, int extensionRendererMode, ArrayList<Renderer> out) {
        out.add(new MetadataRenderer(output, outputLooper));
    }

    protected void buildMiscellaneousRenderers(Context context, Handler eventHandler, int extensionRendererMode, ArrayList<Renderer> out) {
    }

    protected AudioProcessor[] buildAudioProcessors() {
        return new AudioProcessor[0];
    }
}
