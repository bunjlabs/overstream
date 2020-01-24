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

package com.overstreamapp.twitchpubsub.support;

import com.bunjlabs.fuga.inject.Inject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.twitchpubsub.TwitchPubSub;
import com.overstreamapp.twitchpubsub.TwitchPubSubConnectionState;
import com.overstreamapp.twitchpubsub.TwitchPubSubSettings;
import com.overstreamapp.twitchpubsub.domain.PubSubRequest;
import com.overstreamapp.twitchpubsub.domain.PubSubResponse;
import com.overstreamapp.twitchpubsub.domain.PubSubTopic;
import com.overstreamapp.twitchpubsub.domain.PubSubType;
import com.overstreamapp.twitchpubsub.events.*;
import com.overstreamapp.twitchpubsub.messages.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DefaultTwitchPubSub implements TwitchPubSub {
    private final Logger logger;
    private final TwitchPubSubSettings settings;

    private final WebSocketClient webSocketClient;
    private final EventLoopGroupManager loopGroupManager;
    private final WebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    private final List<PubSubRequest> subscribedTopics = new ArrayList<>();
    private final Event<TwitchBitsEvent> bitsEvent;
    private final Event<TwitchBitsBadgeEvent> bitsBadgeEvent;
    private final Event<TwitchChannelPointsEvent> channelPointsEvent;
    private final Event<TwitchSubscriptionEvent> subscriptionEvent;
    private final Event<TwitchCommerceEvent> commerceEvent;
    private volatile WebSocket webSocket;
    private volatile ScheduledFuture<?> pingScheduledFuture;
    private volatile ScheduledFuture<?> pongScheduledFuture;
    private AtomicBoolean pongReceived = new AtomicBoolean(false);
    private volatile TwitchPubSubConnectionState state = TwitchPubSubConnectionState.DISCONNECTED;

    @Inject
    public DefaultTwitchPubSub(Logger logger, TwitchPubSubSettings settings, WebSocketClient webSocketClient, EventLoopGroupManager loopGroupManager, EventKeeper eventKeeper) {
        this.logger = logger;
        this.settings = settings;
        this.webSocketClient = webSocketClient;
        this.loopGroupManager = loopGroupManager;

        this.webSocketHandler = new Handler();
        this.objectMapper = new ObjectMapper();

        this.bitsEvent = eventKeeper.eventBuilder(TwitchBitsEvent.class).build();
        this.bitsBadgeEvent = eventKeeper.eventBuilder(TwitchBitsBadgeEvent.class).build();
        this.channelPointsEvent = eventKeeper.eventBuilder(TwitchChannelPointsEvent.class).build();
        this.subscriptionEvent = eventKeeper.eventBuilder(TwitchSubscriptionEvent.class).build();
        this.commerceEvent = eventKeeper.eventBuilder(TwitchCommerceEvent.class).build();

        Arrays.stream(PubSubTopic.values()).forEach(this::listenTopic);
    }

    @Override
    public void connect() {
        if (state == TwitchPubSubConnectionState.DISCONNECTED || state == TwitchPubSubConnectionState.RECONNECTING) {
            logger.info("Connecting to Twitch PubSub {} ...", settings.serverUri());

            this.state = TwitchPubSubConnectionState.CONNECTING;

            var uri = URI.create(settings.serverUri());
            this.webSocketClient.connect(uri, this.webSocketHandler, settings.connectTimeout());
        }
    }

    @Override
    public void disconnect() {
        if (state != TwitchPubSubConnectionState.DISCONNECTED) {
            state = TwitchPubSubConnectionState.DISCONNECTING;
            if (this.pingScheduledFuture != null) {
                this.pingScheduledFuture.cancel(false);
                this.pingScheduledFuture.awaitUninterruptibly(1000);
            }

            if (this.pongScheduledFuture != null) {
                this.pongScheduledFuture.cancel(false);
                this.pongScheduledFuture.awaitUninterruptibly(1000);
                this.pongScheduledFuture = null;
            }

            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            state = TwitchPubSubConnectionState.DISCONNECTED;
        }
    }

    @Override
    public void reconnect() {
        if (state != TwitchPubSubConnectionState.RECONNECTING) {
            state = TwitchPubSubConnectionState.RECONNECTING;
            if (this.pingScheduledFuture != null) {
                this.pingScheduledFuture.cancel(false);
                this.pingScheduledFuture.awaitUninterruptibly(1000);
                this.pingScheduledFuture = null;
            }

            if (this.pongScheduledFuture != null) {
                this.pongScheduledFuture.cancel(false);
                this.pongScheduledFuture.awaitUninterruptibly(1000);
                this.pongScheduledFuture = null;
            }

            if (this.webSocket != null) {
                this.webSocket.close();
                this.webSocket = null;
            }
            loopGroupManager.getWorkerEventLoopGroup().schedule(this::connect, settings.reconnectDelay(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public TwitchPubSubConnectionState getConnectionState() {
        return state;
    }

    public void listenTopic(PubSubTopic topic) {
        var message = new PubSubRequest();
        var topics = settings.channels().stream().map(topic::getTopic).collect(Collectors.toList());

        message.setType(PubSubType.LISTEN);
        message.setNonce(topics.toString());
        message.getData().put("auth_token", settings.authToken());
        message.getData().put("topics", topics);

        logger.debug("Listen for topics: {}", topics);

        send(message);
        subscribedTopics.add(message);
    }

    private void ping() {
        if (pongReceived.get()) {
            return;
        }

        var message = new PubSubRequest();
        message.setType(PubSubType.PING);
        send(message);

        pongScheduledFuture = loopGroupManager.getWorkerEventLoopGroup().schedule(() -> {
            if (state != TwitchPubSubConnectionState.CONNECTED) {
                return;
            }

            if (!pongReceived.getAndSet(false)) {
                logger.debug("No pong received. Reconnecting...");
                reconnect();
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void send(PubSubRequest message) {
        if (webSocket != null) {
            try {
                var rawMessage = objectMapper.writeValueAsString(message);

                logger.trace("Send: {}", rawMessage);

                webSocket.send(rawMessage);
            } catch (JsonProcessingException e) {
                logger.error("Error sending message", e);
            }
        }
    }


    private void onMessage(PubSubResponse message) throws IOException {
        logger.debug("Message: {}", message);

        var payload = message.getData();
        var data = payload.getMessage();
        var topic = payload.getTopic();

        if (PubSubTopic.BITS.isSame(topic)) {
            var bitsMessage = objectMapper.readValue(data, BitsMessage.class);
            bitsEvent.fire(new TwitchBitsEvent(bitsMessage));
        } else if (PubSubTopic.BITS_BADGE.isSame(topic)) {
            var bitsBadgeMessage = objectMapper.readValue(data, BitsBadgeMessage.class);
            bitsBadgeEvent.fire(new TwitchBitsBadgeEvent(bitsBadgeMessage));
        } else if (PubSubTopic.CHANNEL_POINTS.isSame(topic)) {
            var channelPointsMessage = objectMapper.readValue(data, ChannelPointsMessage.class);
            channelPointsEvent.fire(new TwitchChannelPointsEvent(channelPointsMessage));
        } else if (PubSubTopic.CHANNEL_SUBSCRIPTIONS.isSame(topic)) {
            var subscription = objectMapper.readValue(data, TwitchMessage.class);
            subscriptionEvent.fire(new TwitchSubscriptionEvent(subscription));
        } else if (PubSubTopic.COMMERCE.isSame(topic)) {
            var commerce = objectMapper.readValue(data, CommerceMessage.class);
            commerceEvent.fire(new TwitchCommerceEvent(commerce));
        }
    }

    private class Handler implements WebSocketHandler {
        @Override
        public void onOpen(WebSocket socket) {
            DefaultTwitchPubSub.this.webSocket = socket;
            DefaultTwitchPubSub.this.state = TwitchPubSubConnectionState.CONNECTED;

            DefaultTwitchPubSub.this.pingScheduledFuture
                    = DefaultTwitchPubSub.this.loopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                    DefaultTwitchPubSub.this::ping, 0, 1, TimeUnit.MINUTES);

            subscribedTopics.forEach(DefaultTwitchPubSub.this::send);

            logger.info("Connection established");
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (state != TwitchPubSubConnectionState.DISCONNECTING && state != TwitchPubSubConnectionState.RECONNECTING) {
                logger.info("Connection lost: {} {}. Retrying ...", code, reason);

                reconnect();
            } else {
                state = TwitchPubSubConnectionState.DISCONNECTED;
                logger.info("Disconnected: {} {}", code, reason);
            }
        }

        @Override
        public void onMessage(WebSocket socket, String rawMessage) {
            logger.trace("Received: {}", rawMessage);

            try {
                var message = objectMapper.readValue(rawMessage, PubSubResponse.class);

                switch (message.getType()) {
                    case RECONNECT:
                        reconnect();
                        break;
                    case PONG:
                        pongReceived.set(true);
                        break;
                    case MESSAGE:
                        DefaultTwitchPubSub.this.onMessage(message);
                        break;
                }
            } catch (IOException e) {
                logger.error("Error parsing message", e);
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable ex) {

        }

        @Override
        public void onStart() {

        }
    }

}
