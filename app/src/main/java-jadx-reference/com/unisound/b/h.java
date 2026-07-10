package com.unisound.b;

import cn.yunzhisheng.asr.JniUscClient;
import java.lang.reflect.Type;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/* JADX INFO: loaded from: classes.dex */
public class h {
    public static double a(JSONObject jSONObject, String str, double d) {
        if (jSONObject == null || !jSONObject.has(str)) {
            return d;
        }
        try {
            return jSONObject.getDouble(str);
        } catch (JSONException e) {
            e.printStackTrace();
            return d;
        }
    }

    public static int a(JSONObject jSONObject, String str, int i) {
        if (jSONObject == null || !jSONObject.has(str)) {
            return i;
        }
        try {
            return jSONObject.getInt(str);
        } catch (JSONException e) {
            e.printStackTrace();
            return i;
        }
    }

    public static <T> Object a(String str, Class<T> cls) {
        return null;
    }

    public static Object a(String str, Type type) {
        return null;
    }

    public static String a(Object obj) {
        return null;
    }

    public static String a(JSONObject jSONObject, String str, String str2) {
        if (jSONObject == null || !jSONObject.has(str)) {
            return str2;
        }
        try {
            String string = jSONObject.getString(str);
            return (string == null || string.equals("")) ? str2 : string.equals(JniUscClient.az) ? str2 : string;
        } catch (JSONException e) {
            e.printStackTrace();
            return str2;
        }
    }

    public static JSONObject a(String str) {
        JSONObject jSONObject = new JSONObject();
        try {
            Object objNextValue = new JSONTokener(str).nextValue();
            return (objNextValue == null || !(objNextValue instanceof JSONObject)) ? jSONObject : (JSONObject) objNextValue;
        } catch (Exception e) {
            return jSONObject;
        }
    }

    public static JSONObject a(JSONArray jSONArray, int i) {
        if (jSONArray == null) {
            return null;
        }
        try {
            if (i < jSONArray.length()) {
                return jSONArray.getJSONObject(i);
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject a(JSONObject jSONObject, String str) {
        if (jSONObject == null || !jSONObject.has(str)) {
            return null;
        }
        try {
            return jSONObject.getJSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void a(JSONObject jSONObject, String str, Object obj) {
        if (obj != null) {
            try {
                if ("".equals(obj)) {
                    return;
                }
                jSONObject.put(str, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean a(JSONObject jSONObject, String str, boolean z) {
        if (jSONObject == null || !jSONObject.has(str)) {
            return z;
        }
        try {
            return jSONObject.getBoolean(str);
        } catch (JSONException e) {
            e.printStackTrace();
            return z;
        }
    }

    public static String b(JSONObject jSONObject, String str) {
        if (jSONObject != null && jSONObject.has(str)) {
            try {
                return jSONObject.getString(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x002f  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static JSONArray b(String str) {
        JSONArray jSONArray;
        JSONArray jSONArray2 = new JSONArray();
        if (str == null || "".equals(str)) {
            return jSONArray2;
        }
        try {
            Object objNextValue = new JSONTokener(str).nextValue();
            if (objNextValue == null) {
                jSONArray = jSONArray2;
            } else if (objNextValue instanceof JSONObject) {
                jSONArray2.put((JSONObject) objNextValue);
                jSONArray = jSONArray2;
            } else if (objNextValue instanceof JSONArray) {
                jSONArray = (JSONArray) objNextValue;
            }
            return jSONArray;
        } catch (Exception e) {
            return jSONArray2;
        }
    }

    public static void b(JSONObject jSONObject, String str, String str2) {
        if (str2 != null) {
            try {
                if ("".equals(str2)) {
                    return;
                }
                jSONObject.put(str, str2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONArray c(JSONObject jSONObject, String str) {
        if (jSONObject != null && jSONObject.has(str)) {
            try {
                return jSONObject.getJSONArray(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
