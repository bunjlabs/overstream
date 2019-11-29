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

package com.overstreamapp.streamlabs;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.statemanager.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import io.netty.util.concurrent.ScheduledFuture;
import org.bson.Document;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class StreamlabsClient {
    private final Logger logger;
    private final StreamlabsSettings settings;
    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    private final State twitchSubState;
    private final State twitchResubState;
    private final State twitchFollowState;
    private final State twitchHostState;
    private final State twitchBitsState;
    private final State twitchRaidState;

    private URI uri;
    private WebSocket webSocket;
    private ScheduledFuture<?> pingScheduledFuture;
    private ConnectionState state = ConnectionState.DISCONNECTED;

    @Inject
    public StreamlabsClient(Logger logger, StreamlabsSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, StateManager stateManager) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;
        this.webSocketHandler = new Handler();
        this.objectMapper = new ObjectMapper();

        this.twitchSubState = stateManager.createState(new StateOptions("TwitchSub", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchSubObject::new));
        this.twitchResubState = stateManager.createState(new StateOptions("TwitchResub", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchResubObject::new));
        this.twitchFollowState = stateManager.createState(new StateOptions("TwitchFollow", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchFollowObject::new));
        this.twitchHostState = stateManager.createState(new StateOptions("TwitchHost", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchHostObject::new));
        this.twitchBitsState = stateManager.createState(new StateOptions("TwitchBits", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchBitsObject::new));
        this.twitchRaidState = stateManager.createState(new StateOptions("TwitchRaid", StateType.EVENT, HistoryStrategy.LIMIT, 10, TwitchRaidObject::new));
    }

    public void connect() {
        if (state == ConnectionState.DISCONNECTED || state == ConnectionState.RECONNECTING) {
            this.state = ConnectionState.CONNECTING;

            this.uri = URI.create(String.format(settings.serverUri(), settings.socketToken()));
            this.webSocketClient.connect(this.uri, this.webSocketHandler);
        }
    }

    public void disconnect() {
        if (state != ConnectionState.DISCONNECTED) {
            state = ConnectionState.DISCONNECTED;
            if (this.pingScheduledFuture != null) {
                this.pingScheduledFuture.cancel(false);
                this.pingScheduledFuture.awaitUninterruptibly(1000);
            }
            this.webSocket.close();
            this.webSocket = null;
        }
    }

    public void reconnect() {
        state = ConnectionState.RECONNECTING;
        disconnect();
        connect();
    }

    private void sendPing() {
        webSocket.send("2");
        logger.trace("-> PING");
    }

    private enum ConnectionState {
        DISCONNECTING,
        RECONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
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
                    var subscription = new TwitchSubObject();
                    subscription.name = jsonNodeGetOrDefault(m, "name", "");
                    subscription.months = jsonNodeGetOrDefault(m, "months", 0);
                    subscription.message = jsonNodeGetOrDefault(m, "message", "");
                    subscription.sub_plan = jsonNodeGetOrDefault(m, "sub_plan", "");
                    subscription.sub_plan_name = jsonNodeGetOrDefault(m, "sub_plan_name", "");
                    subscription.sub_type = jsonNodeGetOrDefault(m, "sub_type", "");
                    twitchSubState.push(subscription);
                    break;
                case "resub":
                    var resub = new TwitchResubObject();
                    resub.name = jsonNodeGetOrDefault(m, "name", "");
                    resub.months = jsonNodeGetOrDefault(m, "months", 0);
                    resub.streak_months = jsonNodeGetOrDefault(m, "streak_months", 0);
                    resub.message = jsonNodeGetOrDefault(m, "message", "");
                    resub.sub_plan = jsonNodeGetOrDefault(m, "sub_plan", "");
                    resub.sub_plan_name = jsonNodeGetOrDefault(m, "sub_plan_name", "");
                    resub.sub_type = jsonNodeGetOrDefault(m, "sub_type", "");
                    resub.amount = jsonNodeGetOrDefault(m, "amount", 0);
                    twitchSubState.push(resub);
                    break;
                case "follow":
                    var follow = new TwitchFollowObject();
                    follow.name = jsonNodeGetOrDefault(m, "name", "");
                    follow.created_at = jsonNodeGetOrDefault(m, "created_at", "");
                    twitchFollowState.push(follow);
                    break;
                case "host":
                    var host = new TwitchHostObject();
                    host.name = jsonNodeGetOrDefault(m, "name", "");
                    host.viewers = jsonNodeGetOrDefault(m, "viewers", 0);
                    host.type = jsonNodeGetOrDefault(m, "type", "");
                    twitchHostState.push(host);
                    break;
                case "bits":
                    var bits = new TwitchBitsObject();
                    bits.name = jsonNodeGetOrDefault(m, "name", "");
                    bits.amount = jsonNodeGetOrDefault(m, "amount", 0);
                    bits.message = jsonNodeGetOrDefault(m, "message", "");
                    bits.currency = jsonNodeGetOrDefault(m, "currency", "");
                    twitchBitsState.push(bits);
                    break;
                case "raid":
                    var raid = new TwitchRaidObject();
                    raid.name = jsonNodeGetOrDefault(m, "name", "");
                    raid.raiders = jsonNodeGetOrDefault(m, "raiders", 0);
                    twitchRaidState.push(raid);
                    break;
                default:
                    logger.trace("Unsupported event type {}", eventType);
                    break;
            }
        });
    }

    private static String jsonNodeGetOrDefault(JsonNode node, String name, String defaultValue) {
        return node.has(name) ? node.get(name).asText(defaultValue) : defaultValue;
    }

    private static int jsonNodeGetOrDefault(JsonNode node, String name, int defaultValue) {
        return node.has(name) ? node.get(name).asInt(defaultValue) : defaultValue;
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            logger.debug("Connecting to Streamlabs Socket API ...");
            StreamlabsClient.this.webSocket = socket;
            StreamlabsClient.this.state = ConnectionState.CONNECTING;
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != ConnectionState.DISCONNECTING) {
                logger.info("Connection to Streamlabs Socket API lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = ConnectionState.DISCONNECTED;
                logger.info("Disconnected from Streamlabs Socket API: {} {}", code, reason);
            }
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.trace("Message received {}", message);

            try {
                if (message.startsWith("0") && state == ConnectionState.CONNECTING) {
                    JsonNode obj = objectMapper.readTree(message.substring(1));

                    int pingInterval = obj.get("pingInterval").asInt(25000);

                    StreamlabsClient.this.pingScheduledFuture
                            = StreamlabsClient.this.loopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                            StreamlabsClient.this::sendPing, pingInterval, pingInterval, TimeUnit.MILLISECONDS);

                    StreamlabsClient.this.state = ConnectionState.CONNECTED;

                    logger.info("Connected to Streamlabs Socket API");
                } else if (message.startsWith("3")) {
                    logger.trace("<- PONG");
                } else if (message.startsWith("42")) {
                    JsonNode obj = objectMapper.readTree(message.substring(2));

                    if (obj != null && obj.get(0) != null && "event".equals(obj.get(0).asText(""))) {
                        JsonNode event = obj.get(1);

                        if (event != null) {
                            StreamlabsClient.this.onEvent(obj.get(1));
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
            logger.info("Web socket client started");
        }
    }

    private static class TwitchSubObject implements StateObject {
        private String name;
        private int months;
        private String message;
        private String sub_plan;
        private String sub_plan_name;
        private String sub_type;

        TwitchSubObject() {
        }

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("months", months);
            document.put("message", message);
            document.put("sub_plan", sub_plan);
            document.put("sub_plan_name", sub_plan_name);
            document.put("sub_type", sub_type);
        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            months = document.getInteger("months", 0);
            message = document.getString("message");
            sub_plan = document.getString("sub_plan");
            sub_plan_name = document.getString("sub_plan_name");
            sub_type = document.getString("sub_type");
        }
    }

    private static class TwitchResubObject implements StateObject {
        private String name;
        private int months;
        private int streak_months;
        private String message;
        private String sub_plan;
        private String sub_plan_name;
        private String sub_type;
        private int amount;

        TwitchResubObject() {
        }

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("months", months);
            document.put("streak_months", streak_months);
            document.put("message", message);
            document.put("sub_plan", sub_plan);
            document.put("sub_plan_name", sub_plan_name);
            document.put("sub_type", sub_type);
            document.put("amount", amount);
        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            months = document.getInteger("months", 0);
            streak_months = document.getInteger("streak_months", 0);
            message = document.getString("message");
            sub_plan = document.getString("sub_plan");
            sub_plan_name = document.getString("sub_plan_name");
            sub_type = document.getString("sub_type");
            amount = document.getInteger("amount", 0);
        }
    }

    private static class TwitchFollowObject implements StateObject {
        private String name;
        private String created_at;

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("created_at", created_at);

        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            created_at = document.getString("created_at");
        }
    }

    private static class TwitchHostObject implements StateObject {
        private String name;
        private int viewers;
        private String type;

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("viewers", viewers);
            document.put("type", type);

        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            viewers = document.getInteger("viewers", 0);
            type = document.getString("type");
        }
    }

    private static class TwitchBitsObject implements StateObject {
        private String name;
        private int amount;
        private String message;
        public String currency;

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("amount", amount);
            document.put("message", message);
            document.put("currency", currency);
        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            amount = document.getInteger("amount", 0);
            message = document.getString("message");
            currency = document.getString("currency");
        }
    }

    private static class TwitchRaidObject implements StateObject {
        private String name;
        private int raiders;

        @Override
        public void save(Document document) {
            document.put("name", name);
            document.put("raiders", raiders);
        }

        @Override
        public void load(Document document) {
            name = document.getString("name");
            raiders = document.getInteger("raiders", 0);
        }
    }
}
