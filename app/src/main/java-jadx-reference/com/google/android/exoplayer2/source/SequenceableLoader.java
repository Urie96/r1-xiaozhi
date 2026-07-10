package com.google.android.exoplayer2.source;

/* JADX INFO: loaded from: classes.dex */
public interface SequenceableLoader {

    public interface Callback<T extends SequenceableLoader> {
        void onContinueLoadingRequested(T t);
    }

    boolean continueLoading(long j);

    long getNextLoadPositionUs();
}
