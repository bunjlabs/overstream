package com.overstreamapp;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

import java.util.List;

@Settings("application")
public interface AppSettings {

    @SettingName("scripts")
    List<String> scripts();

    @SettingName("message-server-enabled")
    @SettingDefault("false")
    boolean messageServer();


    @SettingName("x32mixer-client-enabled")
    @SettingDefault("false")
    boolean x32MixerClient();

    @SettingName("mpd-client-enabled")
    @SettingDefault("false")
    boolean mpdClient();

    @SettingName("twitchmi-client-enabled")
    @SettingDefault("false")
    boolean twitchMiClient();

    @SettingName("twitch-bot-enabled")
    @SettingDefault("false")
    boolean twitchBot();
}
