package com.overstreamapp.x32mixer.messages;

import com.overstreamapp.messagebus.Message;

public class ChannelGateMessage extends Message {

    private final int channel;
    private final float gate;

    public ChannelGateMessage(Object source, int channel, float gate) {
        super(source);
        this.channel = channel;
        this.gate = gate;
    }

    @Override
    public String typeName() {
        return "MixerChannelGate";
    }
}
