/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.overstreamapp.websocket.client.netty;

import com.overstreamapp.network.ConnectionRegistry;
import com.overstreamapp.websocket.WebSocketHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;

import java.net.URI;

class NettyWebSocketClientInitializer extends ChannelInitializer<SocketChannel> {
    private final URI uri;
    private final SslContext sslContext;
    private final ConnectionRegistry connectionRegistry;
    private final WebSocketHandler handler;

    NettyWebSocketClientInitializer(URI uri, SslContext sslContext, ConnectionRegistry connectionRegistry, WebSocketHandler handler) {
        this.uri = uri;
        this.sslContext = sslContext;
        this.connectionRegistry = connectionRegistry;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch)  {
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

        connectionRegistry.pushChannel(ch);
    }
}
