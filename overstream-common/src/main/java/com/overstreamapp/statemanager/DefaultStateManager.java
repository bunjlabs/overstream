package com.overstreamapp.statemanager;

import com.bunjlabs.fuga.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultStateManager implements StateManager {
    private final Logger logger;
    private final Map<StateInfo, InternalState> stateMap;
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
    public State getState(StateInfo info) {
        InternalState state = stateMap.get(info);

        if (state != null) {
            logger.debug("Return existing state");
            return state;
        }

        logger.debug("Create new state");
        state = new InternalState();
        state.stateInfo = info;

        if (info.getHistorySize() > 0) {
            if (!collectionExists(info.getName())) {
                CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions().capped(true).sizeInBytes(1024 * 1024 * 64).maxDocuments(info.getHistorySize());
                database.createCollection(info.getName(), createCollectionOptions);
            }
            state.collection = database.getCollection(info.getName());
        }

        stateMap.put(info, state);

        return state;
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

    public void burst(StateUpdateListener listener) {
        logger.debug("Burst state");

        stateMap.forEach((info, state) -> {
            if(state.stateObject != null) {
                listener.onUpdate(info, state.stateObject);
            }
        });
    }

    private void firePushEvent(StateInfo info, StateObject stateObject) {
        logger.debug("Fire push event {}", info);

        listenersList.forEach(l -> fireListenerEvent(l, info, stateObject));

        var listeners = listenersMap.get(info.getName());
        if (listeners != null) {
            listeners.forEach(l -> fireListenerEvent(l, info, stateObject));
        }
    }

    private void fireListenerEvent(StateUpdateListener listener, StateInfo info, StateObject stateObject) {
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
        private StateInfo stateInfo;
        private StateObject stateObject;
        private MongoCollection<Document> collection;

        @Override
        public void push(StateObject stateObject) {
            firePushEvent(stateInfo, stateObject);

            if (this.stateInfo.getHistorySize() > 0 && this.collection != null) {
                Document document = new Document();
                stateObject.save(document);
                this.collection.insertOne(document);
                this.stateObject = stateObject;
                logger.debug("State object saved {}", document);
            }
        }

        @Override
        public void push(Map<String, Object> stateObject) {
            push(new MapStateObject(stateObject));
        }

    }
}
