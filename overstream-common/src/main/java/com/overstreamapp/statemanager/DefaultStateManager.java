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

package com.overstreamapp.statemanager;

import com.bunjlabs.fuga.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DefaultStateManager implements StateManager {
    private final Logger logger;
    private final Map<String, InternalState> stateMap;
    private final MongoDatabase database;
    private final List<StateUpdateListener> listenersList;
    private final Map<String, List<StateUpdateListener>> listenersMap;

    @Inject
    public DefaultStateManager(Logger logger, StateManagerSettings settings) {
        this.logger = logger;

        MongoClient mongoClient = new MongoClient(settings.mongoClient().host(), settings.mongoClient().port());
        this.database = mongoClient.getDatabase(settings.mongoClient().database());
        logger.info("Connected to MongoDB");

        this.stateMap = new ConcurrentHashMap<>();
        this.listenersList = new CopyOnWriteArrayList<>();
        this.listenersMap = new ConcurrentHashMap<>();
    }

    @Override
    public State createState(StateOptions options) {
        InternalState state = stateMap.get(options.getName());

        if (state != null) {
            logger.debug("Return existing state");
            return state;
        }

        logger.debug("Create new state {}", options);
        state = new InternalState();
        state.stateOptions = options;

        if (options.getHistoryStrategy() != HistoryStrategy.DISABLED) {
            if (!collectionExists(options.getName())) {
                CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();

                if (options.getHistoryStrategy() == HistoryStrategy.CAPPED) {
                    createCollectionOptions.capped(true)
                            .sizeInBytes(1024 * 4 * options.getHistorySize())
                            .maxDocuments(options.getHistorySize());
                }

                database.createCollection(options.getName(), createCollectionOptions);
            }
            state.collection = database.getCollection(options.getName());
        }

        stateMap.put(options.getName(), state);

        return state;
    }

    @Override
    public State getState(String channel) {
        return stateMap.get(channel);
    }

    @Override
    public StateObject getLastStateValue(String channel) {
        var state = stateMap.get(channel);

        if (state == null) {
            return null;
        }

        return state.lastState;
    }

    @Override
    public void subscribe(String channel, StateUpdateListener listener) {
        var listeners = listenersMap.get(channel);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            listenersMap.put(channel, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public void subscribeAll(StateUpdateListener listener) {
        listenersList.add(listener);
    }

    public List<StateOptions> getAllStateOption() {
        return stateMap.values().stream().map(s -> s.stateOptions).collect(Collectors.toList());
    }

    @Override
    public void pushAll(StateUpdateListener listener) {
        logger.debug("Burst state");

        stateMap.forEach((channel, state) -> {
            List<StateObject> stateObjects = state.load();

            for (StateObject stateObject : stateObjects) {
                listener.onUpdate(state.stateOptions, stateObject);
            }
        });
    }

    private void firePushEvent(StateOptions info, StateObject stateObject) {
        logger.trace("Fire push event {}", info);

        listenersList.forEach(l -> fireListenerEvent(l, info, stateObject));

        var listeners = listenersMap.get(info.getName());
        if (listeners != null) {
            listeners.forEach(l -> fireListenerEvent(l, info, stateObject));
        }
    }

    private void fireListenerEvent(StateUpdateListener listener, StateOptions info, StateObject stateObject) {
        try {
            listener.onUpdate(info, stateObject);
        } catch (Throwable t) {
            logger.error("Error while proccess listener {}: {}", listener, t);
        }
    }

    private boolean collectionExists(String name) {
        final MongoIterable<String> iterable = database.listCollectionNames();
        try (final MongoCursor<String> it = iterable.iterator()) {
            while (it.hasNext()) {
                if (it.next().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    private class InternalState implements State {
        private StateOptions stateOptions;
        private StateObject lastState;
        private MongoCollection<Document> collection;

        public StateOptions getStateOptions() {
            return stateOptions;
        }

        @Override
        public void push(StateObject stateObject) {
            firePushEvent(stateOptions, stateObject);

            if (this.stateOptions.getHistorySize() > 0 && this.collection != null) {
                var document = new Document();
                stateObject.save(document);
                this.collection.insertOne(document);
                this.lastState = stateObject;
                logger.debug("State object saved {}", document);
            }
        }

        private List<StateObject> load() {
            if (this.stateOptions.getHistoryStrategy() == HistoryStrategy.DISABLED
                    || this.stateOptions.getHistorySize() <= 0
                    || this.collection == null) {
                return Collections.emptyList();
            }

            var documents = collection.find().sort(Sorts.descending("_id")).limit(stateOptions.getHistorySize());

            if (documents == null) return Collections.emptyList();

            var list = new ArrayList<StateObject>();
            for (Document document : documents) {
                StateObject stateObject = stateOptions.createStateObject();
                stateObject.load(document);

                list.add(stateObject);
            }

            return list;
        }

        @Override
        public void push(Map<String, Object> stateObject) {
            push(new MapStateObject(stateObject));
        }

    }
}
