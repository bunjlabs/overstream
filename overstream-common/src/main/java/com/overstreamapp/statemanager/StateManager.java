package com.overstreamapp.statemanager;

public interface StateManager {

    State getState(StateInfo info);

    void subscribe(String channel, StateUpdateListener listener);

    void subscribeAll(StateUpdateListener listener);

    void burst(StateUpdateListener listener);
}
