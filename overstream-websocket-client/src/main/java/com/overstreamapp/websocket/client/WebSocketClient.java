package com.overstreamapp.websocket.client;

import com.overstreamapp.websocket.WebSocketHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface WebSocketClient {

    void connect(SocketAddress remoteAddress, WebSocketHandler handler);
}
