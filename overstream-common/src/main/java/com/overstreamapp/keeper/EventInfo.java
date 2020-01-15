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
import java.util.function.Supplier;

public class EventInfo<T extends EventObject> {

    private final String name;
    private transient final Class<T> type;
    private transient final Supplier<T> supplier;

    public EventInfo(Class<T> type, Supplier<T> supplier) {
        this.name = type.getSimpleName();
        this.type = type;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
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
        var stateInfo = (EventInfo) o;
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
                .toString();
    }

}
