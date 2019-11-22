package com.overstreamapp.statemanager;


import java.util.Map;

public interface State {

    void push(StateObject stateObject);

    void push(Map<String, Object> stateObject);
}
