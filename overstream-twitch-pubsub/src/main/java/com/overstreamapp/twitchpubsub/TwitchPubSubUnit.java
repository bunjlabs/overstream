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

package com.overstreamapp.twitchpubsub;

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.twitchpubsub.support.DefaultTwitchPubSub;

public class TwitchPubSubUnit implements Unit {
    @Override
    public void setup(Configuration c) {
        c.bind(TwitchPubSubSettings.class).auto();
        c.bind(TwitchPubSubCommands.class).auto();
        c.bind(DefaultTwitchPubSub.class).auto();
        c.bind(TwitchPubSub.class).to(DefaultTwitchPubSub.class).in(Singleton.class);
    }
}
