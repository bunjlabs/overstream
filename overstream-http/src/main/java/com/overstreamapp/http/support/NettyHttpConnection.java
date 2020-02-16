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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;

import javax.net.ssl.SSLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

class NettyHttpConnection {

    private final Logger logger;
    private final EventLoopGroup executors;
    private final ConnectionPoint connectionPoint;
    private final Queue<InternalContext> contextQueue;
    private final AtomicReference<InternalContext> currentContext;

    private volatile boolean starting = false;
    private Channel channel;

    NettyHttpConnection(Logger logger, EventLoopGroup executors, ConnectionPoint connectionPoint) {
        this.logger = logger;
        this.executors = executors;
        this.connectionPoint = connectionPoint;
        this.contextQueue = new ConcurrentLinkedQueue<>();
        this.currentContext = new AtomicReference<>();
    }

    void execute(final InternalContext context) {
        enqueueRequest(context);

        if (starting) {
            if (contextQueue.isEmpty()) {
                enqueueRequest(context);
                processOne();
            } else {
                enqueueRequest(context);
            }
            return;
        }

        starting = true;

        logger.debug("Starting connection to {}", connectionPoint);

        SslContext sslCtx;
        if (connectionPoint.isSsl()) {
            try {
                sslCtx = SslContextBuilder.forClient().build();
            } catch (SSLException e) {
                sslCtx = null;
            }
        } else {
            sslCtx = null;
        }

        var bootstrap = new Bootstrap()
                .group(executors)
                .channel(NioSocketChannel.class)
                .handler(new NettyHttpClientInitializer(logger, this, sslCtx));

        var channelFuture = bootstrap.connect(connectionPoint.getHost(), connectionPoint.getPort());

        channelFuture.awaitUninterruptibly().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                channel = future.channel();

                logger.debug("Connection to {} success", connectionPoint);
                enqueueRequest(context);
                processOne();
            } else {
                channel.close();

                logger.debug("Connection to {} failed", connectionPoint);
            }
        });
    }

    InternalContext getCurrentContext() {
        return currentContext.get();
    }


    void processOne() {
        var context = contextQueue.poll();

        if (context != null) {
            currentContext.set(context);
            sendRequest(context.getRequest());
        }
    }

    private void enqueueRequest(InternalContext context) {
        contextQueue.add(context);
    }

    private void sendRequest(FullHttpRequest request) {
        request.headers().set(HttpHeaderNames.HOST, connectionPoint.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

        logger.debug("Executing request {} for {}", connectionPoint, request);

        channel.writeAndFlush(request);

    }
}
