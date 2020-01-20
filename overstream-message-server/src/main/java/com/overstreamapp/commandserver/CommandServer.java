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

package com.overstreamapp.commandserver;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.AppInfo;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class CommandServer {
    private final Logger logger;
    private final WebSocketServer webSocketServer;
    private final CommandServerSettings settings;
    private final CommandRegistry commandRegistry;

    @Inject
    public CommandServer(Logger logger, WebSocketServer webSocketServer, CommandServerSettings settings, CommandRegistry commandRegistry) {
        this.logger = logger;
        this.webSocketServer = webSocketServer;
        this.settings = settings;
        this.commandRegistry = commandRegistry;
    }

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
            logger.info("Client connected {}", socket.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            logger.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            logger.debug("Message received {} {}", socket.getRemoteSocketAddress(), message);

            socket.send(commandRegistry.executeFlat(message));
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
