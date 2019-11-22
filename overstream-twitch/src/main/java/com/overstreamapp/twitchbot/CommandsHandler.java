package com.overstreamapp.twitchbot;

import java.util.List;

public abstract class CommandsHandler {

    private long lastCooldown;

    public CommandsHandler() {
        this.lastCooldown = 0;
    }

    public void setCooldownTime(long time) {
        this.lastCooldown = time;
    }

    public boolean checkCooldown(long cooldown, long time) {
        return time - this.lastCooldown > cooldown;
    }

    public abstract List<CommandReference> register();
}
