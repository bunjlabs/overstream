package com.overstreamapp.x32mixer;

import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscChannel;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.OscWriteException;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscType;
import org.slf4j.Logger;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

class X32Mixer {
    private final Logger logger;

    private final Map<String, X32Subscription<OscType>> subscriptions;
    private final List<X32Meter> meters;

    private final OscChannel oscChannel;

    X32Mixer(Logger logger, EventLoopGroupManager eventLoopGroupManager, OscChannel oscChannel) {
        this.logger = logger;
        this.oscChannel = oscChannel;

        this.subscriptions = new ConcurrentHashMap<>();
        this.meters = new CopyOnWriteArrayList<>();

        eventLoopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                this::onTimer, 5, 5, TimeUnit.SECONDS);
    }

    private void onTimer() {
        if(this.subscriptions.isEmpty() || this.meters.isEmpty()) return;

        logger.trace("Renew subscriptions");

        try {
            oscChannel.send(new OscMessage("/renew"));
        } catch (OscWriteException ex) {
            logger.error("Unable to renew mixer subscriptions", ex);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends OscType> void subscribe(String address, X32SubscriptionListener<T> listener) {
        send("/subscribe", address, 5);

        X32Subscription<T> subscription = new X32Subscription<>(address, listener);
        subscriptions.put(address, (X32Subscription<OscType>) subscription);

        logger.debug("Subscribed to {}", address);
    }

    void fireSubscriptionUpdate(String address, OscType value) {
        var subscription = subscriptions.get(address);

        if (subscription != null) {
            subscription.onData(value);
        }
    }

    void meters(int channel, int type, float sensitivity, boolean latch, X32MeterListener listener) {
        if (meters.isEmpty()) {
            send("/meters", "/meters/1", 10);
        }

        X32Meter meter = new X32Meter(channel, type, sensitivity, latch, listener);
        meters.add(meter);
        logger.debug("Subscribed to meter({}, {}) ", channel, type);
    }

    void fireMetersUpdate(float[] nativeFloats) {
        meters.forEach(s -> {
            s.onData(nativeFloats);
        });
    }

    private void send(String address, Object... arguments) {
        try {
            oscChannel.send(new OscMessage(address, arguments));
        } catch (OscWriteException ex) {
            logger.error("Unable to write message", ex);
        }
    }

}
