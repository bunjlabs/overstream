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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;

class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final Logger logger;
    private final NettyHttpConnection connection;

    NettyHttpClientHandler(Logger logger, NettyHttpConnection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
        var context = connection.getCurrentContext();

        logger.trace("Response {} for {}", response.status(), context.getConnectionPoint());

        context.getHandler().onResponse(response);

        connection.processOne();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        var context = connection.getCurrentContext();

        logger.trace("Error {} for {}", cause, context.getConnectionPoint());

        context.getHandler().onError(cause);

        connection.processOne();
    }
}
