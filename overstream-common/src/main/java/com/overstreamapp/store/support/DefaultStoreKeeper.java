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

import com.bunjlabs.fuga.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.CreateCollectionOptions;
import com.overstreamapp.store.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultStoreKeeper implements StoreKeeper {
    private static final String STORE_COLLECTION_PREFIX = "STORE_";

    private final Logger logger;
    private final MongoDatabase database;
    private final Map<Class<?>, AbstractStore<?>> storeMap = new ConcurrentHashMap<>();
    private final Set<StoreSubscriber<Object>> subscribers = Collections.newSetFromMap(new IdentityHashMap<>());

    @Inject
    public DefaultStoreKeeper(Logger logger, MongoDatabase database) {
        this.logger = logger;
        var codecRegistry = CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(
                        PojoCodecProvider.builder().automatic(true).build()
                )
        );

        this.database = database.withCodecRegistry(codecRegistry);
    }

    @Override
    public <T> StoreBuilder<T> storeBuilder(Class<T> type) {
        return new DefaultStoreBuilder<>(this, type);
    }

    @Override
    public <T> Store<T> getStore(Class<T> type) {
        var rawStore = storeMap.get(type);
        if (rawStore != null) {
            @SuppressWarnings("unchecked")
            var store = (Store<T>) rawStore;
            return store;
        }
        return null;
    }

    @Override
    public <T> StoreSubscription subscribe(Class<T> type, StoreSubscriber<T> subscriber) {
        var store = getStore(type);

        if (store != null) {
            return store.subscribe(subscriber);
        }

        return new StoreSubscription() {
        };
    }

    @Override
    public StoreSubscription subscribe(StoreSubscriber<Object> subscriber) {
        logger.trace("Global subscription: {}", subscriber);

        subscribers.add(subscriber);

        var subscription = new StoreSubscription() {
            @Override
            public void unsubscribe() {
                subscribers.remove(subscriber);
            }

            @Override
            public void forceUpdate() {
                storeMap.values().forEach(s -> {
                    var history = s.findAll();
                    if (history.isEmpty()) {
                        var state = s.getState();
                        if (state != null) {
                            subscriber.onChange(s.getState());
                        }
                    } else {
                        history.forEach(subscriber::onChange);
                    }
                });
            }
        };

        subscription.forceUpdate();

        return subscription;
    }

    void notifySubscribers(Object state) {
        subscribers.forEach(s -> s.onChange(state));
    }

    void registerStore(AbstractStore store) {
        storeMap.put(store.getType(), store);
    }

    Logger getLogger() {
        return logger;
    }

    <T> MongoCollection<T> createPersistenceCollection(Class<T> type, int persistenceSize) {
        var collectionName = STORE_COLLECTION_PREFIX + type.getName();
        if (!collectionExists(collectionName)) {
            var createCollectionOptions = new CreateCollectionOptions()
                    .capped(true)
                    .sizeInBytes(1024 * 4 * persistenceSize)
                    .maxDocuments(persistenceSize);
            database.createCollection(collectionName, createCollectionOptions);

            logger.debug("Mongo collection created: {}", collectionName);
        }

        return database.getCollection(collectionName, type);
    }

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
}
