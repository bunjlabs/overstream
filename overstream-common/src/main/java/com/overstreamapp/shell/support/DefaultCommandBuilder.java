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
import com.overstreamapp.shell.CommandBuilder;
import com.overstreamapp.shell.CommandFunction;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

class DefaultCommandBuilder implements CommandBuilder {
    private final DefaultCommandRegistry commandRegistry;
    private final Set<String> aliases = new HashSet<>();
    private CommandFunction function;

    DefaultCommandBuilder(DefaultCommandRegistry commandRegistry, String... aliases) {
        this.commandRegistry = commandRegistry;
        this.aliases.addAll(Arrays.asList(aliases));
    }

    @Override
    public CommandBuilder alias(String... name) {
        aliases.addAll(Arrays.asList(name));
        return this;
    }

    @Override
    public CommandBuilder alias(Collection<String> names) {
        aliases.addAll(names);
        return this;
    }

    @Override
    public CommandBuilder function(CommandFunction function) {
        this.function = function;
        return this;
    }

    @Override
    public CommandBuilder function(Function<List<Object>, Object> function) {
        this.function = (a, n) -> function.apply(a);
        return this;
    }

    @Override
    public CommandBuilder function(Supplier<Object> function) {
        this.function = (a, n) -> function.get();
        return this;
    }

    @Override
    public Command build() {
        var command = new CommandImpl(aliases, function);
        commandRegistry.register(command);
        return command;
    }
}
