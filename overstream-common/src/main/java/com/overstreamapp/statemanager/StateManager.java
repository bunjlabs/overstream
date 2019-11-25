package com.overstreamapp.statemanager;

import java.util.List;

public interface StateManager {

    State createState(StateOptions info);

    State getState(String channel);

    StateObject getLastStateValue(String channel);

    void subscribe(String channel, StateUpdateListener listener);

    void subscribeAll(StateUpdateListener listener);

    List<StateOptions> getAllStateOption();

    void pushAll(StateUpdateListener listener);
}
