package com.overstreamapp.network;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class DefaultThreadFactory implements ThreadFactory {
    private final AtomicInteger nextId = new AtomicInteger();
    private final String prefix;

    DefaultThreadFactory() {
        this.prefix = DefaultThreadFactory.class.getSimpleName() + "-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var thread = new Thread(runnable, prefix + nextId.incrementAndGet());
        try {
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.MAX_PRIORITY) {
                thread.setPriority(Thread.MAX_PRIORITY);
            }
        } catch (Throwable ignored) {
        }
        return thread;
    }
}
