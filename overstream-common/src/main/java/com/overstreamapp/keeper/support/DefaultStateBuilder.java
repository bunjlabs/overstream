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

import com.overstreamapp.keeper.*;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class DefaultStateBuilder<T extends StateObject> implements StateBuilder<T> {
    private final DefaultKeeper keeper;
    private final Class<T> type;

    private StatePersistenceStrategy persistenceStrategy = StatePersistenceStrategy.DISABLED;
    private int size = 0;

    DefaultStateBuilder(DefaultKeeper keeper, Class<T> type) {
        this.keeper = keeper;
        this.type = type;
    }

    @Override
    public StateBuilder<T> persistence(StatePersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
        return this;
    }

    @Override
    public StateBuilder<T> persistenceDisabled() {
        this.persistenceStrategy = StatePersistenceStrategy.DISABLED;
        return this;
    }

    @Override
    public StateBuilder<T> persistenceTransient() {
        this.persistenceStrategy = StatePersistenceStrategy.TRANSIENT;
        return this;
    }

    @Override
    public StateBuilder<T> persistenceListLimited() {
        this.persistenceStrategy = StatePersistenceStrategy.LIST_LIMITED;
        return this;
    }

    @Override
    public StateBuilder<T> persistenceListCapped() {
        this.persistenceStrategy = StatePersistenceStrategy.LIST_CAPPED;
        return this;
    }

    @Override
    public StateBuilder<T> history(int size) {
        this.size = size;
        return this;
    }


    @Override
    public State<T> build() {
        Constructor<T> constructor;

        try {
            constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to build state object", e);
        }

        Supplier<T> supplier = () -> {
            try {
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to create state object instance", e);
            }
        };

        var info = new StateInfo<T>(type, supplier, persistenceStrategy, size);
        return keeper.createState(info);
    }
}
