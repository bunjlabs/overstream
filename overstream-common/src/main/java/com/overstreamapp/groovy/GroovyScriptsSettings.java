package com.overstreamapp.groovy;

import com.bunjlabs.fuga.settings.SettingName;
import com.bunjlabs.fuga.settings.Settings;

import java.util.List;

@Settings("groovy-scripts")
public interface GroovyScriptsSettings {

    @SettingName("run")
    List<String> run();
}
