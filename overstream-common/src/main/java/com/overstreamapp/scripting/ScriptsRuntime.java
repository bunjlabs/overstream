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

package com.overstreamapp.scripting;

import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.largo.*;
import org.slf4j.Logger;

public class ScriptsRuntime {

    private final Logger logger;
    private final LargoRuntime runtime;

    @Inject
    public ScriptsRuntime(Logger logger) {
        this.logger = logger;
        this.runtime = new LargoRuntimeBuilder()
                .withLoader(new FileLargoLoader())
                .withEnvironment(new DefaultLargoEnvironment())
                .build();

        exportStdApi();
    }

    private void exportStdApi() {
        var env = this.runtime.getEnvironment();

        env.addModule("Math", new LargoMathModule());
    }

    public void evaluate(String name) {
        try {
            this.runtime.load(name);
        } catch (Throwable e) {
            logger.warn("Unable to load script", e);
        }
    }

    public void export(LargoBinding binding) {
        binding.bindTo(this.runtime.getEnvironment());
    }
}
