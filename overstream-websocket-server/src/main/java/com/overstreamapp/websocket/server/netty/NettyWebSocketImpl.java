package com.overstreamapp.websocket.server.netty;

import com.bunjlabs.fuga.util.ObjectUtils;
import com.overstreamapp.websocket.WebSocket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class NettyWebSocketImpl implements WebSocket {

    private final ChannelHandlerContext context;
    private final WebSocketServerHandshaker handshaker;

    public NettyWebSocketImpl(ChannelHandlerContext context, WebSocketServerHandshaker handshaker) {
        this.context = context;
        this.handshaker = handshaker;
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
        NettyWebSocketImpl that = (NettyWebSocketImpl) o;
        return context.equals(that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context);
    }
}
