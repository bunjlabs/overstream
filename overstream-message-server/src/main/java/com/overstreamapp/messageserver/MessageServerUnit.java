package com.overstreamapp.messageserver;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;

public class MessageServerUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(MessageServerSettings.class).auto();
        c.bind(MessageServer.class).auto();
    }
}
