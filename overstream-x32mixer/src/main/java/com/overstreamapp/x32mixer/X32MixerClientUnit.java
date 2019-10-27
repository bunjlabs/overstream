package com.overstreamapp.x32mixer;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;

public class X32MixerClientUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(X32MixerSettings.class).auto();
        c.bind(X32MixerClient.class).auto();
    }
}
