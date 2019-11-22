package com.overstreamapp.messageserver.messages;

import com.overstreamapp.statemanager.StateInfo;
import com.overstreamapp.statemanager.StateObject;

public class StateUpdateMessage extends Message {

    private final StateInfo state;
    private final StateObject data;

    public StateUpdateMessage(StateInfo state, StateObject data) {
        this.state = state;
        this.data = data;
    }

    @Override
    public String typeName() {
        return "State";
    }
}
