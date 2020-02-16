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
import com.overstreamapp.shell.CommandRegistry;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.ympd.state.PlayerSong;
import com.overstreamapp.ympd.state.PlayerState;

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
        commandRegistry.builder("ympd.state").function(this::state).build();
        commandRegistry.builder("ympd.connect").function(this::connect).build();
        commandRegistry.builder("ympd.disconnect").function(this::disconnect).build();
        commandRegistry.builder("ympd.reconnect").function(this::reconnect).build();

        commandRegistry.builder("ympd.play").function(this::play).build();
        commandRegistry.builder("ympd.pause").function(this::pause).build();
        commandRegistry.builder("ympd.next").function(this::next).build();
        commandRegistry.builder("ympd.prev").function(this::prev).build();
        commandRegistry.builder("ympd.song", "ympd.current").function(this::song).build();
    }

    private String state() {
        return "YMPD: " + ympd.getConnectionState().name();
    }

    private String connect() {
        ympd.connect();
        return "ok";
    }

    private String disconnect() {
        ympd.disconnect();
        return "ok";
    }

    private String reconnect() {
        ympd.reconnect();
        return "ok";
    }

    private String play() {
        ympd.play();
        return "ok";
    }

    private String pause() {
        ympd.pause();
        return "ok";
    }

    private String next() {
        ympd.next();
        return "ok";
    }

    private String prev() {
        ympd.prev();
        return "ok";
    }

    private String song() {
        var song = songStore.getState();
        var state = stateStore.getState();
        return String.format("%s by %s (state: %d, time %d:%d)",
                song.getTitle(), song.getArtist(), state.getState(),
                state.getElapsedTime(), state.getTotalTime());
    }
}
