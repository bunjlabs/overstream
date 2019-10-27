package com.overstreamapp.websocket;

public interface WebSocketHandler {

    void onOpen(WebSocket socket);

    void onClose(WebSocket socket, int code, String reason, boolean remote);

    void onMessage(WebSocket socket, String message);

    void onError(WebSocket socket, Throwable ex);

    void onStart();

}

