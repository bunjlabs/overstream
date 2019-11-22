package com.overstreamapp.twitch;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class TwitchMiUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(TwitchMiSettings.class).auto();
        c.bind(TwitchMi.class).auto().in(Singleton.class);
    }
}
