package com.overstreamapp.websocket.server;

import com.overstreamapp.websocket.WebSocketHandler;

import java.net.SocketAddress;

public interface WebSocketServer {

    void start(SocketAddress socketAddress, WebSocketHandler handler);

}
