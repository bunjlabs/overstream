/*
 * Copyright 2019 Bunjlabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.overstreamapp.twitchmi;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

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

    @SettingName("connect-timeout")
    @SettingDefault("4000")
    int connectTimeout();

    @SettingName("reconnect-delay")
    @SettingDefault("2000")
    int reconnectDelay();
}
