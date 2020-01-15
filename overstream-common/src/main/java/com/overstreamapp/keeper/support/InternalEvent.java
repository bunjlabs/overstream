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

package com.overstreamapp.keeper.support;

import com.overstreamapp.keeper.Event;
import com.overstreamapp.keeper.EventInfo;
import com.overstreamapp.keeper.EventObject;

class InternalEvent<T extends EventObject> implements Event<T> {
    private final DefaultKeeper keeper;
    private final EventInfo<T> info;

    InternalEvent(DefaultKeeper keeper, EventInfo<T> info) {
        this.keeper = keeper;
        this.info = info;
    }

    @Override
    public void fire(T eventObject) {
        keeper.fireEvent(info, eventObject);
    }

    @Override
    public EventInfo<T> getInfo() {
        return info;
    }
}
