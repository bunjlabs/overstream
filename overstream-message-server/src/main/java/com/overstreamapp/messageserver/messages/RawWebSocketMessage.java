package com.overstreamapp.messageserver.messages;


public class RawWebSocketMessage extends Message {

    private final String rawMessage;

    public RawWebSocketMessage(String rawMessage) {
        super();
        this.rawMessage = rawMessage;
    }

    @Override
    public String typeName() {
        return "RawWebSocketMessage";
    }
}
