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

import com.overstreamapp.event.Event;
import com.overstreamapp.event.EventSubscriber;
import com.overstreamapp.event.EventSubscription;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

class LocalEvent<T> implements Event<T> {

    private final Set<EventSubscriber<T>> subscribers = Collections.newSetFromMap(new IdentityHashMap<>());

    private final Logger logger;
    private final DefaultEventKeeper eventKeeper;
    private final Class<T> type;

    LocalEvent(DefaultEventKeeper eventKeeper, Class<T> type) {
        this.logger = eventKeeper.getLogger();
        this.eventKeeper = eventKeeper;
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void fire(T event) {
        logger.trace("Event: {} {}", type, event);

        if (event != null) {
            notifyGlobalSubscribers(event);
            notifySubscribers(event);
        }
    }

    @Override
    public EventSubscription subscribe(EventSubscriber<T> subscriber) {
        logger.trace("Subscription: {} {}", type, subscriber);

        subscribers.add(subscriber);

        return new EventSubscription() {
            @Override
            public void unsubscribe() {
                subscribers.remove(subscriber);
            }
        };
    }

    private void notifyGlobalSubscribers(T event) {
        eventKeeper.notifySubscribers(event);
    }

    private void notifySubscribers(T event) {
        subscribers.forEach(s -> s.onEvent(event));
    }

}
