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
    public interface ConversationCloseListener {
        void onConversationClosed();
    }

    private static final String TAG = "XiaoZhiUpstream";
    private static final int PROTOCOL_VERSION = 3;

    private final OkHttpClient client;
    private final String wsUrl;
    private final String token;
    private final Object lock = new Object();
    private final Map<String, XiaoZhiTurn> turns = new HashMap<String, XiaoZhiTurn>();
    private final ConversationCloseListener closeListener;

    private WebSocket webSocket;
    private String deviceId = "feixun-r1";
    private String serverSessionId;
    private XiaoZhiTurn currentTurn;
    private final List<byte[]> pendingAudio = new ArrayList<byte[]>();
    private boolean pendingStop;
    private boolean connecting;
    private boolean connected;
    private boolean helloReceived;
    private boolean listenStarted;
    private boolean serverTtsStarted;
    private boolean serverAudioReceived;

    public XiaoZhiUpstreamClient(String wsUrl, String token, ConversationCloseListener closeListener) {
        this.wsUrl = normalizeWsUrl(wsUrl);
        this.token = token == null ? "" : token.trim();
        this.closeListener = closeListener;
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
            resetTurnStateLocked();
            currentTurn = turn;
            turns.put(turn.uuid(), turn);
        }
        ensureConnected();
        flushPendingIfReady();
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
            if (!readyForAudioLocked()) {
                pendingAudio.add(opusPacket);
                ws = null;
            } else {
                ws = webSocket;
            }
        }
        if (ws == null) {
            flushPendingIfReady();
            return;
        }
        if (!sendAudioFrame(ws, opusPacket)) {
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
            if (!readyForAudioLocked()) {
                pendingStop = true;
                ws = null;
            } else {
                ws = webSocket;
            }
        }
        if (ws == null) {
            flushPendingIfReady();
            return;
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
                resetTurnStateLocked();
                ws = connected ? webSocket : null;
            }
        }
        if (ws != null) {
            ws.send(abort());
        }
    }

    public void closeConnection() {
        WebSocket ws;
        XiaoZhiTurn turn;
        synchronized (lock) {
            ws = webSocket;
            webSocket = null;
            connected = false;
            connecting = false;
            helloReceived = false;
            serverSessionId = null;
            turn = currentTurn;
            currentTurn = null;
            if (turn != null) {
                turns.remove(turn.uuid());
            }
            resetTurnStateLocked();
        }
        if (turn != null) {
            turn.cancel();
        }
        if (ws != null) {
            LogMgr.d(TAG, "close websocket for enter wakeup");
            ws.close(1000, "enter wakeup");
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
        LogMgr.d(TAG, "connect " + wsUrl + ", deviceId=" + deviceId + ", protocol=v" + PROTOCOL_VERSION);
        WebSocket connectingSocket = client.newWebSocket(builder.build(), new Listener());
        boolean keepSocket;
        synchronized (lock) {
            keepSocket = connecting && webSocket == null;
            if (keepSocket) {
                webSocket = connectingSocket;
            }
        }
        if (!keepSocket) {
            connectingSocket.close(1000, "connection no longer needed");
        }
    }

    private boolean readyForAudioLocked() {
        return connected && helloReceived && listenStarted && webSocket != null;
    }

    private void handleOpen(WebSocket ws) {
        synchronized (lock) {
            if (ws != webSocket) {
                ws.close(1000, "stale connection");
                return;
            }
            connected = true;
            connecting = false;
            helloReceived = false;
            serverSessionId = null;
        }
        if (!ws.send(hello())) {
            failCurrent("send hello to xiaozhi websocket failed");
        }
    }

    private void flushPendingIfReady() {
        WebSocket ws;
        List<byte[]> audio;
        boolean stop;
        boolean start;
        synchronized (lock) {
            if (!connected || !helloReceived || webSocket == null || currentTurn == null) {
                return;
            }
            ws = webSocket;
            start = !listenStarted;
            if (start) {
                listenStarted = true;
            }
            audio = new ArrayList<byte[]>(pendingAudio);
            pendingAudio.clear();
            stop = pendingStop;
            pendingStop = false;
        }
        if (start && !ws.send(listenStart())) {
            failCurrent("send listen.start to xiaozhi websocket failed");
            return;
        }
        for (int i = 0; i < audio.size(); i++) {
            if (!sendAudioFrame(ws, audio.get(i))) {
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
                synchronized (lock) {
                    serverSessionId = json.optString("session_id", null);
                    helloReceived = true;
                }
                LogMgr.d(TAG, "server hello session=" + serverSessionId);
                flushPendingIfReady();
                return;
            }
            if ("stt".equals(type)) {
                LogMgr.d(TAG, "stt=" + json.optString("text", ""));
                return;
            }
            if ("llm".equals(type)) {
                LogMgr.d(TAG, "llm=" + json.optString("text", ""));
                return;
            }
            if ("tts".equals(type)) {
                String state = json.optString("state", "");
                LogMgr.d(TAG, "tts state=" + state + ", text=" + json.optString("text", ""));
                if ("start".equals(state) || "sentence_start".equals(state)) {
                    synchronized (lock) {
                        serverTtsStarted = true;
                    }
                } else if ("stop".equals(state)) {
                    if (shouldFinishOnTtsStop()) {
                        finishCurrentAudioFromServer();
                    } else {
                        LogMgr.d(TAG, "ignore tts.stop before server tts/audio");
                    }
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
        byte[] audio = decodeAudioFrame(bytes.toByteArray());
        if (audio == null || audio.length == 0) {
            LogMgr.e(TAG, "drop invalid xiaozhi audio frame, bytes=" + bytes.size());
            return;
        }
        XiaoZhiTurn turn;
        synchronized (lock) {
            serverAudioReceived = true;
            turn = currentTurn;
        }
        if (turn != null) {
            turn.offerAudio(audio);
        }
    }

    private boolean shouldFinishOnTtsStop() {
        synchronized (lock) {
            return serverTtsStarted || serverAudioReceived;
        }
    }

    private void finishCurrentAudioFromServer() {
        XiaoZhiTurn turn;
        synchronized (lock) {
            turn = currentTurn;
            currentTurn = null;
            resetTurnStateLocked();
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
            resetTurnStateLocked();
        }
        if (turn != null) {
            turn.fail(message);
        }
        LogMgr.e(TAG, message);
    }

    private void handleFailure(WebSocket ws, String message) {
        XiaoZhiTurn turn = null;
        synchronized (lock) {
            if (ws != webSocket) {
                LogMgr.e(TAG, message + " (stale websocket)");
                return;
            }
            webSocket = null;
            connected = false;
            connecting = false;
            helloReceived = false;
            serverSessionId = null;
            turn = currentTurn;
            currentTurn = null;
            resetTurnStateLocked();
        }
        if (turn != null) {
            turn.fail(message);
        }
        LogMgr.e(TAG, message);
    }

    private void handleClosed(WebSocket ws, String message) {
        XiaoZhiTurn turn = null;
        synchronized (lock) {
            if (ws != webSocket) {
                LogMgr.d(TAG, message + " (stale websocket)");
                return;
            }
            webSocket = null;
            connected = false;
            connecting = false;
            helloReceived = false;
            serverSessionId = null;
            turn = currentTurn;
            currentTurn = null;
            resetTurnStateLocked();
        }
        boolean deliveredToTurn = turn != null && turn.close();
        LogMgr.d(TAG, message);
        if (!deliveredToTurn && closeListener != null) {
            closeListener.onConversationClosed();
        }
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
            if (serverSessionId != null) {
                json.put("session_id", serverSessionId);
            }
            json.put("type", "abort");
            json.put("reason", "client cancel");
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resetTurnStateLocked() {
        pendingAudio.clear();
        pendingStop = false;
        listenStarted = false;
        serverTtsStarted = false;
        serverAudioReceived = false;
    }

    private static boolean sendAudioFrame(WebSocket ws, byte[] opusPacket) {
        return ws.send(ByteString.of(encodeAudioFrame(opusPacket)));
    }

    private static byte[] encodeAudioFrame(byte[] opusPacket) {
        if (PROTOCOL_VERSION == 1) {
            return opusPacket;
        }
        if (PROTOCOL_VERSION == 3) {
            byte[] frame = new byte[4 + opusPacket.length];
            frame[0] = 0;
            frame[1] = 0;
            frame[2] = (byte) ((opusPacket.length >> 8) & 0xff);
            frame[3] = (byte) (opusPacket.length & 0xff);
            System.arraycopy(opusPacket, 0, frame, 4, opusPacket.length);
            return frame;
        }
        byte[] frame = new byte[16 + opusPacket.length];
        frame[0] = 0;
        frame[1] = 2;
        frame[2] = 0;
        frame[3] = 0;
        frame[12] = (byte) ((opusPacket.length >> 24) & 0xff);
        frame[13] = (byte) ((opusPacket.length >> 16) & 0xff);
        frame[14] = (byte) ((opusPacket.length >> 8) & 0xff);
        frame[15] = (byte) (opusPacket.length & 0xff);
        System.arraycopy(opusPacket, 0, frame, 16, opusPacket.length);
        return frame;
    }

    private static byte[] decodeAudioFrame(byte[] frame) {
        if (frame == null || frame.length == 0) {
            return null;
        }
        if (PROTOCOL_VERSION == 1) {
            return frame;
        }
        if (PROTOCOL_VERSION == 3) {
            if (frame.length < 4 || (frame[0] & 0xff) != 0) {
                return null;
            }
            int payloadSize = ((frame[2] & 0xff) << 8) | (frame[3] & 0xff);
            if (payloadSize < 0 || payloadSize > frame.length - 4) {
                return null;
            }
            byte[] payload = new byte[payloadSize];
            System.arraycopy(frame, 4, payload, 0, payloadSize);
            return payload;
        }
        if (frame.length < 16) {
            return null;
        }
        int type = ((frame[2] & 0xff) << 8) | (frame[3] & 0xff);
        if (type != 0) {
            return null;
        }
        int payloadSize = ((frame[12] & 0xff) << 24) | ((frame[13] & 0xff) << 16)
                | ((frame[14] & 0xff) << 8) | (frame[15] & 0xff);
        if (payloadSize < 0 || payloadSize > frame.length - 16) {
            return null;
        }
        byte[] payload = new byte[payloadSize];
        System.arraycopy(frame, 16, payload, 0, payloadSize);
        return payload;
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
        public void onClosing(WebSocket webSocket, int code, String reason) {
            handleClosed(webSocket, "xiaozhi websocket closing: " + code + ", " + reason);
            webSocket.close(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            handleFailure(webSocket, "xiaozhi websocket failed: " + t.toString());
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            handleClosed(webSocket, "xiaozhi websocket closed: " + code + ", " + reason);
        }
    }
}
