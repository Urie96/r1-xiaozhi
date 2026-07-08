package com.phicomm.speaker.device.custom.xiaozhi;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Minimal Ogg Opus muxer for the Xiaozhi websocket path.
 *
 * xiaozhi-server-rs sends raw Opus packets over websocket binary frames. Android
 * players generally expect an audio container, so we collect one response into a
 * temporary Ogg Opus file and hand that file to ExoPlayer.
 */
final class OggOpusWriter implements Closeable {
    private static final byte[] OGG_CAPTURE = new byte[]{'O', 'g', 'g', 'S'};
    private static final int OPUS_GRANULE_RATE = 48000;
    private static final int DEFAULT_FRAME_DURATION_MS = 60;
    private static final int DEFAULT_FRAME_SAMPLES = OPUS_GRANULE_RATE * DEFAULT_FRAME_DURATION_MS / 1000;
    private static final int[] CRC_TABLE = buildCrcTable();

    private final File file;
    private final FileOutputStream out;
    private final int serial;
    private int sequence;
    private long granulePosition;
    private boolean closed;

    OggOpusWriter(File file, int inputSampleRate, int channels) throws IOException {
        this.file = file;
        this.out = new FileOutputStream(file);
        this.serial = new Random().nextInt();
        this.sequence = 0;
        this.granulePosition = 0;
        writePage(0x02, 0, opusHead(inputSampleRate, channels));
        writePage(0x00, 0, opusTags());
    }

    File getFile() {
        return file;
    }

    synchronized void writeOpusPacket(byte[] packet) throws IOException {
        if (closed) {
            return;
        }
        if (packet == null || packet.length == 0) {
            return;
        }
        granulePosition += DEFAULT_FRAME_SAMPLES;
        writePage(0x00, granulePosition, packet);
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        // Emit an explicit EOS page. Some extractors tolerate missing EOS, but
        // writing it makes the temporary file look like a normal finite stream.
        writePage(0x04, granulePosition, new byte[0]);
        out.flush();
        out.close();
        closed = true;
    }

    synchronized void abort() {
        if (!closed) {
            try {
                out.close();
            } catch (IOException ignored) {
            }
            closed = true;
        }
    }

    private void writePage(int headerType, long granule, byte[] packet) throws IOException {
        int segmentCount = packet.length == 0 ? 0 : ((packet.length + 254) / 255);
        if (packet.length > 0 && packet.length % 255 == 0) {
            segmentCount++;
        }
        if (segmentCount > 255) {
            throw new IOException("Opus packet too large for a single Ogg page: " + packet.length);
        }

        byte[] page = new byte[27 + segmentCount + packet.length];
        int p = 0;
        System.arraycopy(OGG_CAPTURE, 0, page, p, OGG_CAPTURE.length);
        p += OGG_CAPTURE.length;
        page[p++] = 0; // stream structure version
        page[p++] = (byte) headerType;
        putLongLE(page, p, granule);
        p += 8;
        putIntLE(page, p, serial);
        p += 4;
        putIntLE(page, p, sequence++);
        p += 4;
        // checksum placeholder, filled after payload is copied
        putIntLE(page, p, 0);
        p += 4;
        page[p++] = (byte) segmentCount;

        int remaining = packet.length;
        for (int i = 0; i < segmentCount; i++) {
            int lace = Math.min(remaining, 255);
            page[p++] = (byte) lace;
            remaining -= lace;
        }
        System.arraycopy(packet, 0, page, p, packet.length);

        int crc = checksum(page);
        putIntLE(page, 22, crc);
        out.write(page);
    }

    private static byte[] opusHead(int inputSampleRate, int channels) {
        byte[] head = new byte[19];
        byte[] magic = new byte[]{'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
        System.arraycopy(magic, 0, head, 0, magic.length);
        head[8] = 1; // OpusHead version
        head[9] = (byte) channels;
        putShortLE(head, 10, 0); // pre-skip
        putIntLE(head, 12, inputSampleRate);
        putShortLE(head, 16, 0); // output gain
        head[18] = 0; // channel mapping family
        return head;
    }

    private static byte[] opusTags() {
        byte[] vendor = "feixun-r1-xiaozhi".getBytes();
        byte[] tags = new byte[8 + 4 + vendor.length + 4];
        byte[] magic = new byte[]{'O', 'p', 'u', 's', 'T', 'a', 'g', 's'};
        System.arraycopy(magic, 0, tags, 0, magic.length);
        putIntLE(tags, 8, vendor.length);
        System.arraycopy(vendor, 0, tags, 12, vendor.length);
        putIntLE(tags, 12 + vendor.length, 0); // user comment list length
        return tags;
    }

    private static void putShortLE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xff);
        data[offset + 1] = (byte) ((value >> 8) & 0xff);
    }

    private static void putIntLE(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xff);
        data[offset + 1] = (byte) ((value >> 8) & 0xff);
        data[offset + 2] = (byte) ((value >> 16) & 0xff);
        data[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private static void putLongLE(byte[] data, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            data[offset + i] = (byte) ((value >> (8 * i)) & 0xff);
        }
    }

    private static int[] buildCrcTable() {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            int r = i << 24;
            for (int j = 0; j < 8; j++) {
                if ((r & 0x80000000) != 0) {
                    r = (r << 1) ^ 0x04c11db7;
                } else {
                    r <<= 1;
                }
            }
            table[i] = r;
        }
        return table;
    }

    private static int checksum(byte[] data) {
        int crc = 0;
        for (byte b : data) {
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) & 0xff) ^ (b & 0xff)];
        }
        return crc;
    }
}
