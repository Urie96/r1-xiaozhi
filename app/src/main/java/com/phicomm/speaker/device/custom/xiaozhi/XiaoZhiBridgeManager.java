package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;

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

public final class XiaoZhiBridgeManager {
    private static final String TAG = "XiaoZhiBridge";
    private static final String LOCAL_TR_ADDR = "127.0.0.1:8089";
    private static final int LOCAL_TR_PORT = 8089;
    private static final int MAX_BODY_BYTES = 2 * 1024 * 1024;
    private static XiaoZhiBridgeManager instance;

    private final XiaoZhiUpstreamClient upstream;
    private final Map<String, ActiveSession> active = new HashMap<String, ActiveSession>();
    private volatile boolean serverStarted;

    private XiaoZhiBridgeManager(Context context) {
        this.upstream = new XiaoZhiUpstreamClient(config("xiaozhi_ws", "ws://home.lubui.com:3116/ws"), config("xiaozhi_token", "dev-token"));
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

    private void serveLoop() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(LOCAL_TR_PORT);
            LogMgr.d(TAG, "local http listening on " + LOCAL_TR_ADDR);
            while (true) {
                Socket socket = serverSocket.accept();
                handleSocket(socket);
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
            Request request = readRequest(socket.getInputStream());
            if (request == null) {
                return;
            }
            Response response;
            if ("POST".equals(request.method) && "/trafficRouter/cs".equals(request.path)) {
                response = handleTraffic(request);
            } else if ("GET".equals(request.method) && "/health".equals(request.path)) {
                response = new Response(200, null, null, null, null, "{\"status\":\"ok\"}".getBytes("UTF-8"));
            } else {
                response = new Response(404, null, null, null, null, new byte[0]);
            }
            writeResponse(socket.getOutputStream(), response);
        } catch (SocketTimeoutException e) {
            LogMgr.e(TAG, "local http timeout: " + e.toString());
        } catch (Throwable t) {
            LogMgr.e(TAG, "handle local http failed: " + t.toString());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
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
        Map<String, String> headers = new HashMap<String, String>();
        String line;
        while ((line = readLine(in)) != null && line.length() > 0) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                headers.put(line.substring(0, idx).trim().toLowerCase(Locale.US), line.substring(idx + 1).trim());
            }
        }
        byte[] body = readBody(in, headers);
        return new Request(parts[0], parts[1], headers, body);
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

    private static void writeResponse(OutputStream out, Response response) throws IOException {
        byte[] body = response.body == null ? new byte[0] : response.body;
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(response.status).append(" \r\n");
        sb.append("Access-Control-Allow-Credentials: true\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
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
        sb.append("\r\n");
        out.write(sb.toString().getBytes("ISO-8859-1"));
        out.write(body);
        out.flush();
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
        final Map<String, String> headers;
        final byte[] body;

        Request(String method, String path, Map<String, String> headers, byte[] body) {
            this.method = method;
            this.path = path;
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

        Response(int status, String sid, String pn, String ct, String rs, byte[] body) {
            this.status = status;
            this.sid = sid;
            this.pn = pn;
            this.ct = ct;
            this.rs = rs;
            this.body = body;
        }
    }
}
