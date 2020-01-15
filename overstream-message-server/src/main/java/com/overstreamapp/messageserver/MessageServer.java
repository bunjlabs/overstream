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

package com.overstreamapp.messageserver;

import com.bunjlabs.fuga.inject.Inject;
import com.google.gson.Gson;
import com.overstreamapp.AppInfo;
import com.overstreamapp.messageserver.event.NewConnectionEvent;
import com.overstreamapp.messageserver.messages.*;
import com.overstreamapp.keeper.*;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class MessageServer {

    private final Set<WebSocket> clients = new HashSet<>();
    private final Logger log;
    private final MessageServerSettings settings;
    private AppInfo appInfo;
    private final WebSocketServer webSocketServer;
    private final Keeper keeper;
    private final Gson gson;
    private final WebSocketHandler webSocketHandler;
    private final Event<NewConnectionEvent> connectionEvent;

    @Inject
    public MessageServer(Logger log, AppInfo appInfo, WebSocketServer webSocketServer, Keeper keeper, MessageServerSettings settings) {
        this.log = log;
        this.appInfo = appInfo;
        this.webSocketServer = webSocketServer;
        this.keeper = keeper;
        this.settings = settings;

        this.gson = new Gson();
        this.webSocketHandler = new Handler();

        this.connectionEvent = keeper.eventBuilder(NewConnectionEvent.class).build();
    }

    public void start() {
        SocketAddress bindAddress = new InetSocketAddress(settings.bindHost(), settings.bindPort());
        webSocketServer.start(bindAddress, webSocketHandler);

        keeper.subscribeAll(this::onStateChange);
        keeper.subscribeAll(this::onEvent);

        log.info("Started on {}", bindAddress);
    }


    private void onStateChange(StateInfo info, StateObject stateObject) {
        clients.forEach(c -> c.send(compileStateObject(info, stateObject)));
    }

    private void onEvent(EventInfo info, EventObject eventObject) {
        clients.forEach(c -> c.send(compileEventObject(info, eventObject)));
    }

    private String compileStateObject(StateInfo info, StateObject stateObject) {
        return gson.toJson(new StateMessage(info, stateObject));
    }

    private String compileStateBurstObject(StateInfo info, StateObject stateObject) {
        return gson.toJson(new StateBurstMessage(info, stateObject));
    }

    private String compileStateInfo(StateInfo info) {
        return gson.toJson(new StateInfoMessage(info));
    }

    private String compileEventInfo(EventInfo info) {
        return gson.toJson(new EventInfoMessage(info));
    }

    private String compileEventObject(EventInfo info, EventObject eventObject) {
        return gson.toJson(new EventMessage(info, eventObject));
    }

    private String compileReady() {
        return gson.toJson(new ReadyMessage(appInfo.name() + "-" + appInfo.version()));
    }

    private String compilePong() {
        return gson.toJson(new PongMessage());
    }

    private class Handler implements WebSocketHandler {

        @Override
        public void onOpen(WebSocket socket) {
            clients.add(socket);
            keeper.getAllStateInfo().forEach(info -> socket.send(compileStateInfo(info)));
            keeper.getAllEventInfo().forEach(info -> socket.send(compileEventInfo(info)));
            keeper.burstState((info, stateObject) -> socket.send(compileStateBurstObject(info, stateObject)));
            socket.send(compileReady());

            connectionEvent.fire(new NewConnectionEvent(socket.getRemoteSocketAddress().getHostString()));

            log.info("Client connected {}", socket.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket socket, int code, String reason, boolean remote) {
            clients.remove(socket);
            log.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
        }

        @Override
        public void onMessage(WebSocket socket, String message) {
            log.debug("Message received {} {}", socket.getRemoteSocketAddress(), message);

            if (message.equals("PING")) {
                socket.send(compilePong());
            }
        }

        @Override
        public void onMessage(WebSocket socket, ByteBuffer bytes) {

        }

        @Override
        public void onError(WebSocket socket, Throwable ex) {
            log.error("Error", ex);
        }

        @Override
        public void onStart() {
            log.info("New client connection");
        }
    }

}
