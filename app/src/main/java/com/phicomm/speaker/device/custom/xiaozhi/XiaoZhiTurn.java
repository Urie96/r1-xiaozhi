package com.phicomm.speaker.device.custom.xiaozhi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class XiaoZhiTurn {
    static final int EVENT_AUDIO = 1;
    static final int EVENT_FINISH = 2;
    static final int EVENT_ERROR = 3;

    static final class Event {
        final int type;
        final byte[] data;
        final String message;

        private Event(int type, byte[] data, String message) {
            this.type = type;
            this.data = data;
            this.message = message;
        }

        static Event audio(byte[] data) {
            return new Event(EVENT_AUDIO, data, null);
        }

        static Event finish() {
            return new Event(EVENT_FINISH, null, null);
        }

        static Event error(String message) {
            return new Event(EVENT_ERROR, null, message);
        }
    }

    private final String uuid;
    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private volatile int audioFrames;

    XiaoZhiTurn(String uuid) {
        this.uuid = uuid;
    }

    public String uuid() {
        return uuid;
    }

    int audioFrames() {
        return audioFrames;
    }

    void offerAudio(byte[] data) {
        if (data == null || data.length == 0 || completed.get()) {
            return;
        }
        audioFrames++;
        queue.offer(Event.audio(data));
    }

    void finish() {
        if (completed.compareAndSet(false, true)) {
            queue.offer(Event.finish());
        }
    }

    void fail(String message) {
        if (completed.compareAndSet(false, true)) {
            queue.offer(Event.error(message));
        }
    }

    public void cancel() {
        if (completed.compareAndSet(false, true)) {
            queue.clear();
            queue.offer(Event.finish());
        }
    }

    Event take() throws InterruptedException {
        return queue.take();
    }
}
