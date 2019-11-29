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
import com.google.gson.*;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.obs.event.ObsEvent;
import com.overstreamapp.obs.event.ObsHeartbeatEvent;
import com.overstreamapp.obs.event.ObsStreamStatusEvent;
import com.overstreamapp.obs.request.ObsRequest;
import com.overstreamapp.obs.request.ObsSetHeartbeatRequest;
import com.overstreamapp.statemanager.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class ObsClient {
    private final Logger logger;
    private final ObsSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final WebSocketHandler webSocketHandler;
    private final Gson gson;
    private final State heartbeatState;
    private final State streamStatusState;
    private WebSocket webSocket;
    private ConnectionState state = ConnectionState.DISCONNECTED;

    @Inject
    public ObsClient(Logger logger, ObsSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, StateManager stateManager) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.gson = new GsonBuilder().registerTypeAdapter(ObsEvent.class, new ObsEventDeserializer()).create();

        this.heartbeatState = stateManager.createState(new StateOptions("ObsHearbeat", StateType.STATE, ObsHeartbeatState::new));
        this.streamStatusState = stateManager.createState(new StateOptions("ObsStreamStatus", StateType.STATE, ObsStreamStatusState::new));
    }

    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.RECONNECTING) {
            this.state = ConnectionState.CONNECTING;

            this.webSocketClient.connect(URI.create(settings.serverUri()), this.webSocketHandler);
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

    private void send(ObsRequest request) {
        if (state == ConnectionState.CONNECTED) {
            String json = gson.toJson(request);
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

            send(new ObsSetHeartbeatRequest(true));
            logger.info("Connected to OBS");
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != ConnectionState.DISCONNECTING) {
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
                var event = gson.fromJson(message, ObsEvent.class);

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
        if (event instanceof ObsHeartbeatEvent) {
            var heartbeatEvent = (ObsHeartbeatEvent) event;
            var heartbeat = new ObsHeartbeatState();
            heartbeat.heartbeat = heartbeatEvent;

            heartbeatState.push(heartbeat);
        } else if(event instanceof ObsStreamStatusEvent) {
            var streamStatusEvent = (ObsStreamStatusEvent) event;
            var streamStatus = new ObsStreamStatusState();
            streamStatus.streamStatus = streamStatusEvent;

            streamStatusState.push(streamStatus);
        }
    }

    private static class ObsEventDeserializer implements JsonDeserializer<ObsEvent> {
        @Override
        public ObsEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            JsonElement type = jsonObject.get("update-type");
            if (type != null) {
                switch (type.getAsString()) {
                    case "Heartbeat":
                        return context.deserialize(jsonObject, ObsHeartbeatEvent.class);
                    case "StreamStatus":
                        return context.deserialize(jsonObject, ObsStreamStatusEvent.class);
                }
            }

            return null;
        }
    }

    private static class ObsHeartbeatState extends EventObject {
        private ObsHeartbeatEvent heartbeat;
    }

    private static class ObsStreamStatusState extends EventObject {
        private ObsStreamStatusEvent streamStatus;
    }
}
