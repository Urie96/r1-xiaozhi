package com.google.android.exoplayer2.metadata;

/* JADX INFO: loaded from: classes.dex */
public interface MetadataDecoder {
    Metadata decode(MetadataInputBuffer metadataInputBuffer) throws MetadataDecoderException;
}
