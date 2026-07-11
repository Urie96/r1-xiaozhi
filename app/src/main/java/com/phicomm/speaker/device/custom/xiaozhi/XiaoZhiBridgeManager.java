package com.phicomm.speaker.device.custom.xiaozhi;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.phicomm.speaker.device.custom.ipc.PhicommXController;
import com.phicomm.speaker.device.custom.status.PhicommDeviceStatusProcessor;
import com.unisound.ant.device.controlor.DefaultVolumeOperator;
import com.unisound.vui.engine.ANTEngine;
import com.unisound.vui.engine.ANTHandlerContext;
import com.unisound.vui.util.ExoConstants;
import com.unisound.vui.util.LogMgr;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class XiaoZhiBridgeManager {
    private static final String TAG = "XiaoZhiBridge";
    private static final String LOCAL_TR_ADDR = "127.0.0.1:8089";
    private static final int LOCAL_TR_PORT = 8089;
    private static final int MAX_BODY_BYTES = 2 * 1024 * 1024;
    private static final String DEFAULT_WS_URL = "ws://home.lubui.com:3116/ws";
    private static XiaoZhiBridgeManager instance;

    private final Context context;
    private final XiaoZhiSettings settings;
    private final String defaultWsUrl;
    private final XiaoZhiUpstreamClient upstream;
    private final PhicommXController phicommXController;
    private final Map<String, ActiveSession> active = new HashMap<String, ActiveSession>();
    private final AtomicInteger serverCloseGeneration = new AtomicInteger();
    private volatile ANTHandlerContext antContext;
    private volatile boolean serverStarted;

    private XiaoZhiBridgeManager(Context context) {
        this.context = context;
        this.defaultWsUrl = config("xiaozhi_ws", DEFAULT_WS_URL);
        this.settings = XiaoZhiSettings.load(context, this.defaultWsUrl);
        this.upstream = new XiaoZhiUpstreamClient(this.settings.wsUrl, config("xiaozhi_token", "dev-token"), new XiaoZhiUpstreamClient.ConversationCloseListener() {
            @Override
            public void onConversationClosed() {
                enterWakeupFromServerClose();
            }
        });
        this.phicommXController = new PhicommXController(context);
    }

    public static synchronized XiaoZhiBridgeManager get(Context context) {
        if (instance == null) {
            instance = new XiaoZhiBridgeManager(context.getApplicationContext());
        }
        return instance;
    }

    public static String localTrAddr() {
        return LOCAL_TR_ADDR;
    }

    public XiaoZhiSettings settings() {
        return settings;
    }

    public void registerAntContext(ANTHandlerContext ctx) {
        this.antContext = ctx;
    }

    public void startLocalHttpServer() {
        if (serverStarted) {
            return;
        }
        serverStarted = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                serveLoop();
            }
        }, "xiaozhi-local-http");
        thread.setDaemon(true);
        thread.start();
    }

    public XiaoZhiTurn takeTurn(String uuid) {
        return upstream.getTurn(uuid);
    }

    public void completeTurn(String uuid) {
        upstream.completeTurn(uuid);
    }

    public void cancelTurn(String uuid) {
        upstream.cancelTurn(uuid);
    }

    public void closeUpstreamConnection() {
        upstream.closeConnection();
    }

    public int serverCloseGeneration() {
        return serverCloseGeneration.get();
    }

    public boolean hasServerCloseSince(int generation) {
        return serverCloseGeneration.get() != generation;
    }

    private void enterWakeupFromServerClose() {
        serverCloseGeneration.incrementAndGet();
        ANTHandlerContext ctx = this.antContext;
        if (ctx != null) {
            LogMgr.d(TAG, "server closed conversation, enter wakeup");
            ctx.enterWakeup(false);
        }
    }

    private void serveLoop() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(LOCAL_TR_PORT);
            LogMgr.d(TAG, "local http listening on " + LOCAL_TR_ADDR);
            while (true) {
                final Socket socket = serverSocket.accept();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handleSocket(socket);
                    }
                }, "xiaozhi-http-client");
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Throwable t) {
            LogMgr.e(TAG, "local http server stopped: " + t.toString());
            serverStarted = false;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void handleSocket(Socket socket) {
        try {
            socket.setSoTimeout(15000);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            while (true) {
                Request request;
                try {
                    request = readRequest(in);
                } catch (SocketTimeoutException e) {
                    return;
                }
                if (request == null) {
                    return;
                }
                Response response = handleRequest(request);
                boolean keepAlive = shouldKeepAlive(request);
                writeResponse(out, response, keepAlive);
                if (!keepAlive) {
                    return;
                }
            }
        } catch (Throwable t) {
            LogMgr.e(TAG, "handle local http failed: " + t.toString());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Response handleRequest(Request request) throws Exception {
        String path = pathOnly(request.path);
        if ("OPTIONS".equals(request.method)) {
            return new Response(204, null, null, null, null, new byte[0]);
        }
        if ("POST".equals(request.method) && "/trafficRouter/cs".equals(path)) {
            return handleTraffic(request);
        }
        if ("GET".equals(request.method) && ("/".equals(path) || "/index.html".equals(path))) {
            return new Response(200, null, null, null, null, XiaoZhiIndexPage.bytes(), "text/html; charset=UTF-8");
        }
        if ("GET".equals(request.method) && "/api/status".equals(path)) {
            return handleStatus();
        }
        if ("POST".equals(request.method) && "/api/config".equals(path)) {
            return handleConfig(request);
        }
        if (isBluetoothRoute(request.method, path)) {
            return handleBluetooth(path);
        }
        if (isVolumeRoute(request.method, path)) {
            return handleVolume(request);
        }
        if (isAsrRoute(request.method, path)) {
            return handleAsr(path);
        }
        if (isSleepRoute(request.method, path)) {
            return handleSleep(path);
        }
        return new Response(404, null, null, null, null, new byte[0]);
    }

    private Response handleStatus() throws Exception {
        try {
            JSONObject json = new JSONObject();
            json.put("ok", true);
            json.put("config", configStatusJson());
            json.put("bluetooth", bluetoothStatusJson("status", null, true, null));
            json.put("volume", volumeStatusJson("status", null, null, true, null));
            json.put("asr", asrStatusJson("status", true, null));
            json.put("sleep", sleepStatusJson("status", true, null));
            return jsonResponse(200, json);
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("ok", false);
            json.put("error", e.getMessage() == null ? e.toString() : e.getMessage());
            return jsonResponse(500, json);
        }
    }

    private JSONObject configStatusJson() throws Exception {
        XiaoZhiSettings saved = XiaoZhiSettings.load(this.context, this.defaultWsUrl);
        JSONObject json = saved.toJson();
        json.put("restartRequired", !this.settings.sameAs(saved));
        return json;
    }

    private Response handleConfig(Request request) throws Exception {
        try {
            if (request.body.length == 0) {
                throw new IllegalArgumentException("request body is required");
            }
            XiaoZhiSettings saved = XiaoZhiSettings.update(this.context, this.defaultWsUrl,
                    new JSONObject(new String(request.body, "UTF-8")));
            JSONObject json = saved.toJson();
            json.put("ok", true);
            json.put("restartRequired", !this.settings.sameAs(saved));
            return jsonResponse(200, json);
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("ok", false);
            json.put("error", e.getMessage() == null ? e.toString() : e.getMessage());
            return jsonResponse(400, json);
        }
    }

    private Response handleTraffic(Request request) throws Exception {
        String deviceId = header(request.headers, "UI");
        if (deviceId == null || deviceId.trim().length() == 0) {
            deviceId = "feixun-r1";
        }
        String sid = extractSid(header(request.headers, "P"));
        if (sid == null) {
            sid = deviceId;
        }

        if (request.body.length == 0) {
            ActiveSession session = getActive(sid);
            if (session == null) {
                XiaoZhiTurn turn = upstream.startTurn(deviceId);
                putActive(sid, new ActiveSession(deviceId, turn));
                LogMgr.d(TAG, "session start sid=" + sid + ", uuid=" + turn.uuid());
                return trafficResponse(sid, "q", null, "GeLdJ", initAckBody().getBytes("UTF-8"));
            }
            removeActive(sid);
            upstream.finishTurn(session.turn.uuid());
            LogMgr.d(TAG, "session finish sid=" + sid + ", uuid=" + session.turn.uuid() + ", frames=" + session.frames);
            return trafficResponse(sid, "y", "W510", "hHbHDiH", fakeNlu(session.turn.uuid()).getBytes("UTF-8"));
        }

        ActiveSession session = getActive(sid);
        if (session == null) {
            LogMgr.e(TAG, "audio fragment for missing sid=" + sid);
            return new Response(404, null, null, null, null, new byte[0]);
        }
        int frames = forwardR1Frames(session.turn.uuid(), request.body);
        session.frames += frames;
        return trafficResponse(sid, "r", "W510-8N4", "GeLdJ", new byte[0]);
    }

    private Response handleBluetooth(String path) throws Exception {
        boolean enable;
        String action;
        if (isPath(path, "/api/bluetooth/on")) {
            enable = true;
            action = "on";
        } else if (isPath(path, "/api/bluetooth/off")) {
            enable = false;
            action = "off";
        } else {
            return new Response(404, null, null, null, null, new byte[0]);
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return bluetoothStatusResponse(action, Boolean.valueOf(enable), false, "bluetooth adapter not found");
        }

        boolean ok = true;
        String error = null;
        try {
            if (enable) {
                if (!adapter.isEnabled()) {
                    ok = adapter.enable();
                }
                // 走原厂“三击按键”链路，确保同时切换 R1 蓝牙模式/ASR 状态/提示音。
                this.phicommXController.triggeredTropleClickEvent();
            } else {
                // 关闭原厂蓝牙模式；不强制 disable 适配器，避免破坏原厂状态机。
                this.phicommXController.closeBlueToothStatus();
            }
            LogMgr.d(TAG, "bluetooth api action=" + action + ", mode=" + enable + ", ok=" + ok + ", adapterState=" + adapter.getState());
        } catch (Throwable t) {
            ok = false;
            error = t.toString();
            LogMgr.e(TAG, "bluetooth api failed action=" + action + ": " + error);
        }
        return bluetoothStatusResponse(action, Boolean.valueOf(enable), ok, error);
    }

    private Response bluetoothStatusResponse(String action, Boolean requestedBluetoothMode, boolean ok, String error) throws Exception {
        return jsonResponse(ok ? 200 : 500,
                bluetoothStatusJson(action, requestedBluetoothMode, ok, error));
    }

    private JSONObject bluetoothStatusJson(String action, Boolean requestedBluetoothMode,
                                           boolean ok, String error) throws Exception {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        JSONObject json = new JSONObject();
        json.put("ok", ok);
        json.put("action", action);
        json.put("supported", adapter != null);
        if (requestedBluetoothMode != null) {
            json.put("requestedBluetoothMode", requestedBluetoothMode.booleanValue());
        }
        if (adapter != null) {
            json.put("adapterEnabled", adapter.isEnabled());
            json.put("adapterState", adapter.getState());
        }
        json.put("bluetoothMode", isBluetoothMode());
        if (error != null) {
            json.put("error", error);
        }
        return json;
    }

    private boolean isBluetoothMode() {
        try {
            return PhicommDeviceStatusProcessor.getInstance().getDeviceStatus() == PhicommDeviceStatusProcessor.STATUS_BLUETOOTH;
        } catch (Throwable t) {
            LogMgr.e(TAG, "get bluetooth mode failed: " + t.toString());
            return false;
        }
    }

    private Response handleSleep(String path) throws Exception {
        boolean sleep;
        String action;
        if (isPath(path, "/api/sleep/start")) {
            sleep = true;
            action = "start";
        } else if (isPath(path, "/api/sleep/end")) {
            sleep = false;
            action = "end";
        } else {
            return new Response(404, null, null, null, null, new byte[0]);
        }

        try {
            boolean sleeping = isSleeping();
            if (sleep && !sleeping) {
                this.phicommXController.triggeredDoubleClickEvent();
            } else if (!sleep && sleeping) {
                this.phicommXController.triggeredOneClickEvent();
            }
            LogMgr.d(TAG, "sleep api action=" + action + ", previousSleeping=" + sleeping);
            return sleepStatusResponse(action, true, null, 200);
        } catch (Throwable t) {
            String error = t.toString();
            LogMgr.e(TAG, "sleep api failed action=" + action + ": " + error);
            return sleepStatusResponse(action, false, error, 500);
        }
    }

    private Response sleepStatusResponse(String action, boolean ok, String error, int status) throws Exception {
        return jsonResponse(status, sleepStatusJson(action, ok, error));
    }

    private JSONObject sleepStatusJson(String action, boolean ok, String error) throws Exception {
        JSONObject json = new JSONObject();
        json.put("ok", ok);
        json.put("action", action);
        json.put("sleeping", isSleeping());
        json.put("deviceStatus", PhicommDeviceStatusProcessor.getInstance().getDeviceStatus());
        json.put("contextReady", this.antContext != null);
        if (error != null) {
            json.put("error", error);
        }
        return json;
    }

    private boolean isSleeping() {
        return PhicommDeviceStatusProcessor.getInstance().getDeviceStatus()
                == PhicommDeviceStatusProcessor.STATUS_DORMANT;
    }

    private Response handleAsr(String path) throws Exception {
        if (isPath(path, "/api/asr/wakeup")) {
            ANTHandlerContext ctx = this.antContext;
            if (ctx == null) {
                return asrStatusResponse("wakeupAsr", false, "ant context not ready", 503);
            }
            try {
                ctx.pipeline().fireUserEventTriggered(ExoConstants.DO_ENTER_ASR_BY_MIC);
                LogMgr.d(TAG, "asr api wakeupAsr requested");
                return asrStatusResponse("wakeupAsr", true, null, 200);
            } catch (Throwable t) {
                String error = t.toString();
                LogMgr.e(TAG, "asr api wakeupAsr failed: " + error);
                return asrStatusResponse("wakeupAsr", false, error, 500);
            }
        }

        return new Response(404, null, null, null, null, new byte[0]);
    }

    private Response asrStatusResponse(String action, boolean ok, String error, int status) throws Exception {
        return jsonResponse(status, asrStatusJson(action, ok, error));
    }

    private JSONObject asrStatusJson(String action, boolean ok, String error) throws Exception {
        ANTHandlerContext ctx = this.antContext;
        JSONObject json = new JSONObject();
        json.put("ok", ok);
        json.put("action", action);
        json.put("contextReady", ctx != null);
        if (ctx != null) {
            try {
                ANTEngine engine = ctx.engine();
                json.put("engineState", engine.getEngineState());
                json.put("wakeup", engine.isWakeup());
                json.put("asr", engine.isASR());
                json.put("recognition", engine.isRecognition());
                json.put("ttsPlaying", engine.isTTSPlaying());
            } catch (Throwable t) {
                json.put("stateError", t.toString());
            }
        }
        if (error != null) {
            json.put("error", error);
        }
        return json;
    }

    private Response handleVolume(Request request) throws Exception {
        String path = pathOnly(request.path);
        DefaultVolumeOperator volumeOperator = DefaultVolumeOperator.getInstance(this.context);
        if (isPath(path, "/api/volume/up")) {
            volumeOperator.setVolumeRaise();
            return volumeResponse("up", null, null, true, null, 200);
        }
        if (isPath(path, "/api/volume/down")) {
            volumeOperator.setVolumeLower();
            return volumeResponse("down", null, null, true, null, 200);
        }
        if (isPath(path, "/api/volume/max")) {
            volumeOperator.setVolumeMax();
            return volumeResponse("max", Integer.valueOf(volumeOperator.getMaxVolume()), Integer.valueOf(100), true, null, 200);
        }
        if (isPath(path, "/api/volume/min")) {
            volumeOperator.setVolumeMin();
            return volumeResponse("min", Integer.valueOf(1), null, true, null, 200);
        }
        if (!isPath(path, "/api/volume/set")) {
            return new Response(404, null, null, null, null, new byte[0]);
        }

        String levelText = queryParam(request.path, "level");
        String percentText = queryParam(request.path, "percent");
        if (request.body.length > 0 && levelText == null && percentText == null) {
            try {
                JSONObject body = new JSONObject(new String(request.body, "UTF-8"));
                if (body.has("level")) {
                    levelText = body.optString("level", null);
                } else if (body.has("percent")) {
                    percentText = body.optString("percent", null);
                }
            } catch (Exception e) {
                return volumeResponse("set", null, null, false, "invalid json body", 400);
            }
        }

        try {
            if (levelText != null) {
                int level = Integer.parseInt(levelText);
                int max = volumeOperator.getMaxVolume();
                if (level < 1 || level > max) {
                    return volumeResponse("set", Integer.valueOf(level), null, false,
                            "level must be between 1 and " + max, 400);
                }
                volumeOperator.setVoiceVolume(level);
                return volumeResponse("set", Integer.valueOf(level), null, true, null, 200);
            }
            if (percentText != null) {
                int percent = Integer.parseInt(percentText);
                if (percent < 0 || percent > 100) {
                    return volumeResponse("set", null, Integer.valueOf(percent), false,
                            "percent must be between 0 and 100", 400);
                }
                int max = volumeOperator.getMaxVolume();
                int level = Math.max(1, Math.round(((float) max) * (((float) percent) / 100.0f)));
                volumeOperator.setVoiceVolume(level);
                return volumeResponse("set", Integer.valueOf(level), Integer.valueOf(percent), true, null, 200);
            }
        } catch (NumberFormatException e) {
            return volumeResponse("set", null, null, false, "invalid volume number", 400);
        }
        return volumeResponse("set", null, null, false,
                "missing percent or level parameter", 400);
    }

    private Response volumeResponse(String action, Integer requestedLevel, Integer requestedPercent,
                                    boolean ok, String error, int status) throws Exception {
        return jsonResponse(status,
                volumeStatusJson(action, requestedLevel, requestedPercent, ok, error));
    }

    private JSONObject volumeStatusJson(String action, Integer requestedLevel, Integer requestedPercent,
                                        boolean ok, String error) throws Exception {
        DefaultVolumeOperator volumeOperator = DefaultVolumeOperator.getInstance(this.context);
        int current = volumeOperator.getCurrentVolume();
        int max = volumeOperator.getMaxVolume();
        JSONObject json = new JSONObject();
        json.put("ok", ok);
        json.put("action", action);
        json.put("current", current);
        json.put("max", max);
        json.put("percent", max > 0 ? Math.round((((float) current) / ((float) max)) * 100.0f) : 0);
        if (requestedLevel != null) {
            json.put("requestedLevel", requestedLevel.intValue());
        }
        if (requestedPercent != null) {
            json.put("requestedPercent", requestedPercent.intValue());
        }
        if (error != null) {
            json.put("error", error);
        }
        return json;
    }

    private static String queryParam(String target, String name) {
        if (target == null) {
            return null;
        }
        int query = target.indexOf('?');
        if (query < 0 || query == target.length() - 1) {
            return null;
        }
        String[] pairs = target.substring(query + 1).split("&");
        for (String pair : pairs) {
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            if (name.equals(key)) {
                return equals >= 0 ? pair.substring(equals + 1) : "";
            }
        }
        return null;
    }

    private static boolean isBluetoothRoute(String method, String path) {
        return "POST".equals(method)
                && (path.equals("/api/bluetooth/on") || path.equals("/api/bluetooth/off"));
    }

    private static boolean isVolumeRoute(String method, String path) {
        return "POST".equals(method) && (path.equals("/api/volume/up") || path.equals("/api/volume/down")
                || path.equals("/api/volume/max") || path.equals("/api/volume/min") || path.equals("/api/volume/set"));
    }

    private static boolean isAsrRoute(String method, String path) {
        return "POST".equals(method) && path.equals("/api/asr/wakeup");
    }

    private static boolean isSleepRoute(String method, String path) {
        return "POST".equals(method)
                && (path.equals("/api/sleep/start") || path.equals("/api/sleep/end"));
    }

    private static boolean isPath(String path, String expect) {
        return expect.equals(path);
    }

    private static String pathOnly(String path) {
        if (path == null) {
            return "";
        }
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        return path;
    }

    private int forwardR1Frames(String uuid, byte[] body) {
        int pos = 0;
        int frames = 0;
        while (pos + 2 <= body.length) {
            int len = (body[pos] & 0xff) | ((body[pos + 1] & 0xff) << 8);
            pos += 2;
            if (len <= 0 || pos + len > body.length) {
                break;
            }
            byte[] packet = new byte[len];
            System.arraycopy(body, pos, packet, 0, len);
            pos += len;
            upstream.sendAudio(uuid, packet);
            frames++;
        }
        if (pos < body.length) {
            LogMgr.e(TAG, "drop incomplete r1 opus tail bytes=" + (body.length - pos));
        }
        return frames;
    }

    private synchronized ActiveSession getActive(String sid) {
        return active.get(sid);
    }

    private synchronized void putActive(String sid, ActiveSession session) {
        active.put(sid, session);
    }

    private synchronized void removeActive(String sid) {
        active.remove(sid);
    }

    private static Request readRequest(InputStream in) throws IOException {
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.length() == 0) {
            return null;
        }
        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return null;
        }
        String version = parts.length >= 3 ? parts[2] : "HTTP/1.0";
        Map<String, String> headers = new HashMap<String, String>();
        String line;
        while ((line = readLine(in)) != null && line.length() > 0) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(Locale.US), line.substring(idx + 1).trim());
            }
        }
        byte[] body = readBody(in, headers);
        return new Request(parts[0], parts[1], version, headers, body);
    }

    private static byte[] readBody(InputStream in, Map<String, String> headers) throws IOException {
        String transferEncoding = headers.get("transfer-encoding");
        if (transferEncoding != null && transferEncoding.toLowerCase(Locale.US).contains("chunked")) {
            return readChunkedBody(in);
        }
        String contentLength = headers.get("content-length");
        if (contentLength == null || contentLength.length() == 0) {
            return new byte[0];
        }
        int len = Integer.parseInt(contentLength);
        if (len <= 0) {
            return new byte[0];
        }
        if (len > MAX_BODY_BYTES) {
            throw new IOException("request body too large: " + len);
        }
        byte[] body = new byte[len];
        int off = 0;
        while (off < len) {
            int n = in.read(body, off, len - off);
            if (n < 0) {
                throw new IOException("unexpected eof reading body");
            }
            off += n;
        }
        return body;
    }

    private static byte[] readChunkedBody(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            String line = readLine(in);
            if (line == null) {
                break;
            }
            int semi = line.indexOf(';');
            String sizeText = semi >= 0 ? line.substring(0, semi) : line;
            int size = Integer.parseInt(sizeText.trim(), 16);
            if (size == 0) {
                while ((line = readLine(in)) != null && line.length() > 0) {
                }
                break;
            }
            if (out.size() + size > MAX_BODY_BYTES) {
                throw new IOException("chunked body too large");
            }
            byte[] chunk = new byte[size];
            int off = 0;
            while (off < size) {
                int n = in.read(chunk, off, size - off);
                if (n < 0) {
                    throw new IOException("unexpected eof reading chunk");
                }
                off += n;
            }
            out.write(chunk);
            readLine(in);
        }
        return out.toByteArray();
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b;
        boolean seenAny = false;
        while ((b = in.read()) >= 0) {
            seenAny = true;
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                out.write(b);
            }
        }
        if (!seenAny && out.size() == 0) {
            return null;
        }
        return out.toString("ISO-8859-1");
    }

    private static Response trafficResponse(String sid, String pn, String ct, String rs, byte[] body) {
        return new Response(200, sid, pn, ct, rs, body);
    }

    private static Response jsonResponse(int status, JSONObject json) throws Exception {
        return new Response(status, null, null, null, null,
                json.toString().getBytes("UTF-8"), "application/json; charset=UTF-8");
    }

    private static void writeResponse(OutputStream out, Response response, boolean keepAlive) throws IOException {
        byte[] body = response.body == null ? new byte[0] : response.body;
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.status).append(" \r\n");
        sb.append("Access-Control-Allow-Credentials: true\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
        if (response.contentType != null) {
            sb.append("Content-Type: ").append(response.contentType).append("\r\n");
        }
        if (response.ct != null) {
            sb.append("CT: ").append(response.ct).append("\r\n");
        }
        if (response.rs != null) {
            sb.append("RS: ").append(response.rs).append("\r\n");
        }
        if (response.pn != null) {
            sb.append("PN: ").append(response.pn).append("\r\n");
        }
        sb.append("Date: \r\n");
        sb.append("Server: \r\n");
        if (response.sid != null) {
            sb.append("SID: ").append(response.sid).append("\r\n");
        }
        sb.append("Content-Length: ").append(body.length).append("\r\n");
        sb.append("Connection: ").append(keepAlive ? "keep-alive" : "close").append("\r\n");
        sb.append("\r\n");
        out.write(sb.toString().getBytes("ISO-8859-1"));
        out.write(body);
        out.flush();
    }

    private static boolean shouldKeepAlive(Request request) {
        String connection = header(request.headers, "Connection");
        if (connection != null && containsToken(connection, "close")) {
            return false;
        }
        if ("HTTP/1.1".equalsIgnoreCase(request.version)) {
            return true;
        }
        return connection != null && containsToken(connection, "keep-alive");
    }

    private static boolean containsToken(String headerValue, String token) {
        String[] parts = headerValue.split(",");
        for (String part : parts) {
            if (token.equalsIgnoreCase(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private static String header(Map<String, String> headers, String name) {
        return headers.get(name.toLowerCase(Locale.US));
    }

    private static String extractSid(String p) {
        if (p == null) {
            return null;
        }
        int start = p.indexOf('[');
        int end = p.indexOf(']', start + 1);
        if (start < 0 || end <= start + 1) {
            return null;
        }
        return p.substring(start + 1, end).trim();
    }

    private static String initAckBody() {
        return "{\"returnCode\":0,\"uniCarRet\":{\"result\":{},\"returnCode\":609,\"message\":\"http post reuqest error\"}}";
    }

    private static String fakeNlu(String uuid) throws Exception {
        JSONObject json = new JSONObject();
        json.put("code", "ANSWER");
        json.put("originIntent", new JSONObject().put("nluSlotInfos", new org.json.JSONArray()));
        json.put("confidence", 1.0);
        json.put("modelIntentClsScore", new JSONObject());
        json.put("history", "cn.yunzhisheng.chat");
        json.put("source", "nlu");
        json.put("uniCarRet", new JSONObject().put("result", new JSONObject()).put("returnCode", 609).put("message", "http post reuqest error"));
        json.put("asr_recongize", uuid);
        json.put("rc", 0);
        json.put("general", new JSONObject().put("style", "faq").put("text", ".").put("type", "T"));
        json.put("returnCode", 0);
        json.put("retTag", "nlu");
        json.put("service", "cn.yunzhisheng.chat");
        json.put("nluProcessTime", "0");
        json.put("text", uuid);
        json.put("responseId", uuid);
        return json.toString();
    }

    private static String config(String key, String defaultValue) {
        String value = com.unisound.vui.common.config.a.a(key, defaultValue);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }

    private static final class ActiveSession {
        final String deviceId;
        final XiaoZhiTurn turn;
        int frames;

        ActiveSession(String deviceId, XiaoZhiTurn turn) {
            this.deviceId = deviceId;
            this.turn = turn;
        }
    }

    private static final class Request {
        final String method;
        final String path;
        final String version;
        final Map<String, String> headers;
        final byte[] body;

        Request(String method, String path, String version, Map<String, String> headers, byte[] body) {
            this.method = method;
            this.path = path;
            this.version = version;
            this.headers = headers;
            this.body = body;
        }
    }

    private static final class Response {
        final int status;
        final String sid;
        final String pn;
        final String ct;
        final String rs;
        final byte[] body;
        final String contentType;

        Response(int status, String sid, String pn, String ct, String rs, byte[] body) {
            this(status, sid, pn, ct, rs, body, null);
        }

        Response(int status, String sid, String pn, String ct, String rs, byte[] body,
                 String contentType) {
            this.status = status;
            this.sid = sid;
            this.pn = pn;
            this.ct = ct;
            this.rs = rs;
            this.body = body;
            this.contentType = contentType;
        }
    }
}
