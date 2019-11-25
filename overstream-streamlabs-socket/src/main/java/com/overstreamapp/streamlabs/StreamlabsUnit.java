package com.overstreamapp.streamlabs;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;

public class StreamlabsUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(StreamlabsSettings.class).auto();
        c.bind(StreamlabsClient.class).auto();
    }
}
