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

package com.overstreamapp.x32mixer.support;


import com.overstreamapp.osc.types.OscType;

class X32Subscription<T extends OscType> {

    private final String address;
    private final X32SubscriptionListener<T> listener;

    private T value;

    X32Subscription(String address, X32SubscriptionListener<T> listener) {
        this.address = address;
        this.listener = listener;
        this.value = null;
    }

    String getAddress() {
        return address;
    }

    void onData(T newValue) {
        if (value == null || !value.equals(newValue)) {
            value = newValue;

            fireValueChanged();
        }
    }

    private void fireValueChanged() {
        listener.valueChanged(value);
    }
}
