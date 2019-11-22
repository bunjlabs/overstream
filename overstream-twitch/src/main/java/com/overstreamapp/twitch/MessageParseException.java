package com.overstreamapp.twitch;

public class MessageParseException extends Exception {
    public MessageParseException() {
    }

    public MessageParseException(String message) {
        super(message);
    }

    public MessageParseException(Throwable cause) {
        super(cause);
    }
}
