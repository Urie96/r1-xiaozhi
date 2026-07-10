package com.unisound.vui.util;

/* JADX INFO: loaded from: classes.dex */
public interface AttributeMap {
    <T> Attribute<T> attr(AttributeKey<T> attributeKey);

    <T> boolean hasAttr(AttributeKey<T> attributeKey);
}
