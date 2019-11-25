package com.overstreamapp.groovy;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;

public class GroovyUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(GroovyScriptsSettings.class).auto();
        c.bind(GroovyRuntime.class).auto().in(Singleton.class);
        c.bind(GroovyScripts.class).auto().in(Singleton.class);
    }
}
