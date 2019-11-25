package com.overstreamapp.osc.netty;

import com.overstreamapp.osc.OscHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;

public class NettyOscClientInitializer extends ChannelInitializer<DatagramChannel> {
    private final OscHandler oscHandler;

    NettyOscClientInitializer(OscHandler oscHandler) {
        this.oscHandler = oscHandler;
    }

    @Override
    protected void initChannel(DatagramChannel ch) {
        ch.pipeline().addLast(new NettyOscCodec());
        ch.pipeline().addLast(new NettyOscClientHandler(oscHandler));

    }
}
