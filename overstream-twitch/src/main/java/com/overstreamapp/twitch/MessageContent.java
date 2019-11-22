package com.overstreamapp.twitch;

public class MessageContent {

    private final Type type;
    private final String value;

    public MessageContent(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public enum Type {
        EMOTE, TEXT
    }
}
