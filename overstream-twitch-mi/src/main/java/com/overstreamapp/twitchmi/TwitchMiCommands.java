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

package com.overstreamapp.twitchmi;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.shell.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.twitchmi.state.TwitchChat;

import java.util.List;
import java.util.Map;

public class TwitchMiCommands {

    private final TwitchMi twitch;
    private final CommandRegistry commandRegistry;

    private final Store<TwitchChat> chatStore;

    @Inject
    public TwitchMiCommands(TwitchMi twitch, StoreKeeper storeKeeper, CommandRegistry commandRegistry) {
        this.twitch = twitch;
        this.commandRegistry = commandRegistry;

        this.chatStore = storeKeeper.getStore(TwitchChat.class);
    }

    void registerCommands() {
        commandRegistry.builder("twitchmi.state").function(this::state).build();
        commandRegistry.builder("twitchmi.connect").function(this::connect).build();
        commandRegistry.builder("twitchmi.disconnect").function(this::disconnect).build();
        commandRegistry.builder("twitchmi.reconnect").function(this::reconnect).build();

        commandRegistry.builder("twitchmi.say").function(this::say).build();
        commandRegistry.builder("twitchmi.last").function(this::lastMessage).build();
    }

    private String state() {
        return "TwitchMI: " + twitch.getConnectionState().name();
    }

    private String connect() {
        twitch.connect();
        return "ok";
    }

    private String disconnect() {
        twitch.disconnect();
        return "ok";
    }

    private String reconnect() {
        twitch.reconnect();
        return "ok";
    }

    private String say(List<Object> arguments, Map<String, Object> namedArguments) {
        if (arguments.size() < 1) {
            return "text arguments is required";
        }

        var text = arguments.get(0).toString();
        if (namedArguments.containsKey("channel")) {
            var channel = namedArguments.get("channel");
            twitch.sendMessage(channel.toString(), text);
        } else {
            twitch.sendMessage(text);
        }

        return "ok";
    }

    private String lastMessage() {
        var msg = chatStore.getState().getMessage();
        return String.format("Last message: @%s <%s>: %s", msg.getChannelName(), msg.getUserName(), msg.getText());
    }
}
