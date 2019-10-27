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
    private final X32MixerState state;

    X32OscHandler(X32Mixer mixer) {
        state = new X32MixerState();
        this.mixer = mixer;
    }

    @Override
    public void onMessage(OscChannel channel, OscMessage message) {
        if (message.getAddress().isEmpty() || message.getArguments().isEmpty()) {
            return;
        }

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

}
