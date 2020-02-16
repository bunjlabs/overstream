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
import com.overstreamapp.shell.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.x32mixer.state.X32ChannelGate;
import com.overstreamapp.x32mixer.state.X32ChannelOn;

import java.util.List;

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
        commandRegistry.builder("x32.channel.on").function(this::channelOn).build();
        commandRegistry.builder("x32.bus.on").function(this::busOn).build();
        commandRegistry.builder("x32.state").function(this::state).build();
    }

    private String channelOn(List<Object> arguments) {
        if (arguments.size() < 2) {
            return "using: ch on";
        }

        var ch = (int) arguments.get(0);
        var on = (boolean) arguments.get(1);

        mixer.channelOn(ch, on);
        return String.format("channel %02d is %s", ch, on ? "on" : "off");
    }

    private String busOn(List<Object> arguments) {
        if (arguments.size() < 2) {
            return "using: bus on";
        }

        var bus = (int) arguments.get(0);
        var on = (boolean) arguments.get(1);

        mixer.busOn(bus, on);
        return String.format("bus %02d is %s", bus, on ? "on" : "off");
    }

    private String state() {
        var channelOn = channelOnStore.getState().getChannels();
        var channelGate = channelGateStore.getState().getChannels();

        var sb = new StringBuilder("\n");
        sb.append("Channels:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %02d", i + 1));
        sb.append('\n');
        sb.append("      On:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %s", channelOn[i] ? "+" : " "));
        sb.append('\n');
        sb.append("    Gate:");
        for (int i = 0; i < 32; i++) sb.append(String.format(" %s", channelGate[i] ? "+" : " "));

        return sb.toString();
    }
}
