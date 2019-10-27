package com.overstreamapp.x32mixer;


import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.messagebus.MessageBus;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.types.OscInt;
import com.overstreamapp.x32mixer.messages.ChannelGateMessage;
import com.overstreamapp.x32mixer.messages.ChannelOnMessage;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class X32MixerClient {

    private final X32MixerSettings settings;
    private final Logger logger;
    private final MessageBus messageBus;
    private final X32Mixer mixer;
    private final SocketAddress remoteAddress;

    @Inject
    public X32MixerClient(
            X32MixerSettings settings,
            Logger logger,
            EventLoopGroupManager eventLoopGroupManager,
            OscClient oscClient,
            MessageBus messageBus) {
        this.settings = settings;
        this.logger = logger;
        this.messageBus = messageBus;
        this.remoteAddress = new InetSocketAddress(settings.host(), 10023);

        mixer = new X32Mixer(remoteAddress, logger, eventLoopGroupManager, oscClient);
    }

    public void start() {
        mixer.start();

        mixer.<OscInt>subscribe("/ch/01/mix/on", v -> messageBus.publish(new ChannelOnMessage(this, 1, v.getValue() > 0)));
        mixer.<OscInt>subscribe("/ch/15/mix/on", v -> messageBus.publish(new ChannelOnMessage(this, 15, v.getValue() > 0)));
        mixer.<OscInt>subscribe("/ch/19/mix/on", v -> messageBus.publish(new ChannelOnMessage(this, 19, v.getValue() > 0)));

        mixer.meters(1, 1, settings.meterSensitivity(),
                v -> messageBus.publish(new ChannelGateMessage(this, 1, v)));

        logger.info("Started with {}", remoteAddress);
    }
}
