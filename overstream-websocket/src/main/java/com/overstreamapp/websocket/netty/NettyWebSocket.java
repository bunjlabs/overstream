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

package com.overstreamapp.websocket.netty;

import com.bunjlabs.fuga.util.ObjectUtils;
import com.overstreamapp.websocket.WebSocket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Objects;

public class NettyWebSocket implements WebSocket {

    private final ChannelHandlerContext context;
    private final URI uri;

    public NettyWebSocket(ChannelHandlerContext context, URI uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    public void send(String text) {
        context.channel().write(new TextWebSocketFrame(text));
        context.channel().flush();
    }

    @Override
    public void send(ByteBuffer bytes) {
        context.channel().write(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
        context.channel().flush();
    }

    @Override
    public void send(byte[] bytes) {
        context.channel().write(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
        context.channel().flush();
    }

    @Override
    public void close(int code, String message) {
        context.channel().write(new CloseWebSocketFrame(new WebSocketCloseStatus(code, message)));
        context.channel().flush();
        context.channel().close();
    }

    @Override
    public void close(int code) {
        close(code, "");
    }

    @Override
    public void close() {
        close(1000, "");
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return (InetSocketAddress) context.channel().remoteAddress();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return (InetSocketAddress) context.channel().localAddress();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean isOpen() {
        return context.channel().isOpen();
    }

    @Override
    public boolean isClosed() {
        return !context.channel().isOpen();
    }

    @Override
    public String toString() {
        return ObjectUtils.toStringJoiner(this)
                .add("context", context)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NettyWebSocket that = (NettyWebSocket) o;
        return context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context);
    }
}
