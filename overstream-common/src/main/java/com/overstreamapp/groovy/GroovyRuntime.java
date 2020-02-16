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

import com.bunjlabs.fuga.context.ApplicationContext;
import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Injector;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import org.codehaus.groovy.control.CompilerConfiguration;

public class GroovyRuntime {

    private final GroovyShell groovyShell;

    @Inject
    public GroovyRuntime(ApplicationContext context, Injector injector) {
        Binding binding = new Binding();
        binding.setProperty("Context", context);
        binding.setProperty("Injector", injector);

        this.groovyShell = new GroovyShell(binding);

    }

    public GroovyShell getGroovyShell() {
        return groovyShell;
    }

    public void export(String name, Object value) {
        this.groovyShell.setProperty(name, value);
    }
}
