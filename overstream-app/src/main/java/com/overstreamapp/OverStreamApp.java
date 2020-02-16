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

import com.bunjlabs.fuga.context.ApplicationEventManager;
import com.bunjlabs.fuga.context.ApplicationListener;
import com.bunjlabs.fuga.context.events.ContextClosedEvent;
import com.bunjlabs.fuga.inject.Inject;
import com.bunjlabs.fuga.inject.InjectAll;
import com.bunjlabs.fuga.inject.Injector;
import com.bunjlabs.fuga.inject.Unit;
import com.mongodb.client.MongoDatabase;
import com.overstreamapp.event.EventKeeper;
import com.overstreamapp.groovy.GroovyRuntime;
import com.overstreamapp.groovy.GroovyScripts;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.network.NetworkCommands;
import com.overstreamapp.shell.CommandRegistry;
import com.overstreamapp.store.StoreKeeper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Set;

public class OverStreamApp {

    private final Logger logger;
    private final Set<AppModule> modules;
    private final Injector bootstrapInjector;
    private final GroovyRuntime groovyRuntime;
    private final NetworkCommands networkCommands;
    private final ApplicationEventManager eventManager;
    private Injector appInjector;

    @Inject
    public OverStreamApp(
            Logger logger,
            Injector bootstrapInjector,
            @InjectAll Set<AppModule> modules,
            GroovyRuntime groovyRuntime,
            NetworkCommands networkCommands,
            ApplicationEventManager eventManager) {
        this.logger = logger;
        this.modules = modules;
        this.bootstrapInjector = bootstrapInjector;
        this.groovyRuntime = groovyRuntime;
        this.networkCommands = networkCommands;
        this.eventManager = eventManager;
    }

    public Injector getInjector() {
        return appInjector;
    }

    void start() {
        logger.info("Starting up OverStream");

        var appUnits = new ArrayList<Unit>();
        modules.forEach(module -> appUnits.addAll(module.getUnits()));
        appInjector = bootstrapInjector.createChildInjector(appUnits);
        modules.forEach(module -> module.init(appInjector));

        groovyRuntime.export("EventKeeper", appInjector.getInstance(EventKeeper.class));
        groovyRuntime.export("StoreKeeper", appInjector.getInstance(StoreKeeper.class));
        groovyRuntime.export("MongoDatabase", appInjector.getInstance(MongoDatabase.class));
        groovyRuntime.export("Commands", appInjector.getInstance(CommandRegistry.class));

        networkCommands.registerCommands();

        eventManager.addEventListener((ApplicationListener<ContextClosedEvent>) contextClosedEvent -> {
            logger.info("Shutdown application...");

            appInjector.getInstance(EventLoopGroupManager.class).shutdownGracefully();
        });

        appInjector.getInstance(GroovyScripts.class).start();


        /*
        var client = appInjector.getInstance(HttpClient.class);

        try {
            client.get("https://postman-echo.com/get").header("TT", 1).execute((response, cause) -> logger.debug("Response 1: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 2).execute((response, cause) -> logger.debug("Response 2: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 3).execute((response, cause) -> logger.debug("Response 3: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 4).execute((response, cause) -> logger.debug("Response 4: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 5).execute((response, cause) -> logger.debug("Response 5: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 6).execute((response, cause) -> logger.debug("Response 6: {}", response.content().toString(StandardCharsets.UTF_8)));
            client.get("https://postman-echo.com/get").header("TT", 7).execute((response, cause) -> logger.debug("Response 7: {}", response.content().toString(StandardCharsets.UTF_8)));
        } catch (MalformedURLException e) {
            logger.error("URL error", e);
        }*/


        logger.info("Started");
    }
}
