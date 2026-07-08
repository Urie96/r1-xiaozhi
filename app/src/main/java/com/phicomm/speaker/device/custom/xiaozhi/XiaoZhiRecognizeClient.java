package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;

import com.unisound.vui.util.LogMgr;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class XiaoZhiRecognizeClient {
    private static final String TAG = "XiaoZhiClient";
    private static final int SERVER_SAMPLE_RATE = 24000;
    private static final int SERVER_CHANNELS = 1;
    private static final long RECOGNIZE_TIMEOUT_SECONDS = 90L;

    private final OkHttpClient client;
    private final String wsUrl;
    private final String agentId;

    public XiaoZhiRecognizeClient(String serverBaseUrl, String agentId) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.wsUrl = toWsUrl(serverBaseUrl);
        this.agentId = agentId;
    }

    public File recognize(Context context, String text) throws IOException, InterruptedException {
        File output = File.createTempFile("xiaozhi_", ".ogg", context.getCacheDir());
        SessionListener listener = new SessionListener(output, text, agentId);
        Request request = new Request.Builder()
                .url(wsUrl)
                .header("Protocol-Version", "1")
                .header("Client-Id", "feixun-r1")
                .header("Device-Id", "feixun-r1")
                .build();
        WebSocket webSocket = client.newWebSocket(request, listener);
        boolean finished = listener.await(RECOGNIZE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            webSocket.cancel();
            listener.abort();
            throw new IOException("xiaozhi recognize timeout");
        }
        if (listener.error != null) {
            throw listener.error;
        }
        File result = listener.resultFile;
        if (result == null || !result.exists() || result.length() <= 0) {
            throw new IOException("xiaozhi returned empty audio");
        }
        return result;
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
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

    private static final class SessionListener extends WebSocketListener {
        private final CountDownLatch done = new CountDownLatch(1);
        private final File output;
        private final String text;
        private final String agentId;
        private OggOpusWriter writer;
        private int audioFrames;
        private File resultFile;
        private IOException error;
        private boolean stopped;
        private boolean ttsStarted;

        SessionListener(File output, String text, String agentId) {
            this.output = output;
            this.text = text;
            this.agentId = agentId;
        }

        boolean await(long timeout, TimeUnit unit) throws InterruptedException {
            return done.await(timeout, unit);
        }

        void abort() {
            closeWriterQuietly(false);
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            try {
                writer = new OggOpusWriter(output, SERVER_SAMPLE_RATE, SERVER_CHANNELS);
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
                fail(webSocket, new IOException("open xiaozhi websocket", e));
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JSONObject json = new JSONObject(text);
                String type = json.optString("type", "");
                String state = json.optString("state", "");
                if ("tts".equals(type)) {
                    if ("start".equals(state)) {
                        // 真正的 TTS 本轮开始；服务端在 recognize 后会先发一条
                        // tts.stop(reset) 再发 tts.start，必须以 start 为准。
                        ttsStarted = true;
                    } else if ("stop".equals(state) && ttsStarted) {
                        stopped = true;
                        closeWriterQuietly(true);
                        if (audioFrames <= 0) {
                            fail(webSocket, new IOException("xiaozhi tts completed without audio"));
                            return;
                        }
                        resultFile = output;
                        webSocket.close(1000, "done");
                        done.countDown();
                    }
                }
            } catch (Exception e) {
                fail(webSocket, new IOException("parse xiaozhi message", e));
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            try {
                if (writer != null && !stopped) {
                    writer.writeOpusPacket(bytes.toByteArray());
                    audioFrames++;
                }
            } catch (IOException e) {
                fail(webSocket, e);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (resultFile == null) {
                error = t instanceof IOException ? (IOException) t : new IOException("xiaozhi websocket failed", t);
                closeWriterQuietly(false);
            }
            done.countDown();
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            done.countDown();
        }

        private void fail(WebSocket webSocket, IOException e) {
            error = e;
            closeWriterQuietly(false);
            webSocket.cancel();
            done.countDown();
        }

        private void closeWriterQuietly(boolean finish) {
            if (writer == null) {
                return;
            }
            try {
                if (finish) {
                    writer.close();
                } else {
                    writer.abort();
                    output.delete();
                }
            } catch (IOException e) {
                if (error == null) {
                    error = e;
                }
            } finally {
                writer = null;
            }
        }
    }
}
