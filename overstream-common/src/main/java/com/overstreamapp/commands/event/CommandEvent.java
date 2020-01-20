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

package com.overstreamapp.commands.event;

import java.util.Map;

public class CommandEvent {
    private String command;
    private Map<String, Object> params;
    private String result;

    public CommandEvent() {
    }

    public CommandEvent(String command, Map<String, Object> params, String result) {
        this.command = command;
        this.params = params;
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getResult() {
        return result;
    }
}
