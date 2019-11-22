package com.overstreamapp.mpd;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class YmpdClientUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(YmpdClientSettings.class).auto();
        c.bind(YmpdClient.class).auto().in(Singleton.class);
    }
}
