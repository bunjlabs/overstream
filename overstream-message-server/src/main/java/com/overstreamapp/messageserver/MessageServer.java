package com.overstreamapp.messageserver;

import com.bunjlabs.fuga.inject.Inject;
import com.google.gson.Gson;
import com.overstreamapp.messageserver.messages.StateUpdateMessage;
import com.overstreamapp.statemanager.StateInfo;
import com.overstreamapp.statemanager.StateManager;
import com.overstreamapp.statemanager.StateObject;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class MessageServer implements WebSocketHandler {

    private final Set<WebSocket> clients = new HashSet<>();
    private final Logger log;
    private final MessageServerSettings settings;
    private final WebSocketServer webSocketServer;
    private final StateManager stateManager;
    private final Gson gson;

    @Inject
    public MessageServer(Logger log, WebSocketServer webSocketServer, StateManager stateManager, MessageServerSettings settings) {
        this.log = log;
        this.webSocketServer = webSocketServer;
        this.stateManager = stateManager;
        this.settings = settings;

        this.gson = new Gson();
    }

    public void start() {
        SocketAddress bindAddress = new InetSocketAddress(settings.bindHost(), settings.bindPort());
        webSocketServer.start(bindAddress, this);

        stateManager.subscribeAll(this::onMessageBus);

        log.info("Started on {}", bindAddress);
    }

    private void onMessageBus(StateInfo info, StateObject stateObject) {
        clients.forEach(c -> c.send(compileStateObject(info, stateObject)));
    }

    private String compileStateObject(StateInfo info, StateObject stateObject) {
        return gson.toJson(new MessageObject(new StateUpdateMessage(info, stateObject)));
    }

    @Override
    public void onOpen(WebSocket socket) {
        clients.add(socket);
        log.info("Client connected {}", socket.getRemoteSocketAddress());

        stateManager.burst((info, stateObject) -> socket.send(compileStateObject(info, stateObject)));
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        clients.remove(socket);
        log.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        log.info("Message received {} {}", socket.getRemoteSocketAddress(), message);
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
