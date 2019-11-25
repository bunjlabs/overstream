package com.overstreamapp.obs;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;

public class ObsUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(ObsSettings.class).auto();
        c.bind(ObsClient.class).auto();
    }
}
