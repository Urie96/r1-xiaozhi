package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.unisound.vui.util.LogMgr;

import java.lang.reflect.Method;

final class XiaoZhiStreamPlayer implements ExoPlayer.EventListener {
    interface Callback {
        void onStreamReady();

        void onStreamEnded();

        void onStreamError(String errorMessage);
    }

    private static final String TAG = "XiaoZhiStreamPlayer";
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SimpleExoPlayer player;
    private Callback callback;
    private boolean released;
    private boolean readyNotified;
    private boolean endedNotified;

    XiaoZhiStreamPlayer(Context context) {
        this.context = context.getApplicationContext();
    }

    void start(final XiaoZhiStreamDataSource dataSource, final Callback callback) {
        this.callback = callback;
        this.released = false;
        this.readyNotified = false;
        this.endedNotified = false;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (released) {
                    return;
                }
                try {
                    DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
                    DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(BANDWIDTH_METER));
                    player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, buildLowLatencyLoadControl());
                    addListenerCompat();
                    player.setAudioStreamType(3);

                    DataSource.Factory factory = new DataSource.Factory() {
                        @Override
                        public DataSource createDataSource() {
                            return dataSource;
                        }
                    };
                    MediaSource source = new ExtractorMediaSource(
                            Uri.parse("xiaozhi://stream.ogg"),
                            factory,
                            new DefaultExtractorsFactory(),
                            mainHandler,
                            null
                    );
                    player.setPlayWhenReady(true);
                    player.prepare(source);
                    LogMgr.d(TAG, "stream player prepared");
                } catch (Throwable t) {
                    LogMgr.e(TAG, "start failed: " + t.toString());
                    if (!released && XiaoZhiStreamPlayer.this.callback != null) {
                        XiaoZhiStreamPlayer.this.callback.onStreamError(t.toString());
                    }
                }
            }
        });
    }

    void stop() {
        released = true;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    try {
                        removeListenerCompat();
                        player.stop();
                        player.release();
                    } catch (Throwable t) {
                        LogMgr.e(TAG, "release failed: " + t.toString());
                    }
                    player = null;
                }
            }
        });
    }

    private LoadControl buildLowLatencyLoadControl() throws Exception {
        Class<?> allocatorClass = Class.forName("com.google.android.exoplayer2.upstream.DefaultAllocator");
        Object allocator = allocatorClass.getConstructor(boolean.class, int.class).newInstance(true, 65536);
        Class<?> loadControlClass = Class.forName("com.google.android.exoplayer2.DefaultLoadControl");
        try {
            Object loadControl = loadControlClass
                    .getConstructor(allocatorClass, int.class, int.class, long.class, long.class)
                    .newInstance(allocator, 500, 3000, 200L, 500L);
            LogMgr.d(TAG, "low latency LoadControl: exoplayer 2.4 style, play=200ms, rebuffer=500ms");
            return (LoadControl) loadControl;
        } catch (NoSuchMethodException ignored) {
            Object loadControl = loadControlClass
                    .getConstructor(allocatorClass, int.class, int.class, int.class, int.class, int.class, boolean.class)
                    .newInstance(allocator, 500, 3000, 200, 500, -1, true);
            LogMgr.d(TAG, "low latency LoadControl: exoplayer 2.7 style, play=200ms, rebuffer=500ms");
            return (LoadControl) loadControl;
        }
    }

    private void addListenerCompat() throws Exception {
        Class<?> listenerClass = Class.forName("com.google.android.exoplayer2.ExoPlayer$EventListener");
        Method method = player.getClass().getMethod("addListener", listenerClass);
        method.invoke(player, this);
    }

    private void removeListenerCompat() throws Exception {
        Class<?> listenerClass = Class.forName("com.google.android.exoplayer2.ExoPlayer$EventListener");
        Method method = player.getClass().getMethod("removeListener", listenerClass);
        method.invoke(player, this);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        LogMgr.d(TAG, "state=" + state + ", playWhenReady=" + playWhenReady);
        if (released) {
            return;
        }
        if (state == ExoPlayer.STATE_READY && !readyNotified) {
            readyNotified = true;
            if (callback != null) {
                callback.onStreamReady();
            }
        } else if (state == ExoPlayer.STATE_ENDED && !endedNotified) {
            endedNotified = true;
            if (callback != null) {
                callback.onStreamEnded();
            }
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        LogMgr.e(TAG, "player error: " + error.toString());
        if (!released && callback != null) {
            callback.onStreamError(error.getMessage());
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    // Runtime ExoPlayer on R1 is 2.4.0. Its EventListener calls this older signature.
    public void onTimelineChanged(Timeline timeline, Object manifest) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    // Runtime ExoPlayer on R1 is 2.4.0. Its EventListener calls this older signature.
    public void onPositionDiscontinuity() {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }
}
