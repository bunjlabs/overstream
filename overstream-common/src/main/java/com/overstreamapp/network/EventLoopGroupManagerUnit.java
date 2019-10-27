package com.overstreamapp.network;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class EventLoopGroupManagerUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(EventLoopGroupSettings.class).auto();
        c.bind(DefaultEventLoopGroupManager.class).auto().in(Singleton.class);
        c.bind(EventLoopGroupManager.class).to(DefaultEventLoopGroupManager.class);
    }
}
