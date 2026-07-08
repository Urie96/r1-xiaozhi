package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;

import com.unisound.vui.util.LogMgr;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class XiaoZhiStreamingSession {
    public interface Callback {
        void onPlaybackStarted();

        void onPlaybackEnded();

        void onError(String errorMessage);
    }

    private static final String TAG = "XiaoZhiSession";
    private static final int SERVER_SAMPLE_RATE = 24000;
    private static final int SERVER_CHANNELS = 1;

    private final Context context;
    private final String wsUrl;
    private final String agentId;
    private final Callback callback;
    private final OkHttpClient client;
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    private XiaoZhiStreamDataSource dataSource;
    private XiaoZhiStreamPlayer player;
    private OggOpusStreamMuxer muxer;
    private WebSocket webSocket;
    private int audioFrames;

    public XiaoZhiStreamingSession(Context context, String serverBaseUrl, String agentId, Callback callback) {
        this.context = context.getApplicationContext();
        this.wsUrl = toWsUrl(serverBaseUrl);
        this.agentId = agentId;
        this.callback = callback;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void start(String text) {
        try {
            this.muxer = new OggOpusStreamMuxer();
            this.dataSource = new XiaoZhiStreamDataSource();
            this.dataSource.feed(this.muxer.buildOpusHeadPage(SERVER_SAMPLE_RATE, SERVER_CHANNELS));
            this.dataSource.feed(this.muxer.buildOpusTagsPage());
            this.player = new XiaoZhiStreamPlayer(context);
            this.player.start(this.dataSource, new XiaoZhiStreamPlayer.Callback() {
                @Override
                public void onStreamReady() {
                    if (!canceled.get() && callback != null) {
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
            connect(text);
        } catch (Exception e) {
            fail("start xiaozhi stream failed: " + e.toString());
        }
    }

    public void cancel() {
        canceled.set(true);
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
        if (dataSource != null) {
            dataSource.cancel();
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    private void connect(final String text) {
        Request request = new Request.Builder()
                .url(wsUrl)
                .header("Protocol-Version", "1")
                .header("Client-Id", "feixun-r1")
                .header("Device-Id", "feixun-r1")
                .build();
        this.webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                try {
                    JSONObject hello = new JSONObject();
                    hello.put("type", "hello");
                    hello.put("transport", "websocket");
                    hello.put("version", 1);
                    webSocket.send(hello.toString());

                    JSONObject recognize = new JSONObject();
                    recognize.put("type", "listen");
                    recognize.put("state", "recognize");
                    recognize.put("text", text);
                    recognize.put("agent_id", agentId);
                    webSocket.send(recognize.toString());
                    LogMgr.d(TAG, "recognize sent: " + text);
                } catch (Exception e) {
                    fail("send recognize failed: " + e.toString());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleControlMessage(webSocket, text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                handleAudioFrame(bytes);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (!canceled.get() && !finished.get()) {
                    fail("xiaozhi websocket failed: " + t.toString());
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                LogMgr.d(TAG, "websocket closed: " + code + ", " + reason);
            }
        });
    }

    private void handleControlMessage(WebSocket webSocket, String message) {
        if (canceled.get() || finished.get()) {
            return;
        }
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type", "");
            String state = json.optString("state", "");
            if ("tts".equals(type) && "stop".equals(state)) {
                LogMgr.d(TAG, "tts stop, frames=" + audioFrames);
                if (audioFrames <= 0) {
                    LogMgr.d(TAG, "ignore empty tts stop before first audio frame");
                    return;
                }
                if (dataSource != null && muxer != null) {
                    dataSource.feed(muxer.buildEndPage());
                    dataSource.finish();
                }
                webSocket.close(1000, "done");
            }
        } catch (Exception e) {
            fail("parse xiaozhi control failed: " + e.toString());
        }
    }

    private void handleAudioFrame(ByteString bytes) {
        if (canceled.get() || finished.get() || dataSource == null || muxer == null) {
            return;
        }
        try {
            byte[] page = muxer.buildAudioPage(bytes.toByteArray());
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

    private void finishNormally() {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        LogMgr.d(TAG, "playback ended");
        if (webSocket != null) {
            webSocket.close(1000, "playback-ended");
            webSocket = null;
        }
        if (dataSource != null) {
            dataSource.cancel();
            dataSource = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        if (!canceled.get() && callback != null) {
            callback.onPlaybackEnded();
        }
    }

    private void fail(String message) {
        LogMgr.e(TAG, message);
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
        if (dataSource != null) {
            dataSource.cancel();
            dataSource = null;
        }
        if (player != null) {
            player.stop();
            player = null;
        }
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        if (!canceled.get() && callback != null) {
            callback.onError(message);
        }
    }

    private static String toWsUrl(String serverBaseUrl) {
        String base = serverBaseUrl == null ? "" : serverBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.startsWith("https://")) {
            return "wss://" + base.substring("https://".length()) + "/ws";
        }
        if (base.startsWith("http://")) {
            return "ws://" + base.substring("http://".length()) + "/ws";
        }
        if (base.startsWith("ws://") || base.startsWith("wss://")) {
            return base.endsWith("/ws") ? base : base + "/ws";
        }
        return "ws://" + base + "/ws";
    }
}
