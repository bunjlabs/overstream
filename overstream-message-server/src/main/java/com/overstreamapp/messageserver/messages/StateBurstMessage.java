package com.overstreamapp.messageserver.messages;


import com.overstreamapp.statemanager.StateOptions;
import com.overstreamapp.statemanager.StateObject;

public class StateBurstMessage extends Message {
    private final String name;
    private final StateObject state;

    public StateBurstMessage(StateOptions info, StateObject stateObject) {
        super("StateBurst");
        this.name = info.getName();
        this.state = stateObject;
    }
}
