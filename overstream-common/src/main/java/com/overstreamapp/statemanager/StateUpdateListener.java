package com.overstreamapp.statemanager;

public interface StateUpdateListener {

    void onUpdate(StateOptions info, StateObject stateObject);
}
