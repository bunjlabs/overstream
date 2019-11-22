package com.overstreamapp.statemanager;

public interface StateUpdateListener {

    void onUpdate(StateInfo info, StateObject stateObject);
}
