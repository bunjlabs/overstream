package com.overstreamapp.network;

import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Singleton;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;

import java.util.concurrent.ThreadFactory;

public class DefaultEventLoopGroupManager implements EventLoopGroupManager {
    private final Logger log;
    private final ThreadFactory commonThreadFactory;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    @Inject
    @Singleton
    public DefaultEventLoopGroupManager(Logger log, EventLoopGroupSettings settings) {
        this.log = log;
        this.commonThreadFactory = new DefaultThreadFactory();

        var bossThreadFactory = new DefaultThreadFactory("BossEventLoop");
        var workerThreadFactory = new DefaultThreadFactory("WorkerEventLoop");

        var bossThreads = settings.bossThreads() < 0 ? 0 : settings.bossThreads();
        var workerThreads = settings.workerThreads() < 0 ? 0 : settings.workerThreads();

        var type = settings.type();
        if (type.equalsIgnoreCase("epoll")) {
            this.bossEventLoopGroup = new EpollEventLoopGroup(bossThreads, bossThreadFactory);
            this.workerEventLoopGroup = new EpollEventLoopGroup(workerThreads, workerThreadFactory);
        } else {
            type = "nio";
            this.bossEventLoopGroup = new NioEventLoopGroup(bossThreads, bossThreadFactory);
            this.workerEventLoopGroup = new NioEventLoopGroup(workerThreads, workerThreadFactory);
        }

        System.setProperty("io.netty.tryReflectionSetAccessible", "false");

        log.info("Initialized with {} event loop group, {} boss threads and {} worker threads", type, bossThreads, workerThreads);
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return commonThreadFactory;
    }

    @Override
    public EventLoopGroup getBossEventLoopGroup() {
        return bossEventLoopGroup;
    }

    @Override
    public EventLoopGroup getWorkerEventLoopGroup() {
        return workerEventLoopGroup;
    }
}
