package com.overstreamapp.network;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ThreadFactory;

public interface EventLoopGroupManager {

    ThreadFactory getThreadFactory();

    EventLoopGroup getBossEventLoopGroup();

    EventLoopGroup getWorkerEventLoopGroup();

}
