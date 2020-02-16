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

package com.overstreamapp.shell.event;

import java.util.List;
import java.util.Map;

public class CommandEvent {
    private String command;
    private List<Object> arguments;
    private Map<String, Object> namedArguments;
    private Object result;

    public CommandEvent() {
    }

    public CommandEvent(String command, List<Object> arguments, Map<String, Object> namedArguments, Object result) {
        this.command = command;
        this.arguments = arguments;
        this.namedArguments = namedArguments;
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public Map<String, Object> getNamedArguments() {
        return namedArguments;
    }

    public Object getResult() {
        return result;
    }
}
