package com.overstreamapp.messagebus;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class MessageBusUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(SimpleMessageBus.class).auto().in(Singleton.class);
        c.bind(MessageBus.class).to(SimpleMessageBus.class);
    }
}
