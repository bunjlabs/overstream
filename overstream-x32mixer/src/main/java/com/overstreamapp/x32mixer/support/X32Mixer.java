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

package com.overstreamapp.x32mixer.support;

import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscChannel;
import com.overstreamapp.osc.OscWriteException;
import com.overstreamapp.osc.types.OscMessage;
import com.overstreamapp.osc.types.OscType;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

class X32Mixer {
    private final Logger logger;

    private final X32OscHandler x32OscHandler;
    private final Map<String, X32Subscription<OscType>> subscriptions;
    private final List<X32Meter> meters;

    private final EventLoopGroupManager eventLoopGroupManager;
    private final OscChannel oscChannel;

    X32Mixer(Logger logger, EventLoopGroupManager eventLoopGroupManager, OscChannel oscChannel) {
        this.logger = logger;
        this.eventLoopGroupManager = eventLoopGroupManager;
        this.oscChannel = oscChannel;

        this.x32OscHandler = new X32OscHandler(this);
        this.subscriptions = new ConcurrentHashMap<>();
        this.meters = new CopyOnWriteArrayList<>();
    }

    void start() {
        eventLoopGroupManager.getWorkerEventLoopGroup().scheduleAtFixedRate(
                this::onTimer, 5, 5, TimeUnit.SECONDS);
    }

    void channelOn(int channel, boolean on) {
        send(String.format("/ch/%02d/mix/on", channel), on ? 1 : 0);
    }

    void busOn(int bus, boolean on) {
        send(String.format("/bus/%02d/mix/on", bus), on ? 1 : 0);
    }

    private void onTimer() {
        if (this.subscriptions.isEmpty() || this.meters.isEmpty()) return;

        if (x32OscHandler.getLastMessageTime() > 0 && System.currentTimeMillis() - x32OscHandler.getLastMessageTime() > 5000) {
            logger.warn("No response from mixer. Renewing ...");

            subscriptions.values().forEach(sub -> enableSubscription(sub.getAddress()));
            enableMeters();
        } else {
            logger.trace("Renew subscriptions");

            try {
                oscChannel.send(new OscMessage("/renew"));
            } catch (OscWriteException ex) {
                logger.error("Unable to renew mixer subscriptions", ex);
            }
        }

    }

    X32OscHandler getX32OscHandler() {
        return x32OscHandler;
    }

    @SuppressWarnings("unchecked")
    <T extends OscType> void subscribe(String address, X32SubscriptionListener<T> listener) {
        enableSubscription(address);

        var subscription = new X32Subscription<>(address, listener);
        subscriptions.put(address, (X32Subscription<OscType>) subscription);

        logger.debug("Subscribed to {}", address);
    }


    void meters(int channel, int type, float sensitivity, boolean latch, X32MeterListener listener) {
        if (meters.isEmpty()) {
            enableMeters();
        }

        meters.add(new X32Meter(channel, type, sensitivity, latch, listener));
        logger.debug("Subscribed to meter({}, {}) ", channel, type);
    }


    void fireSubscriptionUpdate(String address, OscType value) {
        var subscription = subscriptions.get(address);

        if (subscription != null) {
            subscription.onData(value);
        }
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

    private void enableSubscription(String address) {
        send("/subscribe", address, 5);
    }

    private void enableMeters() {
        send("/meters", "/meters/1", 10);
    }
}
