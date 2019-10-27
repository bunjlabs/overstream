package com.overstreamapp.messageserver.messages;

import com.overstreamapp.messagebus.Message;

public class RawWebSocketMessage extends Message {

    private final String rawMessage;

    public RawWebSocketMessage(Object source, String rawMessage) {
        super(source);
        this.rawMessage = rawMessage;
    }

    @Override
    public String typeName() {
        return "RawWebSocketMessage";
    }
}
