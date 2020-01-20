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

import java.util.Collections;
import java.util.List;

class LocalStore<T> extends AbstractStore<T> {

    LocalStore(DefaultStoreKeeper storeKeeper, Class<T> type, Reducer<T> rootReducer, T initialState) {
        super(storeKeeper, type, rootReducer, initialState);
    }

    @Override
    public void persist(T state) {
        // no persistence
    }

    @Override
    public List<T> findAll() {
        return Collections.emptyList();
    }

    @Override
    public T findLast() {
        return null;
    }
}
