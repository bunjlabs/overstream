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

package com.overstreamapp.streamlabs.support;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.streamlabs.StreamlabsClient;
import com.overstreamapp.streamlabs.StreamlabsConnectionState;
import com.overstreamapp.streamlabs.StreamlabsSettings;
import com.overstreamapp.streamlabs.events.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class DefaultStreamlabsClient implements StreamlabsClient {
    private final Logger logger;
    private final StreamlabsSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    private final Event<TwitchSub> twitchSubEvent;
    private final Event<TwitchResub> twitchResubEvent;
    private final Event<TwitchFollow> twitchFollowEvent;
    private final Event<TwitchHost> twitchHostEvent;
    private final Event<TwitchBits> twitchBitsEvent;
    private final Event<TwitchRaid> twitchRaidEvent;

    private volatile StreamlabsConnectionState state = StreamlabsConnectionState.DISCONNECTED;
    private WebSocket webSocket;
    private ScheduledFuture<?> pingScheduledFuture;

    @Inject
    public DefaultStreamlabsClient(Logger logger, StreamlabsSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, EventKeeper eventKeeper) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.objectMapper = new ObjectMapper();

        this.twitchSubEvent = eventKeeper.eventBuilder(TwitchSub.class).build();
        this.twitchResubEvent = eventKeeper.eventBuilder(TwitchResub.class).build();
        this.twitchFollowEvent = eventKeeper.eventBuilder(TwitchFollow.class).build();
        this.twitchHostEvent = eventKeeper.eventBuilder(TwitchHost.class).build();
        this.twitchBitsEvent = eventKeeper.eventBuilder(TwitchBits.class).build();
        this.twitchRaidEvent = eventKeeper.eventBuilder(TwitchRaid.class).build();
    }

    private static String jsonNodeGetOrDefault(JsonNode node, String name, String defaultValue) {
        return node.has(name) ? node.get(name).asText(defaultValue) : defaultValue;
    }

    private static int jsonNodeGetOrDefault(JsonNode node, String name, int defaultValue) {
        return node.has(name) ? node.get(name).asInt(defaultValue) : defaultValue;
    }

    @Override
    public void connect() {
        if (state == StreamlabsConnectionState.DISCONNECTED || state == StreamlabsConnectionState.RECONNECTING) {
            logger.info("Connecting to Streamlabs Socket API {} ...", settings.serverUri());

            this.state = StreamlabsConnectionState.CONNECTING;
            var uri = URI.create(String.format(settings.serverUri(), settings.socketToken()));
            this.webSocketClient.connect(uri, this.webSocketHandler, settings.connectTimeout());
        }
    }

    @Override
    public void disconnect() {
        if (state != StreamlabsConnectionState.DISCONNECTED) {
            state = StreamlabsConnectionState.DISCONNECTING;
            if (this.pingScheduledFuture != null) {
                this.pingScheduledFuture.cancel(false);
                this.pingScheduledFuture.awaitUninterruptibly(1000);
                this.pingScheduledFuture = null;
            }

            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            state = StreamlabsConnectionState.DISCONNECTED;
        }
    }

    @Override
    public void reconnect() {
        if (state != StreamlabsConnectionState.RECONNECTING) {
            state = StreamlabsConnectionState.RECONNECTING;
            if (this.pingScheduledFuture != null) {
                this.pingScheduledFuture.cancel(false);
                this.pingScheduledFuture.awaitUninterruptibly(1000);
            }

            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, settings.reconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public StreamlabsConnectionState getConnectionState() {
        return state;
    }

    private void sendPing() {
        webSocket.send("2");
        logger.trace("-> PING");
    }

    private void onEvent(JsonNode event) {
        logger.debug("Event {}", event);

        if (!"twitch_account".equals(event.get("for").asText())) {
            return;
        }

        String eventType = event.get("type").asText("");
        event.get("message").forEach(m -> {
            switch (eventType) {
                case "subscription":
                    var subscription = new TwitchSub();
                    subscription.name = jsonNodeGetOrDefault(m, "name", "");
                    subscription.months = jsonNodeGetOrDefault(m, "months", 0);
                    subscription.message = jsonNodeGetOrDefault(m, "message", "");
                    subscription.subPlan = jsonNodeGetOrDefault(m, "sub_plan", "");
                    subscription.subPlanName = jsonNodeGetOrDefault(m, "sub_plan_name", "");
                    subscription.subType = jsonNodeGetOrDefault(m, "sub_type", "");
                    twitchSubEvent.fire(subscription);
                    break;
                case "resub":
                    var resub = new TwitchResub();
                    resub.name = jsonNodeGetOrDefault(m, "name", "");
                    resub.months = jsonNodeGetOrDefault(m, "months", 0);
                    resub.streak_months = jsonNodeGetOrDefault(m, "streak_months", 0);
                    resub.message = jsonNodeGetOrDefault(m, "message", "");
                    resub.subPlan = jsonNodeGetOrDefault(m, "sub_plan", "");
                    resub.subPlanName = jsonNodeGetOrDefault(m, "sub_plan_name", "");
                    resub.subType = jsonNodeGetOrDefault(m, "sub_type", "");
                    resub.amount = jsonNodeGetOrDefault(m, "amount", 0);
                    twitchResubEvent.fire(resub);
                    break;
                case "follow":
                    var follow = new TwitchFollow();
                    follow.name = jsonNodeGetOrDefault(m, "name", "");
                    follow.createdAt = jsonNodeGetOrDefault(m, "created_at", "");
                    twitchFollowEvent.fire(follow);
                    break;
                case "host":
                    var host = new TwitchHost();
                    host.name = jsonNodeGetOrDefault(m, "name", "");
                    host.viewers = jsonNodeGetOrDefault(m, "viewers", 0);
                    host.type = jsonNodeGetOrDefault(m, "type", "");
                    twitchHostEvent.fire(host);
                    break;
                case "bits":
                    var bits = new TwitchBits();
                    bits.name = jsonNodeGetOrDefault(m, "name", "");
                    bits.amount = jsonNodeGetOrDefault(m, "amount", 0);
                    bits.message = jsonNodeGetOrDefault(m, "message", "");
                    bits.currency = jsonNodeGetOrDefault(m, "currency", "");
                    twitchBitsEvent.fire(bits);
                    break;
                case "raid":
                    var raid = new TwitchRaid();
                    raid.name = jsonNodeGetOrDefault(m, "name", "");
                    raid.raiders = jsonNodeGetOrDefault(m, "raiders", 0);
                    twitchRaidEvent.fire(raid);
                    break;
                default:
                    logger.trace("Unsupported event type {}", eventType);
                    break;
            }
        });
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            DefaultStreamlabsClient.this.webSocket = socket;
            DefaultStreamlabsClient.this.state = StreamlabsConnectionState.CONNECTING;
            logger.info("Connected established");
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != StreamlabsConnectionState.DISCONNECTING && state != StreamlabsConnectionState.RECONNECTING) {
                logger.info("Connection lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = StreamlabsConnectionState.DISCONNECTED;
                logger.info("Disconnected: {} {}", code, reason);
            }
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Message received {}", message);

            try {
                if (message.startsWith("0") && state == StreamlabsConnectionState.CONNECTING) {
                    JsonNode obj = objectMapper.readTree(message.substring(1));

                    int pingInterval = obj.get("pingInterval").asInt(25000);

                    DefaultStreamlabsClient.this.pingScheduledFuture
                            = DefaultStreamlabsClient.this.loopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                            DefaultStreamlabsClient.this::sendPing, pingInterval, pingInterval, TimeUnit.MILLISECONDS);

                    DefaultStreamlabsClient.this.state = StreamlabsConnectionState.CONNECTED;

                    logger.debug("Handshake successful");
                } else if (message.startsWith("3")) {
                    logger.trace("<- PONG");
                } else if (message.startsWith("42")) {
                    JsonNode obj = objectMapper.readTree(message.substring(2));

                    if (obj != null && obj.get(0) != null && "event".equals(obj.get(0).asText(""))) {
                        JsonNode event = obj.get(1);

                        if (event != null) {
                            DefaultStreamlabsClient.this.onEvent(obj.get(1));
                        }
                    }
                }
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

}
