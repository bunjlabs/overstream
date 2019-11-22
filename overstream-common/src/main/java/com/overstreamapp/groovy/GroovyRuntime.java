package com.overstreamapp.groovy;

import com.bunjlabs.fuga.context.ApplicationContext;
import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Injector;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyRuntime {

    private final GroovyShell groovyShell;

    @Inject
    public GroovyRuntime(ApplicationContext context, Injector injector) {
        Binding binding = new Binding();
        binding.setProperty("context", context);
        binding.setProperty("injector", injector);

        this.groovyShell = new GroovyShell(binding);
    }

    public GroovyShell getGroovyShell() {
        return groovyShell;
    }
}
