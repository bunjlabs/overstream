package com.overstreamapp.websocket;

import java.nio.ByteBuffer;

public interface WebSocketHandler {

    void onOpen(WebSocket socket);

    void onClose(WebSocket socket, int code, String reason, boolean remote);

    void onMessage(WebSocket socket, String message);

    void onMessage(WebSocket socket, ByteBuffer bytes);

    void onError(WebSocket socket, Throwable ex);

    void onStart();

}

