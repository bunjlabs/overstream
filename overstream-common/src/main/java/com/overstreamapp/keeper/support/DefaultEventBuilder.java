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

import com.overstreamapp.keeper.*;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class DefaultEventBuilder<T extends EventObject> implements EventBuilder<T> {
    private final DefaultKeeper keeper;
    private final Class<T> type;

    public DefaultEventBuilder(DefaultKeeper keeper, Class<T> type) {
        this.keeper = keeper;
        this.type = type;
    }

    @Override
    public Event<T> build() {
        Constructor<T> constructor;

        try {
            constructor = type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to build state object", e);
        }


        Supplier<T> supplier = () -> {
            try {
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to create state object instance", e);
            }
        };

        var info = new EventInfo<>(type, supplier);

        return keeper.createEvent(info);
    }
}
