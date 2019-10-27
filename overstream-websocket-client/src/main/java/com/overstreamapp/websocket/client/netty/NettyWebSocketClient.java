package com.overstreamapp.websocket.client.netty;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NettyWebSocketClient implements WebSocketClient {
    private final EventLoopGroupManager loopGroupManager;

    @Inject
    public NettyWebSocketClient(EventLoopGroupManager loopGroupManager) {
        this.loopGroupManager = loopGroupManager;
    }


    @Override
    public void connect(SocketAddress remoteAddress, WebSocketHandler handler) {

    }
}
