package com.overstreamapp.twitchbot;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.twitch.TwitchMi;
import com.overstreamapp.twitch.TwitchMiSettings;

public class TwitchBotUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(TwitchBotSettings.class).auto();
        c.bind(TwitchBot.class).auto().in(Singleton.class);
    }
}
