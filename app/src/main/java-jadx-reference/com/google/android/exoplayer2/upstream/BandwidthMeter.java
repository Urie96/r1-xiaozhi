package com.google.android.exoplayer2.upstream;

/* JADX INFO: loaded from: classes.dex */
public interface BandwidthMeter {
    public static final long NO_ESTIMATE = -1;

    public interface EventListener {
        void onBandwidthSample(int i, long j, long j2);
    }

    long getBitrateEstimate();
}
