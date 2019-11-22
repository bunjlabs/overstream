package com.overstreamapp.websocket.client;

import com.overstreamapp.websocket.WebSocketHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

public interface WebSocketClient {

    void connect(URI uri, WebSocketHandler handler);
}
