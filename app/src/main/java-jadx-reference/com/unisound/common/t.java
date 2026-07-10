package com.unisound.common;

/* JADX INFO: loaded from: classes.dex */
public class t {
    public static int a(String str) {
        try {
            return Integer.valueOf(str).intValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
