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

package com.overstreamapp.x32mixer;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.x32mixer.state.X32ChannelGate;
import com.overstreamapp.x32mixer.state.X32ChannelOn;

import java.util.Map;

public class X32MixerCommands {

    private final StoreKeeper storeKeeper;
    private final CommandRegistry commandRegistry;
    private final Store<X32ChannelOn> channelOnStore;
    private final Store<X32ChannelGate> channelGateStore;
    private X32MixerClient mixer;

    @Inject
    public X32MixerCommands(X32MixerClient mixer, StoreKeeper storeKeeper, CommandRegistry commandRegistry) {
        this.mixer = mixer;
        this.storeKeeper = storeKeeper;
        this.commandRegistry = commandRegistry;

        this.channelOnStore = storeKeeper.getStore(X32ChannelOn.class);
        this.channelGateStore = storeKeeper.getStore(X32ChannelGate.class);
    }

    void registerCommands() {
        commandRegistry.builder("x32.channel.on").command(this::channelOn).build();
        commandRegistry.builder("x32.bus.on").command(this::busOn).build();
        commandRegistry.builder("x32.state").command(this::state).build();
    }

    private String channelOn(Map<String, Object> parameters) {
        if (parameters.containsKey("ch") && parameters.containsKey("on")) {
            mixer.channelOn((int) parameters.get("ch"), (boolean) parameters.get("on"));
            return "OK";
        } else {
            return "parameter ch and on is required";
        }
    }

    private String busOn(Map<String, Object> parameters) {
        if (parameters.containsKey("bus") && parameters.containsKey("on")) {
            mixer.busOn((int) parameters.get("bus"), (boolean) parameters.get("on"));
            return "OK";
        } else {
            return "parameter bus and on is required";
        }
    }

    private String state(Map<String, Object> parameters) {
        var channelOn = channelOnStore.getState().getChannels();
        var channelGate = channelGateStore.getState().getChannels();

        var sb = new StringBuilder("\n");
        sb.append("CHANNELS:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %02d", i + 1));
        sb.append('\n');
        sb.append("   CH ON:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %s", channelOn[i] ? "+" : " "));
        sb.append('\n');
        sb.append(" CH GATE:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %s", channelGate[i] ? "+" : " "));

        return sb.toString();
    }
}
