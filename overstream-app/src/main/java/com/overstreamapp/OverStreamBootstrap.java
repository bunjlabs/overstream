package com.overstreamapp;

import com.bunjlabs.fuga.context.FugaBoot;
import com.bunjlabs.fuga.environment.Environment;
import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.logging.LoggingUnitBuilder;
import com.bunjlabs.fuga.settings.SettingsUnitBuilder;
import com.bunjlabs.fuga.settings.source.ClassPathSettingsSource;
import com.bunjlabs.fuga.settings.source.LocalFilesSettingsSource;

public class OverStreamBootstrap {

    public static void main(String[] args) {
        new OverStreamBootstrap().start();
    }

    private void start() {
        var context = FugaBoot.start(this::configure);
        var app = context.getInjector().getInstance(OverStreamApp.class);

        app.start();
    }

    private void configure(Configuration c) {
        c.install(this::configureLogging);
        c.install(this::configureSettings);
        c.bind(OverStreamApp.class).auto();

    }

    private void configureLogging(Configuration c) {
        c.install(new LoggingUnitBuilder().build());
    }

    private void configureSettings(Configuration c) {
        c.install(new SettingsUnitBuilder()
                .withEnvironment(Environment.DEFAULT)
                .withSettingsSources(
                        new ClassPathSettingsSource(OverStreamApp.class.getClassLoader(), "version.yaml"),
                        new LocalFilesSettingsSource(".", "settings.yaml"))
                .build());

        c.bind(AppInfo.class).auto();
        c.bind(AppSettings.class).auto();
    }
}
