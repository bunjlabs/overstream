/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.overstreamapp.groovy;

import com.bunjlabs.fuga.inject.Inject;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class GroovyScripts {
    private final Logger logger;

    private final GroovyRuntime runtime;
    private final GroovyScriptsSettings settings;
    private final Path baseDirectory;

    @Inject
    public GroovyScripts(Logger logger, GroovyRuntime runtime, GroovyScriptsSettings settings) {
        this.logger = logger;
        this.runtime = runtime;
        this.settings = settings;

        this.baseDirectory = Path.of(settings.baseDirectory());
    }

    public void start() {
        for (String script : settings.load()) {
            var scriptFile = baseDirectory.resolve(String.format("%s.groovy", script)).toFile();

            if (scriptFile.isFile() && scriptFile.canRead()) {
                try {
                    logger.debug("Evaluating script {}", scriptFile);
                    runtime.getGroovyShell().evaluate(scriptFile);
                } catch (Throwable e) {
                    logger.error("Unable to evaluate groovy script {}", scriptFile, e);
                }
            }

        }
    }
}
