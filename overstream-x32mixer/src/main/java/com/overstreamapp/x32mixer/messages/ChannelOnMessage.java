package com.overstreamapp.x32mixer.messages;

import com.overstreamapp.messagebus.Message;

public class ChannelOnMessage extends Message {

    private final int channel;
    private final boolean enabled;

    public ChannelOnMessage(Object source, int channel, boolean enabled) {
        super(source);
        this.channel = channel;
        this.enabled = enabled;
    }

    @Override
    public String typeName() {
        return "MixerChannelOn";
    }
}
