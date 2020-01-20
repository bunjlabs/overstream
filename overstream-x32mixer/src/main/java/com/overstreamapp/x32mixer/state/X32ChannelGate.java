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

package com.overstreamapp.x32mixer.state;

import com.overstreamapp.store.Action;

import java.util.Arrays;

public class X32ChannelGate {
    private boolean[] channels = new boolean[32];

    public X32ChannelGate() {
    }

    public X32ChannelGate(boolean[] channels) {
        System.arraycopy(channels, 0, this.channels, 0, 32);
    }


    public boolean[] getChannels() {
        return channels;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        X32ChannelGate that = (X32ChannelGate) o;
        return Arrays.equals(channels, that.channels);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(channels);
    }

    public static class SetChannelGate implements Action {
        private final int channel;
        private final boolean value;

        public SetChannelGate(int channel, boolean value) {
            this.channel = channel;
            this.value = value;
        }

        public int getChannel() {
            return channel;
        }

        public boolean getValue() {
            return value;
        }
    }
}
