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

package com.overstreamapp.commandserver.support;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.commandserver.CommandServer;
import com.overstreamapp.commandserver.CommandServerSettings;
import com.overstreamapp.commandserver.event.CommandServerConnectionEvent;
import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class DefaultCommandServer implements CommandServer {
    private final Logger logger;
    private final WebSocketServer webSocketServer;
    private final CommandServerSettings settings;
    private final CommandRegistry commandRegistry;
    private final Event<CommandServerConnectionEvent> connectionEvent;

    @Inject
    public DefaultCommandServer(Logger logger, WebSocketServer webSocketServer, CommandServerSettings settings, CommandRegistry commandRegistry, EventKeeper eventKeeper) {
        this.logger = logger;
        this.webSocketServer = webSocketServer;
        this.settings = settings;
        this.commandRegistry = commandRegistry;

        this.connectionEvent = eventKeeper.eventBuilder(CommandServerConnectionEvent.class).build();
    }

    @Override
    public void start() {
        SocketAddress bindAddress = new InetSocketAddress(settings.bindHost(), settings.bindPort());
        webSocketServer.start(bindAddress, Handler::new);

        logger.info("Started on {}", bindAddress);
    }

    private class Handler implements WebSocketHandler {
        private WebSocket socket;

        @Override
        public void onOpen(WebSocket socket) {
            this.socket = socket;

            connectionEvent.fire(new CommandServerConnectionEvent(socket.getRemoteSocketAddress().getHostString()));

            logger.info("Client connected {}", socket.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            logger.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.debug("Message received {} {}", socket.getRemoteSocketAddress(), message);

            var result = commandRegistry.executeFlat(message);

            if (result != null && !result.isBlank()) {
                socket.send(result);
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
            logger.debug("New client connection");
        }
    }
}
