package com.overstreamapp.statemanager;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class MapStateObject implements StateObject {

    private final Map<String, Object> map;

    MapStateObject() {
        this.map = new HashMap<>();
    }

    MapStateObject(Map<String, Object> map) {
        this();
        this.map.putAll(map);
    }

    @Override
    public void save(Document document) {
        document.putAll(map);
    }

    @Override
    public void load(Document document) {
        map.putAll(document);
    }
}
