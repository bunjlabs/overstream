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

package com.overstreamapp.keeper.support;

import com.bunjlabs.fuga.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.overstreamapp.keeper.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DefaultKeeper implements Keeper {
    private static final String STATE_COLLECTION_PREFIX = "STATE_";

    private final Logger logger;
    private final MongoDatabase database;

    private final Map<Class<? extends StateObject>, InternalState<StateObject>> stateMap;
    private final Map<Class<? extends StateObject>, List<StateListener<?>>> stateListenersMap;
    private final List<StateListener<StateObject>> stateListenersList;

    private final Map<Class<? extends EventObject>, InternalEvent<EventObject>> eventMap;
    private final Map<Class<? extends EventObject>, List<EventListener>> eventListenersMap;
    private final List<EventListener> eventListenersList;

    @Inject
    public DefaultKeeper(Logger logger, KeeperSettings settings, MongoDatabase database) {
        this.logger = logger;

        var codecRegistry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                )
        );

        this.database = database.withCodecRegistry(codecRegistry);

        this.stateMap = new ConcurrentHashMap<>();
        this.stateListenersList = new CopyOnWriteArrayList<>();
        this.stateListenersMap = new ConcurrentHashMap<>();

        this.eventMap = new ConcurrentHashMap<>();
        this.eventListenersList = new CopyOnWriteArrayList<>();
        this.eventListenersMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends StateObject> StateBuilder<T> stateBuilder(Class<T> stateClass) {
        return new DefaultStateBuilder<>(this, stateClass);
    }

    @Override
    public <T extends EventObject> EventBuilder<T> eventBuilder(Class<T> eventClass) {
        return new DefaultEventBuilder<>(this, eventClass);
    }

    @Override
    public <T extends StateObject> void subscribe(Class<T> type, StateListener<T> listener) {
        var listeners = stateListenersMap.get(type);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            stateListenersMap.put(type, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public <T extends EventObject> void subscribe(Class<T> type, EventListener<T> listener) {
        var listeners = eventListenersMap.get(type);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            eventListenersMap.put(type, listeners);
        }
        listeners.add(listener);
    }

    @Override
    public void subscribeAll(StateListener<StateObject> listener) {
        stateListenersList.add(listener);
    }

    @Override
    public void subscribeAll(EventListener<EventObject> listener) {
        eventListenersList.add(listener);
    }

    @Override
    public void burstState(StateListener<StateObject> listener) {
        logger.debug("Burst state");

        stateMap.values().forEach(state -> {
            List<StateObject> stateObjects = state.load();

            for (StateObject stateObject : stateObjects) {
                listener.onUpdate(state.getInfo(), stateObject);
            }
        });
    }

    @Override
    public List<StateInfo> getAllStateInfo() {
        return stateMap.values().stream().map(State::getInfo).collect(Collectors.toList());
    }

    @Override
    public List<EventInfo> getAllEventInfo() {
        return eventMap.values().stream().map(Event::getInfo).collect(Collectors.toList());
    }

    /*
     * Internals
     */

    private boolean collectionExists(String name) {
        final MongoIterable<String> iterable = database.listCollectionNames();
        try (final MongoCursor<String> it = iterable.iterator()) {
            while (it.hasNext()) {
                var db = it.next();
                if (db.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    <T extends StateObject> State<T> createState(StateInfo<T> info) {
        InternalState<T> state = (InternalState<T>) stateMap.get(info.getType());

        if (state != null) {
            return state;
        }

        logger.debug("Create new state {}", info);
        state = new InternalState<>(this, info);

        if (info.getPersistenceStrategy() == StatePersistenceStrategy.LIST_CAPPED
                || info.getPersistenceStrategy() == StatePersistenceStrategy.LIST_LIMITED) {
            var collectionName = STATE_COLLECTION_PREFIX + info.getName();
            if (!collectionExists(collectionName)) {
                CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();

                if (info.getPersistenceStrategy() == StatePersistenceStrategy.LIST_CAPPED) {
                    createCollectionOptions.capped(true).sizeInBytes(1024 * 4 * info.getSize()).maxDocuments(info.getSize());
                }


                database.createCollection(collectionName, createCollectionOptions);
            }

            MongoCollection<T> collection = database.getCollection(collectionName, info.getType());

            state.setCollection(collection);
        }

        stateMap.put(info.getType(), (InternalState<StateObject>) state);

        return state;
    }

    <T extends StateObject> void fireStateChanged(StateInfo<T> info, T stateObject) {
        logger.trace("State changed {}", info);

        stateListenersList.forEach(l -> fireStateListener(l, info, stateObject));

        var listeners = stateListenersMap.get(info.getType());
        if (listeners != null) {
            listeners.forEach(l -> fireStateListener(l, info, stateObject));
        }
    }

    @SuppressWarnings("unchecked")
    private void fireStateListener(StateListener listener, StateInfo info, StateObject stateObject) {
        try {
            listener.onUpdate(info, stateObject);
        } catch (Throwable t) {
            logger.error("Error while process listener {}: {}", listener, t);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends EventObject> Event<T> createEvent(EventInfo<T> info) {
        InternalEvent<T> event = (InternalEvent<T>) eventMap.get(info.getType());

        if (event != null) {
            return event;
        }

        logger.debug("Create new event {}", info);
        event = new InternalEvent(this, info);

        eventMap.put(info.getType(), (InternalEvent<EventObject>) event);

        return event;
    }

    <T extends EventObject> void fireEvent(EventInfo<T> info, T eventObject) {
        logger.trace("Event fired {}", info);

        eventListenersList.forEach(l -> fireEventListener(l, info, eventObject));

        var listeners = eventListenersMap.get(info.getType());
        if (listeners != null) {
            listeners.forEach(l -> fireEventListener(l, info, eventObject));
        }
    }

    @SuppressWarnings("unchecked")
    private void fireEventListener(EventListener listener, EventInfo info, EventObject eventObject) {
        try {
            listener.onEvent(info, eventObject);
        } catch (Throwable t) {
            logger.error("Error while process listener {}: {}", listener, t);
        }
    }
}
