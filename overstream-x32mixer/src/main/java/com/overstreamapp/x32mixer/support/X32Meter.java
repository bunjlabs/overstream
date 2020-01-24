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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.overstreamapp.x32mixer.support;

/**
 * @author Artem Shurygin <artem.shurygin@bunjlabs.com>
 */
class X32Meter {

    private static final float EPS = 1E-3f;

    private final int index;
    private final float sensitivity;
    private final boolean latch;
    private final X32MeterListener listener;

    private float value;

    X32Meter(int channel, int type, float sensitivity, boolean latch, X32MeterListener listener) {
        this.sensitivity = sensitivity;
        this.latch = latch;
        this.index = 32 * type + (channel - 1);
        this.listener = listener;

        this.value = 0f;
    }

    void onData(float[] newValues) {
        if (index >= newValues.length) {
            return;
        }

        float newValue = newValues[index];
        float diff = Math.abs(newValue - value);

        if (latch) {
            if (value > sensitivity && newValue < sensitivity) {
                value = newValue;
                fireValueChanged();
            } else if (value < sensitivity && newValue > sensitivity) {
                value = newValue;
                fireValueChanged();
            }
        } else if (Math.abs(newValue - value) > sensitivity) {
            value = newValue;
            fireValueChanged();
        }
    }

    void fireValueChanged() {
        listener.valueChanged(value);
    }

}
