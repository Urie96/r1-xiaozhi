package com.unisound.vui.util.internal;

/* JADX INFO: loaded from: classes.dex */
public final class c {
    public static String a(Class<?> cls) {
        String name = ((Class) ObjectUtil.checkNotNull(cls, "clazz")).getName();
        int iLastIndexOf = name.lastIndexOf(46);
        return iLastIndexOf > -1 ? name.substring(iLastIndexOf + 1) : name;
    }
}
