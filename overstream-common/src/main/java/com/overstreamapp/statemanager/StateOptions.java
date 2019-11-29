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

import com.bunjlabs.fuga.util.Assert;
import com.bunjlabs.fuga.util.ObjectUtils;

import java.util.Objects;
import java.util.function.Supplier;

public class StateOptions {

    private final String name;
    private final StateType type;
    private final HistoryStrategy historyStrategy;
    private final int historySize;
    private transient final Supplier<StateObject> objectSupplier;

    public StateOptions(String name, StateType type, Supplier<StateObject> objectSupplier) {
        this.name = name;
        this.type = type;
        this.historyStrategy = HistoryStrategy.DISABLED;
        this.historySize = 0;
        this.objectSupplier = objectSupplier;
    }

    public StateOptions(String name, StateType type, int historySize, Supplier<StateObject> objectSupplier) {
        this.type = type;
        Assert.isTrue(historySize > 0);
        this.name = name;
        this.historyStrategy = HistoryStrategy.CAPPED;
        this.historySize = historySize;
        this.objectSupplier = objectSupplier;
    }

    public StateOptions(String name, StateType type, HistoryStrategy historyStrategy, int historySize, Supplier<StateObject> objectSupplier) {
        this.type = type;
        if(historyStrategy != HistoryStrategy.DISABLED) Assert.isTrue(historySize > 0);
        this.name = name;
        this.historyStrategy = historyStrategy;
        this.historySize = historySize;
        this.objectSupplier = objectSupplier;
    }

    public String getName() {
        return name;
    }

    public HistoryStrategy getHistoryStrategy() {
        return historyStrategy;
    }

    public int getHistorySize() {
        return historySize;
    }

    public StateObject createStateObject() {
        return objectSupplier.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateOptions stateOptions = (StateOptions) o;
        return Objects.equals(name, stateOptions.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return ObjectUtils.toStringJoiner(StateOptions.class)
                .add("name", name)
                .add("historyStrategy", historyStrategy)
                .add("historySize", historySize)
                .toString();
    }

}
