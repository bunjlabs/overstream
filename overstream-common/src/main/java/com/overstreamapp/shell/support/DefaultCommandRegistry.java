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

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.AppInfo;
import com.overstreamapp.shell.Command;
import com.overstreamapp.shell.CommandBuilder;
import com.overstreamapp.shell.CommandRegistry;
import com.overstreamapp.shell.event.CommandEvent;
import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventKeeper;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultCommandRegistry implements CommandRegistry {
    private final Logger logger;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final CommandParser commandParser = new CommandParser();
    private final Event<CommandEvent> commandEvent;

    @Inject
    public DefaultCommandRegistry(Logger logger, EventKeeper eventKeeper, AppInfo appInfo) {
        this.logger = logger;
        this.commandEvent = eventKeeper.eventBuilder(CommandEvent.class).build();

        builder("help", "list").function(() -> "List of available commands:\n" + String.join("\n", listCommands())).build();
        builder("version").function(() -> appInfo.name() + "-" + appInfo.version()).build();
        builder("echo").function(a -> a.stream().map(Object::toString).collect(Collectors.joining(" "))).build();
        builder("nop").function(() -> "").build();
    }

    @Override
    public CommandBuilder builder(String... aliases) {
        return new DefaultCommandBuilder(this, aliases);
    }

    @Override
    public Object executeFlat(String fullCommand) {
        if (fullCommand.isBlank()) {
            return null;
        }
        try {
            var request = commandParser.parse(fullCommand);

            return execute(request.getCommand(), request.getArguments(), request.getNamedArguments());
        } catch (CommandParserException e) {
            return " ".repeat(e.getColumn() - 1) + "^\nError: " + e.getLocalizedMessage();
        }
    }

    @Override
    public Object execute(String name, List<Object> arguments, Map<String, Object> namedArguments) {
        var command = commands.get(name);

        if (command == null) {
            logger.debug("Command not found: {}", name);
            return "Error: " + name + " not found";
        }

        Object result;

        try {
            logger.info("Execute command: {} {} {}", name, arguments, namedArguments);
            result = command.execute(arguments, namedArguments);
        } catch (Throwable t) {
            logger.debug("Error while executing command", t);
            result = "Error:" + t.getMessage();
        }

        commandEvent.fire(new CommandEvent(name, arguments, namedArguments, result));

        return result;
    }

    void register(Command command) {
        var aliases = command.getAliases();

        logger.debug("Register command: {} ", aliases);

        aliases.forEach(alias -> {
            if (!commands.containsKey(alias)) {
                commands.put(alias, command);
            }
        });
    }

    private List<String> listCommands() {
        return this.commands.keySet().stream()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }
}
