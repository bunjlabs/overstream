package com.overstreamapp.osc.netty;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.OscHandler;
import com.overstreamapp.osc.OscWriteException;
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
    public void send(OscPacket packet)  {
        if(channel != null) {
            channel.writeAndFlush(packet);
        }
    }
}
