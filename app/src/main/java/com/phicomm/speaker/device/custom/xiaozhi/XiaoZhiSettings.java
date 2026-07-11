package com.phicomm.speaker.device.custom.xiaozhi;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Iterator;

public final class XiaoZhiSettings {
    public static final int DEFAULT_VAD_FRONT_SILENCE_MS = 20000;
    public static final int DEFAULT_VAD_BACK_SILENCE_MS = 400;
    public static final long DEFAULT_ENTER_ASR_DELAY_MS = 500L;
    public static final boolean DEFAULT_MULTI_TURN_ENABLED = true;

    private static final String PREFERENCES_NAME = "xiaozhi_settings";
    private static final String KEY_VAD_FRONT_SILENCE_MS = "vadFrontSilenceMs";
    private static final String KEY_VAD_BACK_SILENCE_MS = "vadBackSilenceMs";
    private static final String KEY_WS_URL = "wsUrl";
    private static final String KEY_ENTER_ASR_DELAY_MS = "enterAsrDelayMs";
    private static final String KEY_MULTI_TURN_ENABLED = "multiTurnEnabled";

    public final int vadFrontSilenceMs;
    public final int vadBackSilenceMs;
    public final String wsUrl;
    public final long enterAsrDelayMs;
    public final boolean multiTurnEnabled;

    private XiaoZhiSettings(int vadFrontSilenceMs, int vadBackSilenceMs, String wsUrl,
                            long enterAsrDelayMs, boolean multiTurnEnabled) {
        this.vadFrontSilenceMs = vadFrontSilenceMs;
        this.vadBackSilenceMs = vadBackSilenceMs;
        this.wsUrl = wsUrl;
        this.enterAsrDelayMs = enterAsrDelayMs;
        this.multiTurnEnabled = multiTurnEnabled;
    }

    public static XiaoZhiSettings load(Context context, String defaultWsUrl) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return new XiaoZhiSettings(
                preferences.getInt(KEY_VAD_FRONT_SILENCE_MS, DEFAULT_VAD_FRONT_SILENCE_MS),
                preferences.getInt(KEY_VAD_BACK_SILENCE_MS, DEFAULT_VAD_BACK_SILENCE_MS),
                preferences.getString(KEY_WS_URL, defaultWsUrl),
                preferences.getLong(KEY_ENTER_ASR_DELAY_MS, DEFAULT_ENTER_ASR_DELAY_MS),
                preferences.getBoolean(KEY_MULTI_TURN_ENABLED, DEFAULT_MULTI_TURN_ENABLED));
    }

    public static XiaoZhiSettings update(Context context, String defaultWsUrl, JSONObject json) throws Exception {
        rejectUnknownKeys(json);
        XiaoZhiSettings current = load(context, defaultWsUrl);
        int vadFrontSilenceMs = readInt(json, KEY_VAD_FRONT_SILENCE_MS,
                current.vadFrontSilenceMs, 1000, 120000);
        int vadBackSilenceMs = readInt(json, KEY_VAD_BACK_SILENCE_MS,
                current.vadBackSilenceMs, 100, 5000);
        long enterAsrDelayMs = readLong(json, KEY_ENTER_ASR_DELAY_MS,
                current.enterAsrDelayMs, 0L, 10000L);
        String wsUrl = readWsUrl(json, current.wsUrl);
        boolean multiTurnEnabled = readBoolean(json, KEY_MULTI_TURN_ENABLED, current.multiTurnEnabled);

        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_VAD_FRONT_SILENCE_MS, vadFrontSilenceMs);
        editor.putInt(KEY_VAD_BACK_SILENCE_MS, vadBackSilenceMs);
        editor.putString(KEY_WS_URL, wsUrl);
        editor.putLong(KEY_ENTER_ASR_DELAY_MS, enterAsrDelayMs);
        editor.putBoolean(KEY_MULTI_TURN_ENABLED, multiTurnEnabled);
        if (!editor.commit()) {
            throw new IllegalStateException("save settings failed");
        }
        return new XiaoZhiSettings(vadFrontSilenceMs, vadBackSilenceMs, wsUrl,
                enterAsrDelayMs, multiTurnEnabled);
    }

    public JSONObject toJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put(KEY_VAD_FRONT_SILENCE_MS, vadFrontSilenceMs);
        json.put(KEY_VAD_BACK_SILENCE_MS, vadBackSilenceMs);
        json.put(KEY_WS_URL, wsUrl);
        json.put(KEY_ENTER_ASR_DELAY_MS, enterAsrDelayMs);
        json.put(KEY_MULTI_TURN_ENABLED, multiTurnEnabled);
        return json;
    }

    public boolean sameAs(XiaoZhiSettings other) {
        return other != null
                && vadFrontSilenceMs == other.vadFrontSilenceMs
                && vadBackSilenceMs == other.vadBackSilenceMs
                && enterAsrDelayMs == other.enterAsrDelayMs
                && multiTurnEnabled == other.multiTurnEnabled
                && wsUrl.equals(other.wsUrl);
    }

    private static void rejectUnknownKeys(JSONObject json) {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!KEY_VAD_FRONT_SILENCE_MS.equals(key)
                    && !KEY_VAD_BACK_SILENCE_MS.equals(key)
                    && !KEY_WS_URL.equals(key)
                    && !KEY_ENTER_ASR_DELAY_MS.equals(key)
                    && !KEY_MULTI_TURN_ENABLED.equals(key)) {
                throw new IllegalArgumentException("unknown setting: " + key);
            }
        }
    }

    private static int readInt(JSONObject json, String key, int current, int min, int max) throws Exception {
        if (!json.has(key)) {
            return current;
        }
        Object value = json.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(key + " must be a number");
        }
        long number = ((Number) value).longValue();
        if (number < min || number > max) {
            throw new IllegalArgumentException(key + " must be between " + min + " and " + max);
        }
        return (int) number;
    }

    private static long readLong(JSONObject json, String key, long current, long min, long max) throws Exception {
        if (!json.has(key)) {
            return current;
        }
        Object value = json.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(key + " must be a number");
        }
        long number = ((Number) value).longValue();
        if (number < min || number > max) {
            throw new IllegalArgumentException(key + " must be between " + min + " and " + max);
        }
        return number;
    }

    private static boolean readBoolean(JSONObject json, String key, boolean current) throws Exception {
        if (!json.has(key)) {
            return current;
        }
        Object value = json.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException(key + " must be a boolean");
        }
        return ((Boolean) value).booleanValue();
    }

    private static String readWsUrl(JSONObject json, String current) throws Exception {
        if (!json.has(KEY_WS_URL)) {
            return current;
        }
        Object value = json.get(KEY_WS_URL);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(KEY_WS_URL + " must be a string");
        }
        String wsUrl = ((String) value).trim();
        if (!(wsUrl.startsWith("ws://") || wsUrl.startsWith("wss://")
                || wsUrl.startsWith("http://") || wsUrl.startsWith("https://"))) {
            throw new IllegalArgumentException(KEY_WS_URL + " must use ws, wss, http, or https");
        }
        return wsUrl;
    }
}
