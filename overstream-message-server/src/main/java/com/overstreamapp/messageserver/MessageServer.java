package com.overstreamapp.messageserver;

import com.bunjlabs.fuga.inject.Inject;
import com.google.gson.Gson;
import com.overstreamapp.messagebus.Message;
import com.overstreamapp.messagebus.MessageBus;
import com.overstreamapp.messageserver.messages.RawWebSocketMessage;
import com.overstreamapp.websocket.WebSocket;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

public class MessageServer implements WebSocketHandler {

    private final Set<WebSocket> clients = new HashSet<>();
    private final Logger log;
    private final MessageBus messageBus;
    private final MessageServerSettings settings;
    private final WebSocketServer webSocketServer;
    private final Gson gson;

    @Inject
    public MessageServer(Logger log, WebSocketServer webSocketServer, MessageBus messageBus, MessageServerSettings settings) {
        this.log = log;
        this.webSocketServer = webSocketServer;
        this.messageBus = messageBus;
        this.settings = settings;

        this.gson = new Gson();
    }

    public void start() {
        SocketAddress bindAddress = new InetSocketAddress(settings.bindHost(), settings.bindPort());
        webSocketServer.start(bindAddress, this);

        messageBus.subscribeAll(this::onMessageBus);

        log.info("Started on {}", bindAddress);
    }

    private void onMessageBus(Message message) {
        clients.forEach(c ->
                c.send(gson.toJson(new MessageObject(message)))
        );
    }

    @Override
    public void onOpen(WebSocket socket) {
        clients.add(socket);
        log.info("Client connected {}", socket.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket socket, int code, String reason, boolean remote) {
        clients.remove(socket);
        log.info("Client disconnected {} {} {} remote:{}", socket.getRemoteSocketAddress(), code, reason, remote);
    }

    @Override
    public void onMessage(WebSocket socket, String message) {
        log.info("Message received {} {}", socket.getRemoteSocketAddress(), message);
        messageBus.publish(new RawWebSocketMessage(this, message));
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
