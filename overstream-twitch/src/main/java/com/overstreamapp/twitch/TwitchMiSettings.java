package com.overstreamapp.twitch;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

import java.util.List;

@Settings("twitchmi")
public interface TwitchMiSettings {

    @SettingName("user-name")
    String userName();

    @SettingName("access-token")
    String accessToken();

    @SettingName("server-uri")
    @SettingDefault("wss://irc-ws.chat.twitch.tv:443")
    String serverUri();

    @SettingName("emotes-uri")
    @SettingDefault("https://static-cdn.jtvnw.net/emoticons/v1/%s/1.0")
    String emotesUri();
}
