package com.overstreamapp.x32mixer;


import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.types.OscInt;
import com.overstreamapp.statemanager.*;
import org.bson.Document;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class X32MixerClient {

    private final Logger logger;
    private final SocketAddress remoteAddress;
    private final OscClient oscClient;
    private final X32MixerSettings settings;
    private final X32Mixer mixer;
    private final State x32ChannelOnState;
    private final State x32ChannelGateState;

    @Inject
    public X32MixerClient(
            Logger logger,
            X32MixerSettings settings,
            EventLoopGroupManager eventLoopGroupManager,
            OscClient oscClient,
            StateManager stateManager) {
        this.logger = logger;
        this.settings = settings;
        this.oscClient = oscClient;
        this.remoteAddress = new InetSocketAddress(settings.host(), 10023);
        this.mixer = new X32Mixer(logger, eventLoopGroupManager, oscClient);

        this.x32ChannelOnState = stateManager.createState(new StateOptions("X32ChannelOn", StateType.STATE, X32ChannelOnStateObject::new));
        this.x32ChannelGateState = stateManager.createState(new StateOptions("X32ChannelGate", StateType.STATE, X32ChannelGateStateObject::new));
    }

    public void connect() {
        oscClient.start(remoteAddress, new X32OscHandler(mixer));

        logger.info("Started with {}", remoteAddress);
    }

    public void subscribeChannelOn(int... channels) {
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                String address = String.format("/ch/%02d/mix/on", channel);
                mixer.<OscInt>subscribe(
                        address,
                        v -> x32ChannelOnState.push(
                                new X32ChannelOnStateObject(channel, v.getValue() > 0)));
            }
        }

        logger.info("Subscribed for channels on: {}", channels);
    }

    public void subscribeChannelGate(int... channels) {
        float sensitivity = (float) Math.pow(10, -settings.meterSensitivity());
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                mixer.meters(
                        channel,
                        1,
                        sensitivity,
                        true,
                        v -> x32ChannelGateState.push(
                                new X32ChannelGateStateObject(channel, v > sensitivity)));
            }
        }

        logger.info("Subscribed for channels gate: {}", channels);
    }

    public static class X32ChannelOnStateObject implements StateObject {

        private int channel;
        private boolean enabled;

        public X32ChannelOnStateObject() {
        }

        private X32ChannelOnStateObject(int channel, boolean enabled) {
            this.channel = channel;
            this.enabled = enabled;
        }

        @Override
        public void save(Document document) {
            document.put("channel", channel);
            document.put("enabled", enabled);
        }

        @Override
        public void load(Document document) {
            this.channel = document.getInteger("channel");
            this.enabled = document.getBoolean("enabled");
        }
    }

    public static class X32ChannelGateStateObject implements StateObject {

        private int channel;
        private boolean gate;

        public X32ChannelGateStateObject() {
        }

        private X32ChannelGateStateObject(int channel, boolean gate) {
            this.channel = channel;
            this.gate = gate;
        }

        @Override
        public void save(Document document) {
            document.put("channel", channel);
            document.put("gate", gate);
        }

        @Override
        public void load(Document document) {
            this.channel = document.getInteger("channel");
            this.gate = document.getBoolean("gate");
        }
    }
}
