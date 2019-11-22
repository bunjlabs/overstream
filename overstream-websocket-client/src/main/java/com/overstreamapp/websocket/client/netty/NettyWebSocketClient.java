package com.overstreamapp.websocket.client.netty;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.client.WebSocketClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;

import javax.net.ssl.SSLException;
import java.net.URI;

public class NettyWebSocketClient implements WebSocketClient {
    private final Logger logger;
    private final EventLoopGroupManager loopGroupManager;

    @Inject
    public NettyWebSocketClient(Logger logger, EventLoopGroupManager loopGroupManager) {
        this.logger = logger;
        this.loopGroupManager = loopGroupManager;
    }


    @Override
    public void connect(URI uri, WebSocketHandler handler) {
        EventLoopGroup workerGroup = loopGroupManager.getWorkerEventLoopGroup();

        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            logger.error("Only WS(S) is supported.");
            return;
        }

        SslContext sslContext = null;
        if ("wss".equalsIgnoreCase(scheme)) {
            try {
                sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (SSLException e) {
                logger.warn("Exception while enabling SSL context", e);
            }
        }

        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyWebSocketClientInitializer(uri, sslContext, handler));

        try {
            Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        } catch (InterruptedException e) {
            logger.error("Unable to start netty websocket server", e);
        }
    }
}
