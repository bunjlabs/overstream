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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.overstreamapp.store.Reducer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersistedStore<T> extends AbstractStore<T> {
    private final MongoCollection<T> collection;

    PersistedStore(DefaultStoreKeeper storeKeeper, Class<T> type, Reducer<T> rootReducer, T initialState, MongoCollection<T> collection) {
        super(storeKeeper, type, rootReducer, initialState);
        this.collection = collection;
    }

    @Override
    public void persist(T state) {
        logger.trace("Persist state: {}", state);
        collection.insertOne(state);
    }

    @Override
    public List<T> findAll() {
        logger.trace("Finding all state");

        var stateList = collection.find().sort(Sorts.ascending("_id"));

        if (stateList == null) return Collections.emptyList();

        var list = new ArrayList<T>();
        for (T state : stateList) {
            list.add(state);
        }

        return list;
    }

    @Override
    public T findLast() {
        logger.trace("Finding last state");

        return collection.find().sort(Sorts.descending("_id")).first();
    }
}
