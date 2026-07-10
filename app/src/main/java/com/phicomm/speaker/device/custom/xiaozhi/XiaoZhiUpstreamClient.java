package com.phicomm.speaker.device.custom.xiaozhi;

import com.unisound.vui.util.LogMgr;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class XiaoZhiUpstreamClient {
    private static final String TAG = "XiaoZhiUpstream";
    private static final int PROTOCOL_VERSION = 1;

    private final OkHttpClient client;
    private final String wsUrl;
    private final String token;
    private final Object lock = new Object();
    private final Map<String, XiaoZhiTurn> turns = new HashMap<String, XiaoZhiTurn>();

    private WebSocket webSocket;
    private String deviceId = "feixun-r1";
    private String serverSessionId;
    private XiaoZhiTurn currentTurn;
    private final List<byte[]> pendingAudio = new ArrayList<byte[]>();
    private boolean pendingStop;
    private boolean connecting;
    private boolean connected;

    public XiaoZhiUpstreamClient(String wsUrl, String token) {
        this.wsUrl = normalizeWsUrl(wsUrl);
        this.token = token == null ? "" : token.trim();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public XiaoZhiTurn startTurn(String deviceId) {
        XiaoZhiTurn turn = new XiaoZhiTurn(UUID.randomUUID().toString());
        synchronized (lock) {
            if (deviceId != null && deviceId.trim().length() > 0) {
                this.deviceId = deviceId.trim();
            }
            if (currentTurn != null) {
                turns.remove(currentTurn.uuid());
                currentTurn.cancel();
            }
            pendingAudio.clear();
            pendingStop = false;
            currentTurn = turn;
            turns.put(turn.uuid(), turn);
        }
        ensureConnected();
        if (isConnected() && !sendListenStart()) {
            turn.fail("xiaozhi websocket unavailable");
        }
        return turn;
    }

    public XiaoZhiTurn getTurn(String uuid) {
        synchronized (lock) {
            return turns.get(uuid);
        }
    }

    public void sendAudio(String uuid, byte[] opusPacket) {
        WebSocket ws;
        synchronized (lock) {
            XiaoZhiTurn turn = turns.get(uuid);
            if (turn == null || turn != currentTurn) {
                return;
            }
            ws = connected ? webSocket : null;
            if (ws == null) {
                pendingAudio.add(opusPacket);
                return;
            }
        }
        if (!ws.send(ByteString.of(opusPacket))) {
            failCurrent("send audio to xiaozhi websocket failed");
        }
    }

    public void finishTurn(String uuid) {
        WebSocket ws;
        synchronized (lock) {
            XiaoZhiTurn turn = turns.get(uuid);
            if (turn == null || turn != currentTurn) {
                return;
            }
            ws = connected ? webSocket : null;
            if (ws == null) {
                pendingStop = true;
                return;
            }
        }
        if (!ws.send(listenStop())) {
            failCurrent("send listen.stop to xiaozhi websocket failed");
        }
    }

    public void completeTurn(String uuid) {
        synchronized (lock) {
            turns.remove(uuid);
        }
    }

    public void cancelTurn(String uuid) {
        WebSocket ws = null;
        synchronized (lock) {
            XiaoZhiTurn turn = turns.remove(uuid);
            if (turn != null) {
                turn.cancel();
            }
            if (turn != null && turn == currentTurn) {
                currentTurn = null;
                pendingAudio.clear();
                pendingStop = false;
                ws = connected ? webSocket : null;
            }
        }
        if (ws != null) {
            ws.send(abort());
        }
    }

    private void ensureConnected() {
        synchronized (lock) {
            if (connected || connecting) {
                return;
            }
            connecting = true;
        }

        Request.Builder builder = new Request.Builder()
                .url(wsUrl)
                .header("Protocol-Version", String.valueOf(PROTOCOL_VERSION))
                .header("Client-Id", deviceId)
                .header("Device-Id", deviceId);
        if (token.length() > 0) {
            builder.header("Authorization", "Bearer " + token);
        }
        LogMgr.d(TAG, "connect " + wsUrl + ", deviceId=" + deviceId);
        client.newWebSocket(builder.build(), new Listener());
    }

    private boolean isConnected() {
        synchronized (lock) {
            return connected && webSocket != null;
        }
    }

    private boolean sendListenStart() {
        WebSocket ws;
        synchronized (lock) {
            ws = webSocket;
        }
        if (ws == null) {
            return false;
        }
        return ws.send(listenStart());
    }

    private void handleOpen(WebSocket ws) {
        synchronized (lock) {
            webSocket = ws;
            connected = true;
            connecting = false;
        }
        ws.send(hello());
        flushPendingAfterOpen(ws);
    }

    private void flushPendingAfterOpen(WebSocket ws) {
        List<byte[]> audio;
        boolean stop;
        XiaoZhiTurn turn;
        synchronized (lock) {
            turn = currentTurn;
            audio = new ArrayList<byte[]>(pendingAudio);
            pendingAudio.clear();
            stop = pendingStop;
            pendingStop = false;
        }
        if (turn == null) {
            return;
        }
        if (!ws.send(listenStart())) {
            failCurrent("send listen.start after websocket open failed");
            return;
        }
        for (int i = 0; i < audio.size(); i++) {
            if (!ws.send(ByteString.of(audio.get(i)))) {
                failCurrent("flush pending audio to xiaozhi websocket failed");
                return;
            }
        }
        if (stop && !ws.send(listenStop())) {
            failCurrent("flush pending listen.stop to xiaozhi websocket failed");
        }
    }

    private void handleText(String text) {
        try {
            JSONObject json = new JSONObject(text);
            String type = json.optString("type", "");
            if ("hello".equals(type)) {
                serverSessionId = json.optString("session_id", null);
                LogMgr.d(TAG, "server hello session=" + serverSessionId);
                return;
            }
            if ("stt".equals(type)) {
                LogMgr.d(TAG, "stt=" + json.optString("text", ""));
                return;
            }
            if ("tts".equals(type)) {
                String state = json.optString("state", "");
                LogMgr.d(TAG, "tts state=" + state + ", text=" + json.optString("text", ""));
                if ("stop".equals(state)) {
                    finishCurrentFromServer();
                }
                return;
            }
            if ("error".equals(type)) {
                failCurrent("xiaozhi server error: " + text);
            }
        } catch (Exception e) {
            LogMgr.e(TAG, "parse upstream text failed: " + e.toString() + ", msg=" + text);
        }
    }

    private void handleAudio(ByteString bytes) {
        XiaoZhiTurn turn;
        synchronized (lock) {
            turn = currentTurn;
        }
        if (turn != null) {
            turn.offerAudio(bytes.toByteArray());
        }
    }

    private void finishCurrentFromServer() {
        XiaoZhiTurn turn;
        synchronized (lock) {
            turn = currentTurn;
            currentTurn = null;
            pendingAudio.clear();
            pendingStop = false;
        }
        if (turn != null) {
            turn.finish();
        }
    }

    private void failCurrent(String message) {
        XiaoZhiTurn turn;
        synchronized (lock) {
            turn = currentTurn;
            currentTurn = null;
            pendingAudio.clear();
            pendingStop = false;
        }
        if (turn != null) {
            turn.fail(message);
        }
        LogMgr.e(TAG, message);
    }

    private void handleClosedOrFailed(String message) {
        XiaoZhiTurn turn;
        synchronized (lock) {
            webSocket = null;
            connected = false;
            connecting = false;
            serverSessionId = null;
            turn = currentTurn;
            currentTurn = null;
            pendingAudio.clear();
            pendingStop = false;
        }
        if (turn != null) {
            turn.fail(message);
        }
        LogMgr.e(TAG, message);
    }

    private String hello() throws RuntimeException {
        try {
            JSONObject audioParams = new JSONObject();
            audioParams.put("format", "opus");
            audioParams.put("sample_rate", 16000);
            audioParams.put("channels", 1);
            audioParams.put("frame_duration", 20);
            JSONObject features = new JSONObject();
            features.put("mcp", false);
            features.put("aec", false);
            JSONObject json = new JSONObject();
            json.put("type", "hello");
            json.put("version", PROTOCOL_VERSION);
            json.put("transport", "websocket");
            json.put("features", features);
            json.put("audio_params", audioParams);
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String listenStart() {
        try {
            JSONObject json = new JSONObject();
            if (serverSessionId != null) {
                json.put("session_id", serverSessionId);
            }
            json.put("type", "listen");
            json.put("state", "start");
            json.put("mode", "manual");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String listenStop() {
        try {
            JSONObject json = new JSONObject();
            if (serverSessionId != null) {
                json.put("session_id", serverSessionId);
            }
            json.put("type", "listen");
            json.put("state", "stop");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String abort() {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "abort");
            json.put("reason", "client cancel");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String normalizeWsUrl(String url) {
        String s = url == null ? "" : url.trim();
        if (s.length() == 0) {
            s = "ws://home.lubui.com:3116/ws";
        }
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        if (s.startsWith("https://")) {
            s = "wss://" + s.substring("https://".length());
        } else if (s.startsWith("http://")) {
            s = "ws://" + s.substring("http://".length());
        }
        if (!s.startsWith("ws://") && !s.startsWith("wss://")) {
            s = "ws://" + s;
        }
        return s.endsWith("/ws") ? s : s + "/ws";
    }

    private final class Listener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            LogMgr.d(TAG, "websocket open, http=" + response.code());
            handleOpen(webSocket);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            handleText(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            handleAudio(bytes);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            handleClosedOrFailed("xiaozhi websocket failed: " + t.toString());
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            handleClosedOrFailed("xiaozhi websocket closed: " + code + ", " + reason);
        }
    }
}
