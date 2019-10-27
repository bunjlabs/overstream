package com.overstreamapp.x32mixer;

import com.overstreamapp.network.EventLoopGroupManager;
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

    private final Map<String, X32Subscription<OscType>> subscriptions;
    private final List<X32Meter> meters;

    private final Logger logger;
    private final OscClient oscClient;
    private final SocketAddress remoteAddress;

    X32Mixer(SocketAddress remoteAddress, Logger logger, EventLoopGroupManager eventLoopGroupManager, OscClient oscClient) {
        this.remoteAddress = remoteAddress;
        this.logger = logger;
        this.oscClient = oscClient;

        this.subscriptions = new ConcurrentHashMap<>();
        this.meters = new CopyOnWriteArrayList<>();

        eventLoopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                this::onTimer, 5, 5, TimeUnit.SECONDS);
    }

    private void onTimer() {
        logger.trace("Renew subscriptions");

        try {
            oscClient.send(new OscMessage("/renew"));
        } catch (OscWriteException ex) {
            logger.error("Unable to renew mixer subscriptions", ex);
        }
    }

    void start() {
        this.oscClient.start(remoteAddress, new X32OscHandler(this));
    }

    <T extends OscType> void subscribe(String address, X32SubscriptionListener<T> listener) {
        send("/subscribe", address, 5);

        X32Subscription subscription = new X32Subscription(address, listener);
        subscriptions.put(address, subscription);

        logger.debug("Subscribed to {}", address);
    }

    void fireSubscriptionUpdate(String address, OscType value) {
        var subscription = subscriptions.get(address);

        if (subscription != null) {
            subscription.onData(value);
        }
    }

    void meters(int channel, int type, float sensitivity, X32MeterListener listener) {
        if (meters.isEmpty()) {
            send("/meters", "/meters/1", 10);
        }

        X32Meter meter = new X32Meter(channel, type, sensitivity, listener);
        meters.add(meter);
        logger.debug("Subscribed to meter({}, {}) ", channel, type);
    }

    void fireMetersUpdate(float[] nativeFloats) {
        meters.forEach(s -> {
            s.onData(nativeFloats);
        });
    }

    void forceSubscribesUpdate() {
        subscriptions.values().forEach(X32Subscription::fireValueChanged);
    }

    void forceMetersUpdate() {
        meters.forEach(X32Meter::fireValueChanged);
    }

    private void send(String address, Object... arguments) {
        try {
            oscClient.send(new OscMessage(address, arguments));
        } catch (OscWriteException ex) {
            logger.error("Unable to write message", ex);
        }
    }

}
