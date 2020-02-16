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

package com.overstreamapp.store.support;

import com.overstreamapp.store.*;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

abstract class AbstractStore<T> implements Store<T> {

    protected final Logger logger;
    private final Set<StoreSubscriber<T>> subscribers;
    private final DefaultStoreKeeper storeKeeper;
    private final Class<T> type;
    private final Reducer<T> reducer;
    private volatile T initialState;
    private volatile T currentState;

    AbstractStore(DefaultStoreKeeper storeKeeper, Class<T> type, Reducer<T> rootReducer, T initialState) {
        this.logger = storeKeeper.getLogger();
        this.subscribers = Collections.newSetFromMap(Collections.synchronizedMap(new IdentityHashMap<>()));
        this.storeKeeper = storeKeeper;
        this.type = type;
        this.initialState = initialState;
        this.reducer = rootReducer;
    }

    void initialize() {
        var lastState = findLast();

        if (lastState == null) {
            currentState = initialState;
            doPersist(initialState);
        } else {
            currentState = lastState;
        }
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T getState() {
        return currentState;
    }

    @Override
    public void dispatch(Action action) {
        logger.trace("Dispatch: {} {}", type, action);

        var oldState = this.currentState;
        var newState = reducer.reduce(action, oldState);

        if (newState != null && oldState != newState && !newState.equals(oldState)) {
            this.currentState = newState;

            doPersist(newState);

            notifyGlobalSubscribers(newState);
            notifySubscribers(newState);
        }
    }

    @Override
    public void dispatch(T newState) {
        dispatch(new ValueAction(newState));
    }

    @Override
    public StoreSubscription subscribe(StoreSubscriber<T> subscriber) {
        logger.trace("Subscription: {} {}", type, subscriber);

        subscribers.add(subscriber);

        var subscription = new StoreSubscription() {
            @Override
            public void unsubscribe() {
                subscribers.remove(subscriber);
            }

            @Override
            public void forceUpdate() {
                var history = findAll();
                if (history.isEmpty()) {
                    if (currentState != null) {
                        subscriber.onChange(currentState);
                    }
                } else {
                    history.forEach(subscriber::onChange);
                }
            }
        };

        subscription.forceUpdate();

        return subscription;
    }

    public abstract void persist(T state);

    public abstract List<T> findAll();

    public abstract T findLast();

    private void notifyGlobalSubscribers(T state) {
        storeKeeper.notifySubscribers(state);
    }

    private void notifySubscribers(T state) {
        subscribers.forEach(s -> s.onChange(state));
    }

    private void doPersist(T state) {
        if (state != null) persist(state);
    }
}
