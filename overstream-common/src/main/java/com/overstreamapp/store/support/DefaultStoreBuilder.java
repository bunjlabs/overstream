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

import com.overstreamapp.store.Reducer;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreBuilder;
import com.overstreamapp.store.ValueReducer;

class DefaultStoreBuilder<T> implements StoreBuilder<T> {

    private final DefaultStoreKeeper storeKeeper;
    private final Class<T> type;
    private T initialState = null;
    private Reducer<T> reducer;
    private int persistenceSize;

    DefaultStoreBuilder(DefaultStoreKeeper storeKeeper, Class<T> type) {
        this.storeKeeper = storeKeeper;
        this.type = type;
        this.reducer = new ValueReducer<>(type);
    }

    @Override
    public StoreBuilder<T> withInitial(T initialState) {
        this.initialState = initialState;
        return this;
    }

    @Override
    public StoreBuilder<T> withReducer(Reducer<T> reducer) {
        this.reducer = reducer;
        return this;
    }

    @Override
    public StoreBuilder<T> persistence(int persistenceSize) {
        this.persistenceSize = persistenceSize;
        return this;
    }

    @Override
    public Store<T> build() {
        AbstractStore<T> store;

        if (persistenceSize > 0) {
            var persistenceCollection = storeKeeper.createPersistenceCollection(type, persistenceSize);
            store = new PersistedStore<>(storeKeeper, type, reducer, initialState, persistenceCollection);
        } else {
            store = new LocalStore<>(storeKeeper, type, reducer, initialState);
        }

        store.initialize();

        storeKeeper.registerStore(store);
        return store;
    }
}
