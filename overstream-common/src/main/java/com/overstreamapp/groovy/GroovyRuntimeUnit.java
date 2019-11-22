package com.overstreamapp.groovy;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;

public class GroovyRuntimeUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(GroovyRuntime.class).auto();
    }
}
