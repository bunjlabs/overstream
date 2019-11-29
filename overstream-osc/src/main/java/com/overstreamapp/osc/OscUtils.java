/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
