package com.overstreamapp.websocket.server;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.websocket.server.netty.NettyWebSocketServer;

public class NettyWebSocketServerUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(NettyWebSocketServer.class).auto().in(Singleton.class);
        c.bind(WebSocketServer.class).to(NettyWebSocketServer.class);
    }
}
