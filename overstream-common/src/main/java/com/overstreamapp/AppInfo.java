package com.overstreamapp;

import com.bunjlabs.fuga.settings.Settings;

@Settings("___app")
public interface AppInfo {

    String name();

    String version();
}

