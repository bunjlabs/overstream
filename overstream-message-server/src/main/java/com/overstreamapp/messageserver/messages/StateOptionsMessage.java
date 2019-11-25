package com.overstreamapp.messageserver.messages;

import com.overstreamapp.statemanager.StateOptions;

public class StateOptionsMessage extends Message {
    private final StateOptions options;

    public StateOptionsMessage(StateOptions options) {
        super("StateOptions");

        this.options = options;
    }
}
