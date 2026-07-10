package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;

import com.unisound.vui.util.LogMgr;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class XiaoZhiStreamingSession {
    public interface Callback {
        void onPlaybackStarted();

        void onPlaybackEnded();

        void onNoAudio();

        void onError(String errorMessage);
    }

    private static final String TAG = "XiaoZhiSession";
    private static final int SERVER_SAMPLE_RATE = 24000;
    private static final int SERVER_CHANNELS = 1;

    private final Context context;
    private final XiaoZhiBridgeManager bridgeManager;
    private final Callback callback;
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    private XiaoZhiTurn turn;
    private XiaoZhiStreamDataSource dataSource;
    private XiaoZhiStreamPlayer player;
    private OggOpusStreamMuxer muxer;
    private Thread feederThread;
    private int audioFrames;

    public XiaoZhiStreamingSession(Context context, XiaoZhiBridgeManager bridgeManager, Callback callback) {
        this.context = context.getApplicationContext();
        this.bridgeManager = bridgeManager;
        this.callback = callback;
    }

    public void start(XiaoZhiTurn turn) {
        this.turn = turn;
        try {
            this.muxer = new OggOpusStreamMuxer();
            this.dataSource = new XiaoZhiStreamDataSource();
            this.dataSource.feed(this.muxer.buildOpusHeadPage(SERVER_SAMPLE_RATE, SERVER_CHANNELS));
            this.dataSource.feed(this.muxer.buildOpusTagsPage());
            this.player = new XiaoZhiStreamPlayer(context);
            this.player.start(this.dataSource, new XiaoZhiStreamPlayer.Callback() {
                @Override
                public void onStreamReady() {
                    if (!canceled.get() && !finished.get() && callback != null) {
                        callback.onPlaybackStarted();
                    }
                }

                @Override
                public void onStreamEnded() {
                    finishNormally();
                }

                @Override
                public void onStreamError(String errorMessage) {
                    fail("xiaozhi stream play error: " + errorMessage);
                }
            });
            startFeeder();
        } catch (Exception e) {
            fail("start xiaozhi stream failed: " + e.toString());
        }
    }

    public void cancel() {
        canceled.set(true);
        XiaoZhiTurn currentTurn = turn;
        if (currentTurn != null) {
            bridgeManager.cancelTurn(currentTurn.uuid());
        }
        if (dataSource != null) {
            dataSource.cancel();
        }
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    private void startFeeder() {
        feederThread = new Thread(new Runnable() {
            @Override
            public void run() {
                feedLoop();
            }
        }, "xiaozhi-stream-feeder");
        feederThread.setDaemon(true);
        feederThread.start();
    }

    private void feedLoop() {
        while (!canceled.get() && !finished.get()) {
            try {
                XiaoZhiTurn.Event event = turn.take();
                if (event.type == XiaoZhiTurn.EVENT_AUDIO) {
                    handleAudioFrame(event.data);
                } else if (event.type == XiaoZhiTurn.EVENT_FINISH) {
                    handleFinish();
                    return;
                } else if (event.type == XiaoZhiTurn.EVENT_ERROR) {
                    fail(event.message == null ? "xiaozhi turn failed" : event.message);
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("xiaozhi feeder interrupted");
                return;
            }
        }
    }

    private void handleFinish() {
        if (audioFrames <= 0) {
            finishNoAudio();
            return;
        }
        try {
            if (dataSource != null && muxer != null) {
                dataSource.feed(muxer.buildEndPage());
                dataSource.finish();
            }
        } catch (IOException e) {
            fail("finish ogg stream failed: " + e.toString());
        }
    }

    private void handleAudioFrame(byte[] packet) {
        if (canceled.get() || finished.get() || dataSource == null || muxer == null) {
            return;
        }
        try {
            byte[] page = muxer.buildAudioPage(packet);
            if (page != null) {
                dataSource.feed(page);
                audioFrames++;
                if (audioFrames == 1) {
                    LogMgr.d(TAG, "first audio frame received");
                }
            }
        } catch (IOException e) {
            fail("mux opus frame failed: " + e.toString());
        }
    }

    private void finishNoAudio() {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        LogMgr.d(TAG, "turn finished without tts audio");
        completeTurn();
        if (dataSource != null) {
            dataSource.cancel();
            dataSource = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        if (!canceled.get() && callback != null) {
            callback.onNoAudio();
        }
    }

    private void finishNormally() {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        LogMgr.d(TAG, "playback ended");
        completeTurn();
        if (dataSource != null) {
            dataSource.cancel();
            dataSource = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        if (!canceled.get() && callback != null) {
            callback.onPlaybackEnded();
        }
    }

    private void fail(String message) {
        LogMgr.e(TAG, message);
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        completeTurn();
        if (dataSource != null) {
            dataSource.cancel();
            dataSource = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        if (!canceled.get() && callback != null) {
            callback.onError(message);
        }
    }

    private void completeTurn() {
        XiaoZhiTurn currentTurn = turn;
        if (currentTurn != null) {
            bridgeManager.completeTurn(currentTurn.uuid());
        }
    }
}
