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

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.OscHandler;
import com.overstreamapp.osc.types.OscPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;

import java.net.SocketAddress;

public class NettyOscClient implements OscClient {

    private final Logger logger;
    private final EventLoopGroupManager loopGroupManager;
    private Channel channel;

    @Inject
    public NettyOscClient(Logger logger, EventLoopGroupManager loopGroupManager) {
        this.logger = logger;
        this.loopGroupManager = loopGroupManager;
    }

    @Override
    public void start(SocketAddress socketAddress, OscHandler handler) {
        EventLoopGroup workerGroup = loopGroupManager.getWorkerEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new NettyOscClientInitializer(handler));

        try {
            this.channel = b.connect(socketAddress).sync().channel();
        } catch (InterruptedException e) {
            logger.error("Unable to start netty websocket server", e);
        }
    }

    @Override
    public void send(OscPacket packet) {
        if (channel != null) {
            channel.writeAndFlush(packet);
        }
    }
}
