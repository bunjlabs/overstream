package com.overstreamapp.statemanager;

import org.bson.Document;

public interface StateObject {

    void save(Document document);

    void load(Document document);
}
