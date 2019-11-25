package com.overstreamapp.obs;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("obs")
public interface ObsSettings {

    @SettingName("server-uri")
    @SettingDefault("ws://localhost:4444")
    String serverUri();
}
