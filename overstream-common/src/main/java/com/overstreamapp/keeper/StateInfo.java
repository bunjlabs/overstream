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

package com.overstreamapp.keeper;

import com.bunjlabs.fuga.util.ObjectUtils;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StateInfo<T extends StateObject> {

    private final String name;
    private final StatePersistenceStrategy persistenceStrategy;
    private final int size;
    private transient final Class<T> type;
    private transient final Supplier<T> supplier;

    public StateInfo(Class<T> type, Supplier<T> supplier, StatePersistenceStrategy persistenceStrategy, int size) {
        this.name = type.getSimpleName();
        this.type = type;
        this.supplier = supplier;
        this.persistenceStrategy = persistenceStrategy;
        this.size = size;
    }

    public String getName() {
        return this.name;
    }

    public StatePersistenceStrategy getPersistenceStrategy() {
        return persistenceStrategy;
    }

    public int getSize() {
        return size;
    }

    public Class<T> getType() {
        return type;
    }

    public Supplier<T> getSupplier() {
        return supplier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var stateInfo = (StateInfo) o;
        return Objects.equals(type, stateInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return ObjectUtils.toStringJoiner(StateInfo.class)
                .add("type", type)
                .add("persistenceStrategy", persistenceStrategy)
                .add("historySize", size)
                .toString();
    }

}
