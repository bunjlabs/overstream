package com.overstreamapp.twitchbot;


import com.overstreamapp.twitch.ChatMessage;

public class CommandContext {

    private final ChatMessage message;
    private final String channel;
    private final String command;
    private final String[] args;
    private final CommandReference reference;

    public CommandContext(ChatMessage message, String channel, String command, String[] args, CommandReference reference) {
        this.message = message;
        this.channel = channel;
        this.command = command;
        this.args = args;
        this.reference = reference;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public String getChannel() {
        return channel;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public CommandReference getReference() {
        return reference;
    }
}
