package com.overstreamapp.osc;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.osc.netty.NettyOscClient;

public class NettyOscClientUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(NettyOscClient.class).auto();
        c.bind(OscClient.class).to(NettyOscClient.class);
    }
}
