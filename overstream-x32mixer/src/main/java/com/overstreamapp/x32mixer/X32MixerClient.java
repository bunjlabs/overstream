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

package com.overstreamapp.x32mixer;


import com.bunjlabs.fuga.inject.Inject;
import com.overstreamapp.network.EventLoopGroupManager;
import com.overstreamapp.osc.OscClient;
import com.overstreamapp.osc.types.OscInt;
import com.overstreamapp.store.Store;
import com.overstreamapp.store.StoreKeeper;
import com.overstreamapp.x32mixer.state.X32ChannelGate;
import com.overstreamapp.x32mixer.state.X32ChannelOn;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class X32MixerClient {

    private final Logger logger;
    private final SocketAddress remoteAddress;
    private final OscClient oscClient;
    private final X32MixerSettings settings;
    private final X32Mixer mixer;

    private final Store<X32ChannelOn> channelOnStore;
    private final Store<X32ChannelGate> channelGateStore;

    @Inject
    public X32MixerClient(
            Logger logger,
            X32MixerSettings settings,
            EventLoopGroupManager eventLoopGroupManager,
            OscClient oscClient,
            StoreKeeper storeKeeper) {
        this.logger = logger;
        this.settings = settings;
        this.oscClient = oscClient;
        this.remoteAddress = new InetSocketAddress(settings.host(), 10023);
        this.mixer = new X32Mixer(logger, eventLoopGroupManager, oscClient);

        this.channelOnStore = storeKeeper.storeBuilder(X32ChannelOn.class)
                .withInitial(new X32ChannelOn())
                .withReducer((action, state) -> {
                    var newState = new X32ChannelOn(state.getChannels());
                    if (action instanceof X32ChannelOn.SetChannelOn) {
                        var channelOn = (X32ChannelOn.SetChannelOn) action;
                        newState.getChannels()[channelOn.getChannel()] = channelOn.getValue();
                    }
                    return newState;
                })
                .build();
        this.channelGateStore = storeKeeper.storeBuilder(X32ChannelGate.class)
                .withInitial(new X32ChannelGate())
                .withReducer((action, state) -> {
                    var newState = new X32ChannelGate(state.getChannels());
                    if (action instanceof X32ChannelGate.SetChannelGate) {
                        var channelGate = (X32ChannelGate.SetChannelGate) action;
                        newState.getChannels()[channelGate.getChannel()] = channelGate.getValue();
                    }
                    return newState;
                })
                .build();
    }

    public void connect() {
        oscClient.start(remoteAddress, this.mixer.getX32OscHandler());
        mixer.start();

        logger.info("Started with {}", remoteAddress);
    }

    public void subscribeChannelOn(int... channels) {
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                var address = String.format("/ch/%02d/mix/on", channel);

                mixer.<OscInt>subscribe(address, v -> {
                    channelOnStore.dispatch(
                            new X32ChannelOn.SetChannelOn(channel - 1, v.getValue() > 0));
                });

                logger.info("Subscribed for channel on: {}", channel);
            }
        }

    }

    public void subscribeChannelGate(int... channels) {
        float sensitivity = (float) Math.pow(10, -settings.meterSensitivity());
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                mixer.meters(channel, 1, sensitivity, true, v -> {
                    channelGateStore.dispatch(
                            new X32ChannelGate.SetChannelGate(channel - 1, v > sensitivity));
                });

                logger.info("Subscribed for channel gate: {}", channel);
            }
        }
    }

}
