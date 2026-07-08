package com.phicomm.speaker.device.custom.xiaozhi;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.unisound.vui.util.LogMgr;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

final class XiaoZhiStreamDataSource implements DataSource {
    private static final String TAG = "XiaoZhiDataSource";
    private static final byte[] END = new byte[0];

    private final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
    private final Uri uri = Uri.parse("xiaozhi://stream.ogg");
    private byte[] current;
    private int currentOffset;
    private volatile boolean closed;
    private volatile boolean opened;

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        LogMgr.d(TAG, "open");
        this.closed = false;
        this.opened = true;
        this.current = null;
        this.currentOffset = 0;
        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }
        if (closed) {
            return C.RESULT_END_OF_INPUT;
        }
        try {
            while (current == null || currentOffset >= current.length) {
                current = queue.take();
                currentOffset = 0;
                if (current == END) {
                    LogMgr.d(TAG, "end of stream");
                    return C.RESULT_END_OF_INPUT;
                }
            }
            int copy = Math.min(readLength, current.length - currentOffset);
            System.arraycopy(current, currentOffset, buffer, offset, copy);
            currentOffset += copy;
            return copy;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("xiaozhi stream interrupted", e);
        }
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws IOException {
        LogMgr.d(TAG, "close");
        closed = true;
        opened = false;
        queue.clear();
        queue.offer(END);
    }

    void feed(byte[] data) {
        if (data == null || data.length == 0 || closed) {
            return;
        }
        queue.offer(data);
    }

    void finish() {
        if (!closed) {
            queue.offer(END);
        }
    }

    void cancel() {
        try {
            close();
        } catch (IOException ignored) {
        }
    }

    boolean isOpened() {
        return opened;
    }
}
