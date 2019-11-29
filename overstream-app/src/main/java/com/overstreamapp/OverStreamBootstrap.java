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
