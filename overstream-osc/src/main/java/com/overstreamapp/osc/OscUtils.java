package com.overstreamapp.osc;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class OscUtils {

    private static final int ALIGN_MULTIPLIER = 4;
    private static final byte[] BUNDLE_START_BYTES = "#bundle".getBytes(StandardCharsets.UTF_8);

    public static int padding(ByteBuffer buffer) {
        return (ALIGN_MULTIPLIER - buffer.position() % ALIGN_MULTIPLIER) % ALIGN_MULTIPLIER;
    }

    public static void alignSkip(ByteBuffer buffer) {
        buffer.position(buffer.position() + padding(buffer));
    }

    public static void alignWrite(ByteBuffer buffer) {
        int padding = padding(buffer);
        while (padding-- > 0) {
            buffer.put((byte) 0);
        }
    }

    public static boolean isTypeTagSeparator(ByteBuffer buffer) {
        return buffer.get(buffer.position()) == ((byte) ',');
    }

    public static void writeTypeTagSeparator(ByteBuffer buffer) {
        buffer.put((byte) ',');
    }

    public static boolean isBundle(ByteBuffer buffer) {
        return compareBuffer(buffer, BUNDLE_START_BYTES);
    }

    public static boolean compareBuffer(ByteBuffer buffer, byte[] byteArray) {
        if (buffer.remaining() < byteArray.length) {
            return false;
        }

        for (int i = 0; i < byteArray.length; i++) {
            if (buffer.get(buffer.position() + i) == byteArray[i]) {
                return true;
            }
        }

        return false;
    }
}
