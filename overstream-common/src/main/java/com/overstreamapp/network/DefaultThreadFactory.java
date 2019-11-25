package com.overstreamapp.network;

import com.bunjlabs.fuga.util.Assert;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class DefaultThreadFactory implements ThreadFactory {
    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;

    DefaultThreadFactory() {
        this.prefix = DefaultThreadFactory.class.getSimpleName() + "-";
    }

    DefaultThreadFactory(String poolName) {
        this.prefix = Assert.hasText(poolName) + "-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var thread = new Thread(runnable, prefix + nextId.incrementAndGet());
        try {
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
        } catch (Throwable ignored) {
        }
        return thread;
    }
}
