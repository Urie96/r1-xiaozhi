package com.google.android.exoplayer2.util;

/* JADX INFO: loaded from: classes.dex */
public final class SystemClock implements Clock {
    @Override // com.google.android.exoplayer2.util.Clock
    public long elapsedRealtime() {
        return android.os.SystemClock.elapsedRealtime();
    }
}
