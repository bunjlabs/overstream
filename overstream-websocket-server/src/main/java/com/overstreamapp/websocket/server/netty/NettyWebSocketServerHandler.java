package com.overstreamapp.websocket.server.netty;

import com.overstreamapp.websocket.WebSocketHandler;
import com.overstreamapp.websocket.netty.NettyWebSocket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/";
    private final WebSocketHandler handler;
    private WebSocketServerHandshaker handshaker;

    private NettyWebSocket webSocket;

    public NettyWebSocketServerHandler(WebSocketHandler handler) {
        this.handler = handler;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            res.headers().add(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

    public static boolean isKeepAlive(HttpMessage message) {
        String connection = message.headers().get(HttpHeaderNames.CONNECTION);
        if (connection != null && AsciiString.contentEqualsIgnoreCase(HttpHeaderValues.CLOSE, connection)) {
            return false;
        }

        if (message.protocolVersion().isKeepAliveDefault()) {
            return !AsciiString.contentEqualsIgnoreCase(HttpHeaderValues.CLOSE, connection);
        } else {
            return AsciiString.contentEqualsIgnoreCase(HttpHeaderValues.KEEP_ALIVE, connection);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (req.method() != HttpMethod.GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if (!WEBSOCKET_PATH.equals(req.uri())) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req),
                null,
                true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
            handler.onOpen(webSocket = new NettyWebSocket(ctx,
                    URI.create(String.format("ws://%s:%d/", socketAddress.getHostName(), socketAddress.getPort()))));
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            var closeFrame = (CloseWebSocketFrame) frame;
            handler.onClose(webSocket, closeFrame.statusCode(), closeFrame.reasonText(), true);
            handshaker.close(ctx.channel(), closeFrame.retain());
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof TextWebSocketFrame) {
            handler.onMessage(webSocket, ((TextWebSocketFrame) frame).text());
        } else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        handler.onError(webSocket, cause);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handler.onStart();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }
}