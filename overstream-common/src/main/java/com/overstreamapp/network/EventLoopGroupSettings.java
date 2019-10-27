package com.overstreamapp.network;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("threads")
public interface EventLoopGroupSettings {

    @SettingDefault("nio")
    String type();

    @SettingName("boss-threads")
    @SettingDefault("1")
    int bossThreads();

    @SettingName("worker-threads")
    @SettingDefault("0")
    int workerThreads();
}
