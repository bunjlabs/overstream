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

package com.overstreamapp.obs;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.commands.CommandRegistry;

import java.util.Map;

public class ObsCommands {

    private final ObsClient obs;
    private final CommandRegistry commandRegistry;

    @Inject
    public ObsCommands(ObsClient obs, CommandRegistry commandRegistry) {
        this.obs = obs;
        this.commandRegistry = commandRegistry;
    }

    void registerCommands() {
        commandRegistry.builder("obs.state").command(this::state).build();
        commandRegistry.builder("obs.connect").command(this::connect).build();
        commandRegistry.builder("obs.disconnect").command(this::disconnect).build();
        commandRegistry.builder("obs.reconnect").command(this::reconnect).build();
    }

    private String state(Map<String, Object> parameters) {
        return "OBS: " + obs.getConnectionState().name();
    }

    private String connect(Map<String, Object> parameters) {
        obs.connect();
        return "OK";
    }

    private String disconnect(Map<String, Object> parameters) {
        obs.disconnect();
        return "OK";
    }

    private String reconnect(Map<String, Object> parameters) {
        obs.reconnect();
        return "OK";
    }
}
