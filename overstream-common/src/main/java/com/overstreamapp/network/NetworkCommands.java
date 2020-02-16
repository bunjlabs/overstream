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

package com.overstreamapp.network;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.shell.CommandRegistry;
import io.netty.channel.Channel;

import java.util.Formatter;
import java.util.Locale;

public class NetworkCommands {

    private final CommandRegistry commandRegistry;
    private final EventLoopGroupManager loopGroupManager;
    private final ConnectionRegistry connectionRegistry;

    @Inject
    public NetworkCommands(CommandRegistry commandRegistry, EventLoopGroupManager loopGroupManager, ConnectionRegistry connectionRegistry) {
        this.commandRegistry = commandRegistry;
        this.loopGroupManager = loopGroupManager;
        this.connectionRegistry = connectionRegistry;
    }

    public void registerCommands() {
        commandRegistry.builder("net.connections").function(this::listConnections).build();

    }

    private String listConnections() {
        var channels = connectionRegistry.getSnapshot();

        var sb = new StringBuilder();
        var f = new Formatter(sb, Locale.ROOT);
        var format = "%-4.4s %-10.10s %-50.50s %-50.50s %-10.10s%n";

        f.format("Current connections (%d)%n", channels.size());
        f.format(format, "No", "Id", "Local", "Remote", "State");

        var no = 1;
        for (Channel ch : channels) {
            f.format(format, no,
                    ch.id().asShortText(),
                    ch.localAddress(),
                    ch.remoteAddress(),
                    switchState(ch));

            no++;
        }

        return sb.toString();
    }

    private static String switchState(Channel channel) {
        if(channel.isActive()) {
            return "Active";
        } else if(channel.isOpen()) {
            return "Open";
        } else if(channel.isRegistered()) {
            return "Registered";
        }

        return "n/a";
    }
}
