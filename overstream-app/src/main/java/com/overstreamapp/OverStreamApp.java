package com.overstreamapp;

import com.bunjlabs.fuga.context.ApplicationContext;
import com.bunjlabs.fuga.context.FugaBoot;
import com.bunjlabs.fuga.environment.Environment;
import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Unit;
import com.bunjlabs.fuga.logging.LoggingUnitBuilder;
import com.bunjlabs.fuga.settings.SettingsUnitBuilder;
import com.bunjlabs.fuga.settings.source.ClassPathSettingsSource;
import com.bunjlabs.fuga.settings.source.LocalFilesSettingsSource;
import com.overstreamapp.messagebus.MessageBusUnit;
import com.overstreamapp.messageserver.MessageServer;
import com.overstreamapp.messageserver.MessageServerUnit;
import com.overstreamapp.network.EventLoopGroupManagerUnit;
import com.overstreamapp.osc.NioOscClientUnit;
import com.overstreamapp.websocket.client.netty.NettyWebSocketClientUnit;
import com.overstreamapp.websocket.server.NettyWebSocketServerUnit;
import com.overstreamapp.x32mixer.X32MixerClient;
import com.overstreamapp.x32mixer.X32MixerClientUnit;

import java.util.LinkedList;

public class OverStreamApp {

    public static void main(String[] args) {
        new OverStreamApp().start();
    }

    private void start() {
        ApplicationContext context = FugaBoot.start(this::configure);
        var settings = context.getInjector().getInstance(AppSettings.class);

        var units = new LinkedList<Unit>();
        if (settings.messageServer()) {
            units.add(new MessageServerUnit());
        }
        if (settings.x32MixerClient()) {
            units.add(new X32MixerClientUnit());
        }

        var injector = context.getInjector().createChildInjector(units);
        if (settings.messageServer()) {
            injector.getInstance(MessageServer.class).start();
        }
        if (settings.x32MixerClient()) {
            injector.getInstance(X32MixerClient.class).start();
        }
    }

    private void configure(Configuration c) {
        c.install(this::configureLogging);
        c.install(this::configureSettings);

        c.install(new MessageBusUnit());
        c.install(new EventLoopGroupManagerUnit());
        c.install(new NettyWebSocketServerUnit());
        c.install(new NettyWebSocketClientUnit());
        c.install(new NioOscClientUnit());
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

    private void configureLogging(Configuration c) {
        c.install(new LoggingUnitBuilder().build());
    }
}
