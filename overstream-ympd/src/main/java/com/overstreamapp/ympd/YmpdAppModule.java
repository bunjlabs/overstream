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

package com.overstreamapp.ympd;

import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Injector;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.AppModule;
import com.overstreamapp.groovy.GroovyRuntime;

import java.util.Collections;
import java.util.List;

public class YmpdAppModule implements AppModule {

    private final GroovyRuntime groovyRuntime;

    @Inject
    public YmpdAppModule(GroovyRuntime groovyRuntime) {
        this.groovyRuntime = groovyRuntime;
    }

    @Override
    public List<Unit> getUnits() {
        return Collections.singletonList(new YmpdClientUnit());
    }

    @Override
    public void init(Injector injector) {
        injector.getInstance(YmpdCommands.class).registerCommands();
        groovyRuntime.export("Ympd", injector.getInstance(YmpdClient.class));
    }
}
