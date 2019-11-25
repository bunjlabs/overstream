package com.overstreamapp.messageserver.messages;

public abstract class Message {

    private final long timestamp;
    private final String type;

    public Message(String type) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }
}
