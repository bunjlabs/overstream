package com.overstreamapp.osc;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.osc.nio.NioOscClient;

public class NioOscClientUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(NioOscClient.class).auto();
        c.bind(OscClient.class).to(NioOscClient.class);
    }
}
