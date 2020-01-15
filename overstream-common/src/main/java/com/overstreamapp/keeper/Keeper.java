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

package com.overstreamapp.keeper;

import java.util.List;

public interface Keeper {

    <T extends StateObject> StateBuilder<T> stateBuilder(Class<T> stateClass);

    <T extends EventObject> EventBuilder<T> eventBuilder(Class<T> eventClass);

    <T extends StateObject> void subscribe(Class<T> type, StateListener<T> listener);

    <T extends EventObject> void subscribe(Class<T> type, EventListener<T> listener);

    void subscribeAll(StateListener<StateObject> listener);

    void subscribeAll(EventListener<EventObject> listener);

    void burstState(StateListener<StateObject> listener);

    List<StateInfo> getAllStateInfo();

    List<EventInfo> getAllEventInfo();
}
