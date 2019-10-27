package com.overstreamapp.x32mixer;

import com.bunjlabs.fuga.settings.SettingDefault;
import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

@Settings("x32mixer")
public interface X32MixerSettings {

    static float s = 1E-3f;

    @SettingName("host")
    @SettingDefault("localhost")
    String host();


    @SettingName("meter-sensitivity")
    @SettingDefault("0.001")
    float meterSensitivity();
}
