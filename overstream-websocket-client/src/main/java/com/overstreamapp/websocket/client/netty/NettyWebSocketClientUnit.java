package com.overstreamapp.websocket.client.netty;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.websocket.client.WebSocketClient;

public class NettyWebSocketClientUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(NettyWebSocketClient.class).auto();
        c.bind(WebSocketClient.class).to(NettyWebSocketClient.class);
    }
}
