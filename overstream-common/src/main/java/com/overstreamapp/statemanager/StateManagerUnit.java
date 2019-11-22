package com.overstreamapp.statemanager;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class StateManagerUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(StateManagerSettings.class).auto();

        c.bind(DefaultStateManager.class).auto().in(Singleton.class);
        c.bind(StateManager.class).to(DefaultStateManager.class);
    }
}
