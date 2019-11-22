/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.overstreamapp.x32mixer;

/**
 * @author Artem Shurygin <artem.shurygin@bunjlabs.com>
 */
public class X32Meter {

    private static final float EPS = 1E-3f;

    private final int index;
    private final float sensitivity;
    private final boolean latch;
    private final X32MeterListener listener;

    private float value;

    public X32Meter(int channel, int type, float sensitivity, boolean latch, X32MeterListener listener) {
        this.sensitivity = sensitivity;
        this.latch = latch;
        this.index = 32 * type + (channel - 1);
        this.listener = listener;

        this.value = 0f;
    }

    public void onData(float[] newValues) {
        if (index >= newValues.length) {
            return;
        }

        float newValue = newValues[index];
        float diff = Math.abs(newValue - value);

        if (latch) {
            if(value > sensitivity && newValue < sensitivity) {
                value = newValue;
                fireValueChanged();
            } else if(value < sensitivity && newValue > sensitivity) {
                value = newValue;
                fireValueChanged();
            }
        } else if (Math.abs(newValue - value) > sensitivity) {
            value = newValue;
            fireValueChanged();
        }
    }

    public void fireValueChanged() {
        listener.valueChanged(value);
    }

}
