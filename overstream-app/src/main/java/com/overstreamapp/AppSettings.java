package com.overstreamapp;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("modules")
public interface AppSettings {

    @SettingName("message-server")
    @SettingDefault("false")
    boolean messageServer();


    @SettingName("x32mixer-client")
    @SettingDefault("false")
    boolean x32MixerClient();
}
