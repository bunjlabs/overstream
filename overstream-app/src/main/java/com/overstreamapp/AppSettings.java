package com.overstreamapp;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

import java.util.List;

@Settings("application")
public interface AppSettings {

    @SettingName("modules-enabled")
    ModulesEnabledSettings modulesEnabled();

    interface ModulesEnabledSettings {
        @SettingName("groovy")
        @SettingDefault("false")
        boolean groovy();

        @SettingName("message-server")
        @SettingDefault("false")
        boolean messageServer();

        @SettingName("x32mixer-client")
        @SettingDefault("false")
        boolean x32MixerClient();

        @SettingName("ympd-client")
        @SettingDefault("false")
        boolean mpdClient();

        @SettingName("twitchmi-client")
        @SettingDefault("false")
        boolean twitchMiClient();

        @SettingName("twitch-bot")
        @SettingDefault("false")
        boolean twitchBot();

        @SettingName("streamlabs-socket")
        @SettingDefault("false")
        boolean streamlabsSocket();

        @SettingName("obs-client")
        @SettingDefault("false")
        boolean obsClient();

    }
}
