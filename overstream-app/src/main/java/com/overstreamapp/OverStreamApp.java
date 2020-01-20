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
import com.mongodb.client.MongoDatabase;
import com.overstreamapp.commands.CommandRegistry;
import com.overstreamapp.commands.CommandRegistryUnit;
import com.overstreamapp.commandserver.CommandServerAppModule;
import com.overstreamapp.common.CommonUnit;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.event.EventKeeperUnit;
import com.overstreamapp.groovy.GroovyRuntime;
import com.overstreamapp.groovy.GroovyScripts;
import com.overstreamapp.groovy.GroovyUnit;
import com.overstreamapp.http.HttpClientUnit;
import com.overstreamapp.messageserver.MessageServerAppModule;
import com.overstreamapp.mongodb.MongoUnit;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.network.EventLoopGroupManagerUnit;
import com.overstreamapp.obs.ObsAppModule;
import com.overstreamapp.osc.NettyOscClientUnit;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.store.StoreKeeperUnit;
import com.overstreamapp.streamlabs.StreamlabsAppModule;
import com.overstreamapp.twitchbot.TwitchBotAppModule;
import com.overstreamapp.twitchpubsub.TwitchPubSubAppModule;
import com.overstreamapp.websocket.client.netty.NettyWebSocketClientUnit;
import com.overstreamapp.websocket.server.NettyWebSocketServerUnit;
import com.overstreamapp.x32mixer.X32MixerAppModule;
import com.overstreamapp.ympd.YmpdAppModule;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class OverStreamApp {

    private final Logger logger;
    private final AppSettings settings;
    private final Injector baseInjector;
    private final ApplicationContext context;
    private final List<Class<? extends AppModule>> appModules;
    private Injector appInjector;

    @Inject
    public OverStreamApp(Logger logger, AppSettings settings, ApplicationContext context) {
        this.logger = logger;
        this.settings = settings;
        this.baseInjector = context.getInjector();
        this.context = context;

        this.appModules = List.of(
                MessageServerAppModule.class,
                CommandServerAppModule.class,
                ObsAppModule.class,
                StreamlabsAppModule.class,
                TwitchBotAppModule.class,
                TwitchPubSubAppModule.class,
                X32MixerAppModule.class,
                YmpdAppModule.class
        );
    }

    public Injector getInjector() {
        return appInjector;
    }

    void start() {
        logger.info("Starting up OverStream");

        var baseUnits = new ArrayList<Unit>();
        baseUnits.add(this::configureCommons);
        baseUnits.add(this::configureProto);
        baseUnits.add(this::configureModules);
        baseUnits.add(this::configureApp);

        var commonInjector = baseInjector.createChildInjector(baseUnits);

        var appUnits = new ArrayList<Unit>();
        this.appModules.forEach(module -> appUnits.addAll(commonInjector.getInstance(module).getUnits()));
        this.appInjector = commonInjector.createChildInjector(appUnits);
        this.appModules.forEach(module -> appInjector.getInstance(module).init(appInjector));

        var groovy = appInjector.getInstance(GroovyRuntime.class);
        groovy.export("EventKeeper", appInjector.getInstance(EventKeeper.class));
        groovy.export("StoreKeeper", appInjector.getInstance(StoreKeeper.class));
        groovy.export("MongoDatabase", appInjector.getInstance(MongoDatabase.class));
        groovy.export("Commands", appInjector.getInstance(CommandRegistry.class));

        var eventManager = appInjector.getInstance(ApplicationEventManager.class);
        eventManager.addEventListener((ApplicationListener<ContextClosedEvent>) contextClosedEvent -> {
            logger.info("Shutdown application...");

            appInjector.getInstance(EventLoopGroupManager.class).shutdownGracefully();
        });

        appInjector.getInstance(GroovyScripts.class).start();

        /*
        var client = appInjector.getInstance(HttpClient.class);

        try {
            client.get("http://google.com/").execute();
        } catch (MalformedURLException e) {
            logger.error("URL error", e);
        }

        */

        logger.info("Started");
    }

    private void configureCommons(Configuration c) {
        c.install(new CommonUnit());
        c.install(new MongoUnit());
        c.install(new StoreKeeperUnit());
        c.install(new EventKeeperUnit());
        c.install(new GroovyUnit());
        c.install(new CommandRegistryUnit());
        c.install(new EventLoopGroupManagerUnit());
    }

    private void configureProto(Configuration c) {
        c.install(new HttpClientUnit());
        c.install(new NettyWebSocketServerUnit());
        c.install(new NettyWebSocketClientUnit());
        c.install(new NettyOscClientUnit());
    }

    private void configureModules(Configuration c) {
        this.appModules.forEach(module -> c.bind(module).auto());
    }

    private void configureApp(Configuration c) {
        c.bind(OverStreamApp.class).toInstance(this);
    }
}
