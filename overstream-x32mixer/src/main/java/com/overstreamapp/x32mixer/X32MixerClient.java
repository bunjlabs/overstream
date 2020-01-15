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
import com.overstreamapp.keeper.*;
import com.overstreamapp.x32mixer.state.X32ChannelGateState;
import com.overstreamapp.x32mixer.state.X32ChannelOnState;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class X32MixerClient {

    private final Logger logger;
    private final SocketAddress remoteAddress;
    private final OscClient oscClient;
    private final X32MixerSettings settings;
    private final X32Mixer mixer;

    private boolean[] channelOn = new boolean[32];
    private boolean[] channelGate = new boolean[32];

    private final State<X32ChannelOnState> channelOnState;
    private final State<X32ChannelGateState> channelGateState;

    @Inject
    public X32MixerClient(
            Logger logger,
            X32MixerSettings settings,
            EventLoopGroupManager eventLoopGroupManager,
            OscClient oscClient,
            Keeper keeper) {
        this.logger = logger;
        this.settings = settings;
        this.oscClient = oscClient;
        this.remoteAddress = new InetSocketAddress(settings.host(), 10023);
        this.mixer = new X32Mixer(logger, eventLoopGroupManager, oscClient);

        this.channelOnState = keeper.stateBuilder(X32ChannelOnState.class).persistenceTransient().build();
        this.channelGateState = keeper.stateBuilder(X32ChannelGateState.class).persistenceTransient().build();
    }

    public void connect() {
        oscClient.start(remoteAddress, new X32OscHandler(mixer));

        logger.info("Started with {}", remoteAddress);
    }

    public void subscribeChannelOn(int... channels) {
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                var address = String.format("/ch/%02d/mix/on", channel);

                mixer.<OscInt>subscribe(address, v -> {
                    channelOn[channel - 1] = v.getValue() > 0;
                    channelOnState.push(new X32ChannelOnState(channelOn));
                });

                logger.info("Subscribed for channel on: {}", channel);
            }
        }

    }

    public void subscribeChannelGate(int... channels) {
        float sensitivity = (float) Math.pow(10, -settings.meterSensitivity());
        for (int channel : channels) {
            if (channel >= 1 && channel <= 32) {
                mixer.meters(channel,1, sensitivity,true, v -> {
                    channelGate[channel - 1] = v > sensitivity;
                    channelGateState.push(new X32ChannelGateState(channelGate));
                });

                logger.info("Subscribed for channel gate: {}", channel);
            }
        }
    }

}
