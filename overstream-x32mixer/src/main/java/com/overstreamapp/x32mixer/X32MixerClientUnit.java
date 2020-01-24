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

import com.bunjlabs.fuga.inject.Configuration;
import com.bunjlabs.fuga.inject.Singleton;
import com.bunjlabs.fuga.inject.Unit;
import com.overstreamapp.x32mixer.support.DefaultX32MixerClient;

public class X32MixerClientUnit implements Unit {

    @Override
    public void setup(Configuration c) {
        c.bind(X32MixerSettings.class).auto();
        c.bind(X32MixerCommands.class).auto();
        c.bind(DefaultX32MixerClient.class).auto();
        c.bind(X32MixerClient.class).to(DefaultX32MixerClient.class).in(Singleton.class);
    }
}
