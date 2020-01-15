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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.overstreamapp.keeper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class InternalState<T extends StateObject> implements State<T> {
    private final DefaultKeeper keeper;
    private final StateInfo<T> info;

    private T lastState;
    private MongoCollection<T> collection;

    InternalState(DefaultKeeper keeper, StateInfo<T> info) {
        this.keeper = keeper;
        this.info = info;
    }

    public StateInfo<T> getInfo() {
        return info;
    }

    void setCollection(MongoCollection<T> collection) {
        this.collection = collection;
    }

    @Override
    public void push(T stateObject) {
        keeper.fireStateChanged(info, stateObject);

        var ps = this.info.getPersistenceStrategy();

        if (ps == StatePersistenceStrategy.LIST_CAPPED || ps == StatePersistenceStrategy.LIST_LIMITED) {
            if (this.collection != null && this.info.getSize() > 0) {

                this.collection.insertOne(stateObject);
            }
        } else if (ps == StatePersistenceStrategy.TRANSIENT) {
            this.lastState = stateObject;
        }
    }

    List<T> load() {
        var ps = this.info.getPersistenceStrategy();

        if (ps == StatePersistenceStrategy.LIST_CAPPED || ps == StatePersistenceStrategy.LIST_LIMITED) {
            if (this.collection != null && this.info.getSize() > 0) {
                var stateObjects = collection.find().sort(Sorts.descending("_id")).limit(info.getSize());

                if (stateObjects == null) return Collections.emptyList();

                var list = new ArrayList<T>();
                for (T stateObject : stateObjects) {
                    list.add(stateObject);
                }

                return list;
            }
        } else if (ps == StatePersistenceStrategy.TRANSIENT && lastState != null) {
            return Collections.singletonList(lastState);
        }

        return Collections.emptyList();
    }
}
