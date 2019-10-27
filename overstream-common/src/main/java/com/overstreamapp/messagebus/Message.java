package com.overstreamapp.messagebus;

import java.util.EventObject;

public abstract class Message extends EventObject {

    private final long timestamp;

    public Message(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public abstract String typeName();
}
