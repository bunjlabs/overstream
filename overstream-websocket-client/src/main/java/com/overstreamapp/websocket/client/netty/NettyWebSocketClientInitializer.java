package com.overstreamapp.websocket.client.netty;

import com.overstreamapp.websocket.WebSocketHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;

import java.net.URI;

public class NettyWebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final URI uri;
    private final SslContext sslContext;
    private final WebSocketHandler handler;

    public NettyWebSocketClientInitializer(URI uri, SslContext sslContext, WebSocketHandler handler) {
        this.uri = uri;
        this.sslContext = sslContext;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        NettyWebSocketClientHandler nettyHandler = new NettyWebSocketClientHandler(
                uri, WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, false,
                EmptyHttpHeaders.INSTANCE, 1280000), handler);

        ChannelPipeline pipeline = ch.pipeline();

        if (sslContext != null) {
            pipeline.addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), uri.getPort()));
        }

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(nettyHandler);

    }
}
