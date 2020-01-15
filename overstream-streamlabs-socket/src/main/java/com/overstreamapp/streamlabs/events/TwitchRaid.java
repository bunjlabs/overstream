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

package com.overstreamapp.streamlabs.events;

import com.overstreamapp.keeper.EventObject;

public class TwitchRaid implements EventObject {
    public String name;
    public int raiders;

    public TwitchRaid() {
    }

    public TwitchRaid(String name, int raiders) {
        this.name = name;
        this.raiders = raiders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRaiders() {
        return raiders;
    }

    public void setRaiders(int raiders) {
        this.raiders = raiders;
    }
}
