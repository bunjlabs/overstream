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
