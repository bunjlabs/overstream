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
    private final ThreadFactory threadFactory;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    @Inject
    @Singleton
    public DefaultEventLoopGroupManager(Logger log, EventLoopGroupSettings settings) {
        this.log = log;
        this.threadFactory = new DefaultThreadFactory();

        String type = settings.type();
        int bossThreads = settings.bossThreads() < 0 ? 0 : settings.bossThreads();
        int workerThreads = settings.bossThreads() < 0 ? 0 : settings.workerThreads();

        if (type.equalsIgnoreCase("epoll")) {
            this.bossEventLoopGroup = new EpollEventLoopGroup(bossThreads, threadFactory);
            this.workerEventLoopGroup = new EpollEventLoopGroup(workerThreads, threadFactory);
        } else {
            type = "nio";
            this.bossEventLoopGroup = new NioEventLoopGroup(bossThreads, threadFactory);
            this.workerEventLoopGroup = new NioEventLoopGroup(workerThreads, threadFactory);
        }

        log.info("Initialized with {} event loop group, {} boss threads and {} worker threads", type, bossThreads, workerThreads);
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return threadFactory;
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
