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

package com.overstreamapp.event.support;

import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.event.*;
import org.slf4j.Logger;

import java.util.*;

public class DefaultEventKeeper implements EventKeeper {

    private final Logger logger;
    private final Map<Class<?>, Event<?>> eventMap = new HashMap<>();
    private final Set<EventSubscriber<Object>> subscribers = Collections.newSetFromMap(new IdentityHashMap<>());

    @Inject
    public DefaultEventKeeper(Logger logger) {
        this.logger = logger;
    }

    @Override
    public <T> EventBuilder<T> eventBuilder(Class<T> type) {
        return new DefaultEventBuilder<>(this, type);
    }

    @Override
    public <T> Event<T> getEvent(Class<T> type) {
        var rawEvent = eventMap.get(type);
        if (rawEvent != null) {
            @SuppressWarnings("unchecked")
            var event = (Event<T>) rawEvent;
            return event;
        }
        return null;
    }

    @Override
    public <T> EventSubscription subscribe(Class<T> type, EventSubscriber<T> subscriber) {
        var event = getEvent(type);

        if (event != null) {
            return event.subscribe(subscriber);
        }

        return new EventSubscription() {
        };
    }

    @Override
    public EventSubscription subscribe(EventSubscriber<Object> subscriber) {
        logger.trace("Global subscription: {}", subscriber);

        subscribers.add(subscriber);
        return new EventSubscription() {
            @Override
            public void unsubscribe() {
                subscribers.remove(subscriber);
            }
        };
    }

    Logger getLogger() {
        return logger;
    }

    void registerEvent(Event event) {
        eventMap.put(event.getType(), event);
    }

    void notifySubscribers(Object event) {
        subscribers.forEach(s -> s.onEvent(event));
    }
}
