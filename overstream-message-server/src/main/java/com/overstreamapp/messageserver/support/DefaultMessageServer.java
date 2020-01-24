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

package com.overstreamapp.messageserver.support;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.AppInfo;
import com.overstreamapp.common.TypeConverter;
import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.event.EventSubscription;
import com.overstreamapp.messageserver.MessageServer;
import com.overstreamapp.messageserver.MessageServerSettings;
import com.overstreamapp.messageserver.domain.Message;
import com.overstreamapp.messageserver.domain.MessagePayloadAppInfo;
import com.overstreamapp.messageserver.domain.MessagePayloadEvent;
import com.overstreamapp.messageserver.domain.MessagePayloadState;
import com.overstreamapp.messageserver.event.MessageServerConnectionEvent;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.store.StoreSubscription;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class DefaultMessageServer implements MessageServer {

    private final Logger log;
    private final AppInfo appInfo;
    private final WebSocketServer webSocketServer;
    private final EventKeeper eventKeeper;
    private final StoreKeeper storeKeeper;
    private final TypeConverter conv;
    private final MessageServerSettings settings;

    private final Event<MessageServerConnectionEvent> connectionEvent;

    @Inject
    public DefaultMessageServer(Logger log, AppInfo appInfo, WebSocketServer webSocketServer, EventKeeper eventKeeper, StoreKeeper storeKeeper, TypeConverter conv, MessageServerSettings settings) {
        this.log = log;
        this.appInfo = appInfo;
        this.webSocketServer = webSocketServer;
        this.eventKeeper = eventKeeper;
        this.storeKeeper = storeKeeper;
        this.conv = conv;
        this.settings = settings;

        this.connectionEvent = eventKeeper.eventBuilder(MessageServerConnectionEvent.class).build();
    }

    @Override
    public void start() {
        SocketAddress bindAddress = new InetSocketAddress(settings.bindHost(), settings.bindPort());
        webSocketServer.start(bindAddress, Handler::new);

        log.info("Started on {}", bindAddress);
    }

    private class Handler implements WebSocketHandler {

        private WebSocket socket;
        private EventSubscription eventSubscription;
        private StoreSubscription storeSubscription;

        @Override
        public void onStart() {
            log.debug("New client connection");
        }

        @Override
        public void onOpen(WebSocket socket) {
            this.socket = socket;

            connectionEvent.fire(new MessageServerConnectionEvent(socket.getRemoteSocketAddress().getHostString()));

            this.eventSubscription = eventKeeper.subscribe(event -> {
                send(new Message("Event", new MessagePayloadEvent(event.getClass().getSimpleName(), event)));
            });

            this.storeSubscription = storeKeeper.subscribe(state -> {
                send(new Message("State", new MessagePayloadState(state.getClass().getSimpleName(), state)));
            });

            send(new Message("Ready", new MessagePayloadAppInfo(appInfo.name(), appInfo.version())));

            log.info("Client connected {}", socket.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            if (eventSubscription != null) {
                eventSubscription.unsubscribe();
                eventSubscription = null;
            }

            if (storeSubscription != null) {
                storeSubscription.unsubscribe();
                storeSubscription = null;
            }

            log.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            log.debug("Message received {} {}", socket.getRemoteSocketAddress(), message);

            if (message.equals("Ping")) {
                send(new Message("Pong"));
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable ex) {
            log.error("Error", ex);
        }

        private void send(Message message) {
            if (socket != null) {
                socket.send(conv.objectToJson(message));
            }
        }
    }

}
