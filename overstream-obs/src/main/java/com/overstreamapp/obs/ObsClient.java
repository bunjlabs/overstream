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

package com.overstreamapp.obs;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.common.TypeConverter;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.obs.obsevent.ObsEvent;
import com.overstreamapp.obs.obsevent.ObsHeartbeat;
import com.overstreamapp.obs.obsevent.ObsStreamStatus;
import com.overstreamapp.obs.request.ObsRequest;
import com.overstreamapp.obs.request.ObsSetHeartbeatRequest;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.store.ValueAction;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ObsClient {
    private final Logger logger;
    private final ObsSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final TypeConverter conv;
    private final WebSocketHandler webSocketHandler;
    private final Store<ObsHeartbeat> heartbeatStore;
    private final Store<ObsStreamStatus> streamStatusStore;
    private WebSocket webSocket;
    private ConnectionState state = ConnectionState.DISCONNECTED;

    @Inject
    public ObsClient(Logger logger, ObsSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, TypeConverter conv, StoreKeeper storeKeeper) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.conv = conv;
        this.webSocketHandler = new Handler();

        this.heartbeatStore = storeKeeper.storeBuilder(ObsHeartbeat.class).build();
        this.streamStatusStore = storeKeeper.storeBuilder(ObsStreamStatus.class).build();
    }

    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.RECONNECTING) {
            logger.info("Connecting to OBS {} ...", settings.serverUri());

            this.state = ConnectionState.CONNECTING;
            this.webSocketClient.connect(URI.create(settings.serverUri()), this.webSocketHandler, settings.connectTimeout());
        }
    }

    public void disconnect() {
        if (state != ConnectionState.DISCONNECTED) {
            state = ConnectionState.DISCONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
        }
    }

    public void reconnect() {
        if (state != ConnectionState.RECONNECTING) {
            state = ConnectionState.RECONNECTING;
            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, settings.reconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    private void send(ObsRequest request) {
        if (state == ConnectionState.CONNECTED) {
            String json = conv.objectToJson(request);
            logger.trace("Sending request: {}", json);
            webSocket.send(json);
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
            ObsClient.this.webSocket = socket;
            ObsClient.this.state = ConnectionState.CONNECTED;

            send(new ObsSetHeartbeatRequest(UUID.randomUUID().toString(), true));
            logger.info("Connected to OBS");
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != ConnectionState.DISCONNECTING && state != ConnectionState.RECONNECTING) {
                logger.info("Connection to OBS lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = ConnectionState.DISCONNECTED;
                logger.info("Disconnected from OBS: {} {}", code, reason);
            }
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Message received {}", message);

            try {
                var event = conv.jsonToObject(message, ObsEvent.class);

                onObsEvent(event);
            } catch (Exception e) {
                logger.error("Error parsing message", e);
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable cause) {
            logger.error("WebSocket error", cause);
        }

        @Override
        public void onStart() {
            logger.debug("Web socket client started");
        }
    }

    private void onObsEvent(ObsEvent event) {
        if (event instanceof ObsHeartbeat) {
            var heartbeatEvent = (ObsHeartbeat) event;
            heartbeatStore.dispatch(new ValueAction(heartbeatEvent));
        } else if (event instanceof ObsStreamStatus) {
            var streamStatusEvent = (ObsStreamStatus) event;
            streamStatusStore.dispatch(new ValueAction(streamStatusEvent));
        }
    }
}
