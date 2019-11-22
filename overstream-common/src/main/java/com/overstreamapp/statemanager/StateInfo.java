package com.overstreamapp.statemanager;

import com.bunjlabs.fuga.util.ObjectUtils;

import java.util.Objects;

public class StateInfo {

    private final String name;
    private final int historySize;

    public StateInfo(String name, int historySize) {
        this.name = name;
        this.historySize = historySize;
    }

    public String getName() {
        return name;
    }

    public int getHistorySize() {
        return historySize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateInfo stateInfo = (StateInfo) o;
        return Objects.equals(name, stateInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return ObjectUtils.toStringJoiner(StateInfo.class)
                .add("name", name)
                .add("maxSize", historySize)
                .toString();
    }
}
