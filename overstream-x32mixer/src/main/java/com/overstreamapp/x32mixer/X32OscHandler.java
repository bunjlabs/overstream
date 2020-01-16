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

package com.overstreamapp.x32mixer;

import com.overstreamapp.osc.OscChannel;
import com.overstreamapp.osc.OscHandler;
import com.overstreamapp.osc.types.OscBlob;
import com.overstreamapp.osc.types.OscBundle;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class X32OscHandler implements OscHandler {

    private final X32Mixer mixer;
    private long lastMessageTime = 0;

    X32OscHandler(X32Mixer mixer) {
        this.mixer = mixer;
    }

    @Override
    public void onMessage(OscChannel channel, OscMessage message) {
        if (message.getAddress().isEmpty() || message.getArguments().isEmpty()) {
            return;
        }

        lastMessageTime = System.currentTimeMillis();

        String address = message.getAddress();

        if (address.startsWith("/meters/")) {
            OscBlob blob = (OscBlob) message.getArguments().get(0);

            ByteBuffer buffer = ByteBuffer.wrap(blob.getValue());
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            int nativeFloatsNumber = buffer.getInt();
            float[] nativeFloats = new float[nativeFloatsNumber];

            for (int i = 0; i < nativeFloatsNumber; i++) {
                nativeFloats[i] = buffer.getFloat();
            }

            mixer.fireMetersUpdate(nativeFloats);
        } else if (address.startsWith("/ch/")) {
            OscType value = message.getArguments().get(0);
            mixer.fireSubscriptionUpdate(address, value);
        }

    }

    @Override
    public void onBundle(OscChannel channel, OscBundle bundle) {
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }
}
