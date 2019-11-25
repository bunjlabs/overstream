package com.overstreamapp.groovy;

import com.bunjlabs.fuga.inject.Inject;
import org.slf4j.Logger;

import java.io.File;

public class GroovyScripts {
    private final Logger logger;

    private final GroovyRuntime runtime;
    private final GroovyScriptsSettings settings;

    @Inject
    public GroovyScripts(Logger logger, GroovyRuntime runtime, GroovyScriptsSettings settings) {
        this.logger = logger;
        this.runtime = runtime;
        this.settings = settings;
    }

    public void start() {
        for (String script : settings.run()) {
            try {
                logger.debug("Evaluating script {}", script);
                runtime.getGroovyShell().evaluate(new File(script));
            } catch (Throwable e) {
                logger.error("Unable to evaluate groovy script {}", script, e);
            }
        }
    }
}
