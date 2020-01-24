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

package com.overstreamapp.ympd;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.ympd.state.PlayerSong;
import com.overstreamapp.ympd.state.PlayerState;

import java.util.Map;

public class YmpdCommands {

    private final YmpdClient ympd;
    private final StoreKeeper storeKeeper;
    private final CommandRegistry commandRegistry;
    private final Store<PlayerSong> songStore;
    private final Store<PlayerState> stateStore;

    @Inject
    public YmpdCommands(YmpdClient ympd, StoreKeeper storeKeeper, CommandRegistry commandRegistry) {
        this.ympd = ympd;
        this.storeKeeper = storeKeeper;
        this.commandRegistry = commandRegistry;
        this.songStore = storeKeeper.getStore(PlayerSong.class);
        this.stateStore = storeKeeper.getStore(PlayerState.class);
    }

    void registerCommands() {
        commandRegistry.builder("ympd.state").command(this::state).build();
        commandRegistry.builder("ympd.connect").command(this::connect).build();
        commandRegistry.builder("ympd.disconnect").command(this::disconnect).build();
        commandRegistry.builder("ympd.reconnect").command(this::reconnect).build();

        commandRegistry.builder("ympd.play").command(this::play).build();
        commandRegistry.builder("ympd.pause").command(this::pause).build();
        commandRegistry.builder("ympd.next").command(this::next).build();
        commandRegistry.builder("ympd.prev").command(this::prev).build();
        commandRegistry.builder("ympd.song", "ympd.current").command(this::song).build();
    }

    private String state(Map<String, Object> parameters) {
        return "YMPD: " + ympd.getConnectionState().name();
    }

    private String connect(Map<String, Object> parameters) {
        ympd.connect();
        return "OK";
    }

    private String disconnect(Map<String, Object> parameters) {
        ympd.disconnect();
        return "OK";
    }

    private String reconnect(Map<String, Object> parameters) {
        ympd.reconnect();
        return "OK";
    }

    private String play(Map<String, Object> p) {
        ympd.play();
        return "OK";
    }

    private String pause(Map<String, Object> p) {
        ympd.pause();
        return "OK";
    }

    private String next(Map<String, Object> p) {
        ympd.next();
        return "OK";
    }

    private String prev(Map<String, Object> p) {
        ympd.prev();
        return "OK";
    }

    private String song(Map<String, Object> p) {
        var song = songStore.getState();
        var state = stateStore.getState();
        return String.format("%s by %s (state: %d, time %d:%d)",
                song.getTitle(),
                song.getArtist(),
                state.getState(),
                state.getElapsedTime(),
                state.getTotalTime());
    }
}
