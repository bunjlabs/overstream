package com.overstreamapp.statemanager;

import org.bson.Document;

public abstract class EventObject implements StateObject {
    @Override
    public void save(Document document) {

    }

    @Override
    public void load(Document document) {

    }
}
