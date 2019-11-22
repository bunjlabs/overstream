package com.overstreamapp.twitchbot;

import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

import java.util.List;

@Settings("twitchbot")
public interface TwitchBotSettings {
    @SettingName("channels")
    List<String> channels();

}
