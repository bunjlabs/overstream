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
