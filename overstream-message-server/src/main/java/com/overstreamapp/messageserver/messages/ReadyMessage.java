package com.overstreamapp.messageserver.messages;

public class ReadyMessage extends Message {
    private String server;

    public ReadyMessage(String server) {
        super("Ready");
        this.server = server;
    }
}
