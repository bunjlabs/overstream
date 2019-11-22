package com.overstreamapp.twitchbot;

import com.bunjlabs.fuga.util.Assert;

import java.util.Objects;
import java.util.function.BiConsumer;

public class CommandReference {
    private final String command;
    private final int level;
    private final long cooldown;
    private final CommandsHandler handler;
    private final BiConsumer<CommandContext, String[]> consumer;

    public CommandReference(String command, int level, long cooldown, CommandsHandler handler, BiConsumer<CommandContext, String[]> consumer) {
        Assert.isTrue(level >= 0, "level must be greater or equal to zero");
        Assert.isTrue(cooldown >= 0, "cooldown must be greater equal to zero");

        this.command = Assert.notNull(command);
        this.level = level;
        this.cooldown = cooldown;
        this.handler = Assert.notNull(handler);
        this.consumer = Assert.notNull(consumer);
    }

    public String getCommand() {
        return command;
    }

    public int getLevel() {
        return level;
    }

    public long getCooldown() {
        return cooldown;
    }

    public CommandsHandler getHandler() {
        return handler;
    }

    public BiConsumer<CommandContext, String[]> getConsumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandReference that = (CommandReference) o;
        return command.equals(that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command);
    }

    @Override
    public String toString() {
        return "CommandReference{" +
                "command='" + command + '\'' +
                ", level=" + level +
                ", cooldown=" + cooldown +
                ", consumer=" + consumer +
                '}';
    }
}
