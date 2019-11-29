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

import com.bunjlabs.fuga.context.ApplicationContext;
import com.bunjlabs.fuga.context.ApplicationEventManager;
import com.bunjlabs.fuga.context.ApplicationListener;
import com.bunjlabs.fuga.context.events.ContextClosedEvent;
import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.Injector;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.groovy.GroovyScripts;
import com.overstreamapp.groovy.GroovyUnit;
import com.overstreamapp.messageserver.MessageServer;
import com.overstreamapp.messageserver.MessageServerUnit;
import com.overstreamapp.mpd.YmpdClient;
import com.overstreamapp.mpd.YmpdClientUnit;
import com.overstreamapp.network.EventLoopGroupManagerUnit;
import com.overstreamapp.obs.ObsClient;
import com.overstreamapp.obs.ObsUnit;
import com.overstreamapp.osc.NettyOscClientUnit;
import com.overstreamapp.statemanager.StateManagerUnit;
import com.overstreamapp.streamlabs.StreamlabsClient;
import com.overstreamapp.streamlabs.StreamlabsUnit;
import com.overstreamapp.twitch.TwitchMi;
import com.overstreamapp.twitch.TwitchMiUnit;
import com.overstreamapp.twitchbot.TwitchBot;
import com.overstreamapp.twitchbot.TwitchBotUnit;
import com.overstreamapp.websocket.client.netty.NettyWebSocketClientUnit;
import com.overstreamapp.websocket.server.NettyWebSocketServerUnit;
import com.overstreamapp.x32mixer.X32MixerClient;
import com.overstreamapp.x32mixer.X32MixerClientUnit;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.function.Consumer;

public class OverStreamApp {

    private final Logger logger;
    private final AppSettings settings;
    private final Injector baseInjector;
    private Injector appInjector;

    @Inject
    public OverStreamApp(Logger logger, AppSettings settings, ApplicationContext context) {
        this.logger = logger;
        this.settings = settings;
        this.baseInjector = context.getInjector();
    }

    public Injector getInjector() {
        return appInjector;
    }

    void start() {
        logger.info("Starting up OverStream");

        var modules = new ArrayList<AppModule>();
        modules.add(new AppModule(settings.modulesEnabled().messageServer(), new MessageServerUnit(), (injector -> {
            injector.getInstance(MessageServer.class).start();
        })));
        modules.add(new AppModule(settings.modulesEnabled().x32MixerClient(), new X32MixerClientUnit(), (injector -> {
            injector.getInstance(X32MixerClient.class).connect();
        })));
        modules.add(new AppModule(settings.modulesEnabled().mpdClient(), new YmpdClientUnit(), (injector -> {
            injector.getInstance(YmpdClient.class).connect();
        })));
        modules.add(new AppModule(settings.modulesEnabled().twitchMiClient(), new TwitchMiUnit(), (injector -> {
            injector.getInstance(TwitchMi.class).connect();
        })));
        modules.add(new AppModule(settings.modulesEnabled().twitchBot(), new TwitchBotUnit(), (injector -> {
            injector.getInstance(TwitchBot.class).start();
        })));
        modules.add(new AppModule(settings.modulesEnabled().streamlabsSocket(), new StreamlabsUnit(), (injector -> {
            injector.getInstance(StreamlabsClient.class).connect();
        })));
        modules.add(new AppModule(settings.modulesEnabled().obsClient(), new ObsUnit(), (injector -> {
            injector.getInstance(ObsClient.class).connect();
        })));
        modules.add(new AppModule(settings.modulesEnabled().groovy(), new GroovyUnit(), (injector -> {
            injector.getInstance(GroovyScripts.class).start();
        })));

        var units = new ArrayList<Unit>();
        units.add(this::configure);
        modules.forEach(m -> {
            if (m.enabled) units.add(m.unit);
        });

        this.appInjector  = baseInjector.createChildInjector(units);

        modules.forEach(m -> {
            if (m.enabled) m.initializer.accept(appInjector);
        });

        var eventManager = appInjector.getInstance(ApplicationEventManager.class);
        eventManager.addEventListener(new ApplicationListener<ContextClosedEvent>() {
            @Override
            public void onApplicationEvent(ContextClosedEvent  contextClosedEvent) {
                logger.info("Shutdown application...");
            }
        });

        logger.info("Started");
    }

    private void configure(Configuration c) {
        c.install(new StateManagerUnit());
        c.install(new EventLoopGroupManagerUnit());
        c.install(new NettyWebSocketServerUnit());
        c.install(new NettyWebSocketClientUnit());
        c.install(new NettyOscClientUnit());
        c.bind(OverStreamApp.class).toInstance(this);
    }

    private static class AppModule {
        final boolean enabled;
        final Unit unit;
        final Consumer<Injector> initializer;

        public AppModule(boolean enabled, Unit unit, Consumer<Injector> initializer) {
            this.enabled = enabled;
            this.unit = unit;
            this.initializer = initializer;
        }
    }
}
