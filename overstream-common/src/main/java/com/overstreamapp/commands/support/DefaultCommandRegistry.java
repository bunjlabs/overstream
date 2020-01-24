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

package com.overstreamapp.commands.support;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.AppInfo;
import com.overstreamapp.commands.Command;
import com.overstreamapp.commands.CommandBuilder;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.commands.event.CommandEvent;
import com.overstreamapp.commands.parser.CommandParser;
import com.overstreamapp.commands.parser.CommandParserException;
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

        builder("help", "list").command(p -> "List of available commands:\n" + String.join("\n", listCommands())).build();
        builder("version").command(p -> appInfo.name() + "-" + appInfo.version()).build();
        builder("echo").command(p -> p.getOrDefault("text", "").toString()).build();
        builder("nop").command(p -> "").build();
    }

    @Override
    public CommandBuilder builder(String... aliases) {
        return new DefaultCommandBuilder(this, aliases);
    }

    @Override
    public String executeFlat(String fullCommand) {
        if (fullCommand.isBlank()) {
            return null;
        }
        try {
            var commandRequest = commandParser.parse(fullCommand);

            return execute(commandRequest.getCommand(), commandRequest.getParams());
        } catch (CommandParserException e) {
            return " ".repeat(e.getColumn() - 1) + "^\nError: " + e.getLocalizedMessage();
        }
    }

    @Override
    public String execute(String name, Map<String, Object> parameters) {
        var command = commands.get(name);

        if (command == null) {
            logger.debug("Command not found: {}", name);
            return "Error: " + name + " not found";
        }

        String result;

        try {
            logger.info("Execute command: {} {}", name, parameters);
            result = command.execute(parameters);
        } catch (Throwable t) {
            logger.debug("Error while executing command", t);
            result = "Error:" + t.getMessage();
        }

        commandEvent.fire(new CommandEvent(name, parameters, result));

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
