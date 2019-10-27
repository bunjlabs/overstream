package com.overstreamapp.websocket.server.netty;

import com.overstreamapp.websocket.WebSocketHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

public class NettyWebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket";
    private final WebSocketHandler handler;

    public NettyWebSocketServerInitializer(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new NettyWebSocketServerHandler(handler));
    }
}