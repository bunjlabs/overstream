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
import com.overstreamapp.commandserver.CommandServerAppModule;
import com.overstreamapp.common.CommonUnit;
import com.overstreamapp.event.EventKeeperUnit;
import com.overstreamapp.groovy.GroovyUnit;
import com.overstreamapp.http.HttpClientUnit;
import com.overstreamapp.messageserver.MessageServerAppModule;
import com.overstreamapp.mongodb.MongoUnit;
import com.overstreamapp.network.NetworkSupportUnit;
import com.overstreamapp.obs.ObsAppModule;
import com.overstreamapp.osc.NettyOscClientUnit;
import com.overstreamapp.shell.CommandRegistryUnit;
import com.overstreamapp.store.StoreKeeperUnit;
import com.overstreamapp.streamlabs.StreamlabsAppModule;
import com.overstreamapp.twitchmi.TwitchMiAppModule;
import com.overstreamapp.twitchpubsub.TwitchPubSubAppModule;
import com.overstreamapp.websocket.client.netty.NettyWebSocketClientUnit;
import com.overstreamapp.websocket.server.NettyWebSocketServerUnit;
import com.overstreamapp.x32mixer.X32MixerAppModule;
import com.overstreamapp.ympd.YmpdAppModule;

import java.util.List;

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
        c.install(this::configureCommons);
        c.install(this::configureProto);
        c.install(this::configureModules);
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

    private void configureCommons(Configuration c) {
        c.install(new CommonUnit());
        c.install(new MongoUnit());
        c.install(new StoreKeeperUnit());
        c.install(new EventKeeperUnit());
        c.install(new GroovyUnit());
        c.install(new CommandRegistryUnit());
        c.install(new NetworkSupportUnit());
    }

    private void configureProto(Configuration c) {
        c.install(new HttpClientUnit());
        c.install(new NettyWebSocketServerUnit());
        c.install(new NettyWebSocketClientUnit());
        c.install(new NettyOscClientUnit());
    }

    private void configureModules(Configuration c) {
        var modules = List.of(
                MessageServerAppModule.class,
                CommandServerAppModule.class,
                ObsAppModule.class,
                StreamlabsAppModule.class,
                TwitchMiAppModule.class,
                TwitchPubSubAppModule.class,
                X32MixerAppModule.class,
                YmpdAppModule.class
        );

        modules.forEach(module -> {
            c.bind(module).auto();
            c.bind(AppModule.class).to(module);
        });
    }
}
