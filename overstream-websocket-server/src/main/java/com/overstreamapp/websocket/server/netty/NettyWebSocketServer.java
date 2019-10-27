package com.overstreamapp.websocket.server.netty;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.server.WebSocketServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;

import java.net.SocketAddress;

public class NettyWebSocketServer implements WebSocketServer {

    private final Logger logger;
    private final EventLoopGroupManager loopGroupManager;

    @Inject
    public NettyWebSocketServer(Logger logger, EventLoopGroupManager loopGroupManager) {
        this.logger = logger;
        this.loopGroupManager = loopGroupManager;
    }

    @Override
    public void start(SocketAddress socketAddress, WebSocketHandler handler) {
        EventLoopGroup bossGroup = loopGroupManager.getBossEventLoopGroup();
        EventLoopGroup workerGroup = loopGroupManager.getWorkerEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyWebSocketServerInitializer(handler));

        try {
            b.bind(socketAddress).sync();
        } catch (InterruptedException e) {
            logger.error("Unable to start netty websocket server", e);
        }
    }
}
