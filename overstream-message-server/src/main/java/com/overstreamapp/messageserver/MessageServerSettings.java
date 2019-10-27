package com.overstreamapp.messageserver;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("message-server")
public interface MessageServerSettings {

    @SettingName("bind-host")
    @SettingDefault("localhost")
    String bindHost();

    @SettingName("bind-port")
    @SettingDefault("8888")
    int bindPort();
}
