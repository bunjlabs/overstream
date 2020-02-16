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

package com.overstreamapp.shell.support;

import com.overstreamapp.shell.Command;
import com.overstreamapp.shell.CommandFunction;

import java.util.List;
import java.util.Map;
import java.util.Set;

class CommandImpl implements Command {
    private final Set<String> aliases;
    private final CommandFunction function;

    CommandImpl(Set<String> aliases, CommandFunction function) {
        this.aliases = aliases;
        this.function = function;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public Object execute(List<Object> arguments, Map<String, Object> namedArguments) {
        try {
            return function.execute(arguments, namedArguments);
        } catch (Throwable t) {
            return "Error: " + t.getMessage();
        }
    }
}
