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

package com.overstreamapp.http.support;

import com.overstreamapp.http.HttpRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;

import javax.net.ssl.SSLException;

class NettyHttpConnection {

    private final Logger logger;
    private final NettyHttpClient httpClient;
    private final EventLoopGroup executors;
    private final ConnectionPoint connectionPoint;
    private Channel channel;

    NettyHttpConnection(Logger logger, NettyHttpClient httpClient, EventLoopGroup executors, ConnectionPoint connectionPoint) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.executors = executors;
        this.connectionPoint = connectionPoint;
    }

    void start(final HttpRequest request) {
        SslContext sslCtx;
        if (connectionPoint.isSsl()) {
            try {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } catch (SSLException e) {
                sslCtx = null;
            }
        } else {
            sslCtx = null;
        }

        Bootstrap b = new Bootstrap();
        b.group(executors)
                .channel(NioSocketChannel.class)
                .handler(new NettyHttpClientInitializer(sslCtx));

        var channelFuture = b.connect(connectionPoint.getHost(), connectionPoint.getPort());

        channelFuture.awaitUninterruptibly().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channel = future.channel();

                sendRequest(request);
            } else {
                channel.close();
            }
        });

    }

    private void sendRequest(HttpRequest request) {
        var nettyMethod = io.netty.handler.codec.http.HttpMethod.valueOf(request.getMethod().name());
        var nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, request.getUri().getRawPath());

        request.getHeaders().forEach((name, value) -> nettyRequest.headers().set(name, value));
        nettyRequest.headers().set(HttpHeaderNames.HOST, connectionPoint.getHost());
        nettyRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        nettyRequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

        channel.writeAndFlush(nettyRequest);
    }
}
