package com.overstreamapp;

import com.bunjlabs.fuga.context.FugaBoot;
import com.bunjlabs.fuga.environment.Environment;
import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Injector;
import com.bunjlabs.fuga.inject.Unit;
import com.bunjlabs.fuga.logging.LoggingUnitBuilder;
import com.bunjlabs.fuga.settings.SettingsUnitBuilder;
import com.bunjlabs.fuga.settings.source.ClassPathSettingsSource;
import com.bunjlabs.fuga.settings.source.LocalFilesSettingsSource;
import com.overstreamapp.groovy.GroovyRuntime;
import com.overstreamapp.groovy.GroovyRuntimeUnit;
import com.overstreamapp.messageserver.MessageServerUnit;
import com.overstreamapp.mpd.YmpdClientUnit;
import com.overstreamapp.network.EventLoopGroupManagerUnit;
import com.overstreamapp.osc.NioOscClientUnit;
import com.overstreamapp.statemanager.StateManagerUnit;
import com.overstreamapp.twitch.TwitchMiUnit;
import com.overstreamapp.twitchbot.TwitchBotUnit;
import com.overstreamapp.websocket.client.netty.NettyWebSocketClientUnit;
import com.overstreamapp.websocket.server.NettyWebSocketServerUnit;
import com.overstreamapp.x32mixer.X32MixerClientUnit;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;

public class OverStreamApp {

    public static void main(String[] args) {
        new OverStreamApp().start();
    }

    private void start() {
        var context = FugaBoot.start(this::configureBase);
        var baseInjector = context.getInjector();
        var settings = baseInjector.getInstance(AppSettings.class);
        var logger = baseInjector.getInstance(Logger.class);

        logger.info("Starting up OverStream");

        var modules = new ArrayList<AppModule>();
        modules.add(new AppModule(settings.messageServer(), new MessageServerUnit()));
        modules.add(new AppModule(settings.x32MixerClient(), new X32MixerClientUnit()));
        modules.add(new AppModule(settings.mpdClient(), new YmpdClientUnit()));
        modules.add(new AppModule(settings.twitchMiClient(), new TwitchMiUnit()));
        modules.add(new AppModule(settings.twitchBot(), new TwitchBotUnit()));

        var units = new ArrayList<Unit>();
        units.add(this::configureCommon);
        modules.forEach(m -> {
            if (m.enabled) units.add(m.unit);
        });

        var commonInjector = context.getInjector().createChildInjector(units);

        var appInjector = commonInjector.createChildInjector(
                new GroovyRuntimeUnit(),
                c -> c.bind(Injector.class).toInstance(commonInjector)
        );
        var groovy = appInjector.getInstance(GroovyRuntime.class);
        for (String script : settings.scripts()) {
            try {
                groovy.getGroovyShell().evaluate(new File(script));
            } catch (Throwable e) {
                logger.error("Unable to evaluate groovy script {}", script, e);
            }
        }
    }

    private void configureCommon(Configuration c) {
        c.install(new StateManagerUnit());
        c.install(new EventLoopGroupManagerUnit());
        c.install(new NettyWebSocketServerUnit());
        c.install(new NettyWebSocketClientUnit());
        c.install(new NioOscClientUnit());
    }

    private void configureBase(Configuration c) {
        c.install(this::configureLogging);
        c.install(this::configureSettings);
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

    private static class AppModule {
        final boolean enabled;
        final Unit unit;

        public AppModule(boolean enabled, Unit unit) {
            this.enabled = enabled;
            this.unit = unit;
        }
    }
}
