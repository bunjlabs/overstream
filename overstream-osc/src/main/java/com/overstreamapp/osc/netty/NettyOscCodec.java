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

        out.add(new DatagramPacket(Unpooled.copiedBuffer(byteBuffer), (InetSocketAddress) ctx.channel().remoteAddress()));
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
