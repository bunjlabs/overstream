package com.overstreamapp.osc.netty;

import com.overstreamapp.osc.OscChannel;
import com.overstreamapp.osc.OscHandler;
import com.overstreamapp.osc.OscWriteException;
import com.overstreamapp.osc.types.OscBundle;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.EventExecutorGroup;

public class NettyOscClientHandler extends SimpleChannelInboundHandler<OscPacket> {

    private OscHandler oscHandler;
    private OscChannel oscChannel;

    NettyOscClientHandler(OscHandler oscHandler) {
        this.oscHandler = oscHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.oscChannel = new InternalOscChannel(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OscPacket packet) throws Exception {
        if(packet instanceof OscMessage) {
            oscHandler.onMessage(oscChannel, (OscMessage) packet);
        } else if(packet instanceof OscBundle) {
            oscHandler.onBundle(oscChannel, (OscBundle) packet);
        }
    }

    private static class InternalOscChannel implements OscChannel {
        private final ChannelHandlerContext ctx;

        InternalOscChannel(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void send(OscPacket packet) {
            ctx.writeAndFlush(packet);
        }
    }
}
