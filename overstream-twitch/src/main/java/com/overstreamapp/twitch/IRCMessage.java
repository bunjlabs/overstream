package com.overstreamapp.twitch;

import java.util.Collections;
import java.util.Map;

public class IRCMessage {

    private final Map<String, String> tags;
    private final String clientName;
    private final String command;
    private final String channelName;
    private final String message;
    private final String rawMessage;

    public IRCMessage(String rawMessage) {
        this.tags = Collections.emptyMap();
        this.clientName = "";
        this.command = "";
        this.channelName = "";
        this.message = "";
        this.rawMessage = rawMessage;
    }

    public IRCMessage(Map<String, String> tags, String clientName, String command, String channelName, String message, String rawMessage) {
        this.tags = tags;
        this.clientName = clientName;
        this.command = command;
        this.channelName = channelName;
        this.message = message;
        this.rawMessage = rawMessage;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public String getClientName() {
        return clientName;
    }

    public String getCommand() {
        return command;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getMessage() {
        return message;
    }

    public String getRawMessage() {
        return rawMessage;
    }
}
