package com.overstreamapp;

import com.bunjlabs.fuga.settings.Settings;

@Settings("app")
public interface AppInfo {

    String name();

    String version();
}

