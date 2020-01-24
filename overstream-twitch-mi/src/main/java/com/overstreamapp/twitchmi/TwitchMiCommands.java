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
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.twitchmi.state.TwitchChat;

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
        commandRegistry.builder("twitchmi.state").command(this::state).build();
        commandRegistry.builder("twitchmi.connect").command(this::connect).build();
        commandRegistry.builder("twitchmi.disconnect").command(this::disconnect).build();
        commandRegistry.builder("twitchmi.reconnect").command(this::reconnect).build();

        commandRegistry.builder("twitchmi.say").command(this::say).build();
        commandRegistry.builder("twitchmi.last").command(this::lastMessage).build();
    }

    private String state(Map<String, Object> parameters) {
        return "TwitchMI: " + twitch.getConnectionState().name();
    }

    private String connect(Map<String, Object> parameters) {
        twitch.connect();
        return "OK";
    }

    private String disconnect(Map<String, Object> parameters) {
        twitch.disconnect();
        return "OK";
    }

    private String reconnect(Map<String, Object> parameters) {
        twitch.reconnect();
        return "OK";
    }

    private String say(Map<String, Object> parameters) {
        if (!parameters.containsKey("text")) {
            return "text parameter is required";
        }

        var text = parameters.get("text").toString();
        if (parameters.containsKey("channel")) {
            var channel = parameters.get("channel");
            twitch.sendMessage(channel.toString(), text);
        } else {
            twitch.sendMessage(text);
        }

        return "OK";
    }

    private String lastMessage(Map<String, Object> parameters) {
        var msg = chatStore.getState().getMessage();
        return String.format("Last message: @%s <%s>: %s", msg.getChannelName(), msg.getUserName(), msg.getText());
    }
}
