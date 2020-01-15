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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.keeper.Keeper;
import com.overstreamapp.keeper.State;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import com.overstreamapp.ympd.state.PlayerState;
import com.overstreamapp.ympd.state.SongState;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class YmpdClient {
    private final Logger logger;

    private final YmpdClientSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final Handler webSocketHandler;
    private final ObjectMapper mapper;
    private final State<SongState> songState;
    private final State<PlayerState> playerState;

    private ConnectionState state = ConnectionState.DISCONNECTED;
    private WebSocket webSocket;

    @Inject
    public YmpdClient(Logger logger, YmpdClientSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, Keeper keeper) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.mapper = new ObjectMapper();

        this.songState = keeper.stateBuilder(SongState.class).persistenceListCapped().history(settings.historySize()).build();
        this.playerState = keeper.stateBuilder(PlayerState.class).persistenceTransient().build();
    }

    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.RECONNECTING) {

            this.state = ConnectionState.CONNECTING;

            this.webSocketClient.connect(URI.create(settings.serverUri()), webSocketHandler);
        }
    }

    public void disconnect() {
        if (state != ConnectionState.DISCONNECTED) {
            state = ConnectionState.DISCONNECTING;
            this.webSocket.close();
            this.webSocket = null;
        }
    }

    public void reconnect() {
        state = ConnectionState.RECONNECTING;
        disconnect();
        loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, 2, TimeUnit.SECONDS);
    }

    private void onData(String type, JsonNode data) {
        logger.debug("Received message: {}", data);

        if ("song_change".equals(type)) {
            SongState song = new SongState(
                    data.get("pos").asInt(0),
                    data.get("title").asText(""),
                    data.get("artist").asText(""),
                    data.get("album").asText("")
            );

            this.songState.push(song);
        } else if ("state".equals(type)) {
            this.playerState.push(new PlayerState(
                    data.get("state").asInt(0),
                    data.get("totalTime").asInt(0),
                    data.get("elapsedTime").asInt(0)
            ));
        }
    }

    private enum ConnectionState {
        DISCONNECTING,
        RECONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            YmpdClient.this.webSocket = socket;
            state = ConnectionState.CONNECTED;
            logger.info("Connected to {}", settings.serverUri());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            state = ConnectionState.DISCONNECTED;
            logger.info("Disconnected from {}", settings.serverUri());
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Received message: {}", message);

            try {
                JsonNode root = mapper.readTree(message);

                if (root != null && root.isObject() && root.has("type") && root.has("data")) {
                    onData(root.get("type").asText(""), root.get("data"));
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
