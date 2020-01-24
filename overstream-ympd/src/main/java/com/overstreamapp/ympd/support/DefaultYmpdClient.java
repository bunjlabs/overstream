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

package com.overstreamapp.ympd.support;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.store.ValueAction;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import com.overstreamapp.ympd.YmpdClient;
import com.overstreamapp.ympd.YmpdClientSettings;
import com.overstreamapp.ympd.YmpdConnectionState;
import com.overstreamapp.ympd.state.PlayerSong;
import com.overstreamapp.ympd.state.PlayerState;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class DefaultYmpdClient implements YmpdClient {
    private final Logger logger;

    private final YmpdClientSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final Handler webSocketHandler;
    private final ObjectMapper mapper;
    private final Store<PlayerSong> songStore;
    private final Store<PlayerState> stateStore;

    private volatile YmpdConnectionState state = YmpdConnectionState.DISCONNECTED;
    private WebSocket webSocket;

    @Inject
    public DefaultYmpdClient(Logger logger, YmpdClientSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, StoreKeeper storeKeeper) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.mapper = new ObjectMapper();

        this.songStore = storeKeeper.storeBuilder(PlayerSong.class).persistence(settings.historySize()).build();
        this.stateStore = storeKeeper.storeBuilder(PlayerState.class).build();
    }

    @Override
    public void connect() {
        if (state == YmpdConnectionState.DISCONNECTED || state == YmpdConnectionState.RECONNECTING) {
            logger.info("Connecting to YMPD {} ...", settings.serverUri());

            this.state = YmpdConnectionState.CONNECTING;

            this.webSocketClient.connect(URI.create(settings.serverUri()), webSocketHandler, settings.connectTimeout());
        }
    }

    @Override
    public void disconnect() {
        if (state != YmpdConnectionState.DISCONNECTED) {
            state = YmpdConnectionState.DISCONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
        }
    }

    @Override
    public void reconnect() {
        if (state != YmpdConnectionState.RECONNECTING) {
            state = YmpdConnectionState.RECONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, settings.reconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public YmpdConnectionState getConnectionState() {
        return state;
    }

    @Override
    public void play() {
        send("MPD_API_SET_PLAY");
    }

    @Override
    public void pause() {
        send("MPD_API_SET_PAUSE");
    }

    @Override
    public void next() {
        send("MPD_API_SET_NEXT");
    }

    @Override
    public void prev() {
        send("MPD_API_SET_PREV");
    }

    private void send(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    private void onData(String type, JsonNode data) {
        logger.debug("Received message: {}", type);

        if ("song_change".equals(type)) {
            var song = new PlayerSong(
                    data.get("pos").asInt(0),
                    data.get("title").asText(""),
                    data.get("artist").asText(""),
                    data.get("album").asText(""));

            this.songStore.dispatch(new ValueAction(song));
        } else if ("state".equals(type)) {
            var state = new PlayerState(
                    data.get("state").asInt(0),
                    data.get("totalTime").asInt(0),
                    data.get("elapsedTime").asInt(0));

            this.stateStore.dispatch(new ValueAction(state));
        }
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            DefaultYmpdClient.this.webSocket = socket;
            state = YmpdConnectionState.CONNECTED;
            logger.info("Connected to YMPD {}", settings.serverUri());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != YmpdConnectionState.DISCONNECTING && state != YmpdConnectionState.RECONNECTING) {
                logger.info("Connection to YMPD lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = YmpdConnectionState.DISCONNECTED;
                logger.info("Disconnected from YMPD: {} {}", code, reason);
            }
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Received raw message: {}", message);

            try {
                JsonNode root = mapper.readTree(message);

                if (root != null && root.isObject() && root.has("type") && root.has("data")) {
                    var type = root.get("type").asText("");
                    var data = root.get("data");

                    onData(type, data);
                }
            } catch (Exception e) {
                logger.error("Error parsing message", e);
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable ex) {
            logger.error("Error", ex);
        }

        @Override
        public void onStart() {
            logger.debug("Web socket client started");
        }
    }


}
