package com.overstreamapp.mpd;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("ympd")
public interface YmpdClientSettings {

    @SettingName("server-uri")
    String serverUri();

    @SettingName("history-size")
    @SettingDefault("20")
    int historySize();
}
