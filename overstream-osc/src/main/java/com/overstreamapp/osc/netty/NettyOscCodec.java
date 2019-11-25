package com.overstreamapp.osc.netty;

import com.overstreamapp.osc.OscUtils;
import com.overstreamapp.osc.types.OscBundle;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

public class NettyOscCodec extends MessageToMessageCodec<DatagramPacket, OscPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, OscPacket msg, List<Object> out) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);

        byteBuffer.clear();
        msg.write(byteBuffer);
        byteBuffer.flip();

        out.add(new DatagramPacket(Unpooled.wrappedBuffer(byteBuffer), (InetSocketAddress) ctx.channel().remoteAddress()));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuffer nioBuffer = msg.content().nioBuffer();

        OscPacket packet;
        if (OscUtils.isBundle(nioBuffer)) {
            packet = new OscBundle();
        } else {
            packet = new OscMessage();
        }

        packet.read(nioBuffer);
        out.add(packet);
    }
}
