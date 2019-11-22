package com.overstreamapp.messageserver.messages;

public abstract class Message {
    private final long timestamp;

    public Message() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public abstract String typeName();
}
