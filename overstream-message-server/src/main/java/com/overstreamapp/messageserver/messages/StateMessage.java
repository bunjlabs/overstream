package com.overstreamapp.messageserver.messages;


import com.overstreamapp.statemanager.StateOptions;
import com.overstreamapp.statemanager.StateObject;

public class StateMessage extends Message {
    private final String name;
    private final StateObject state;

    public StateMessage(StateOptions info, StateObject stateObject) {
        super("State");
        this.name = info.getName();
        this.state = stateObject;
    }
}
