package com.overstreamapp.x32mixer;


import com.overstreamapp.osc.types.OscType;

public class X32Subscription<T extends OscType> {

    private final String address;
    private final X32SubscriptionListener listener;

    private T value;

    public X32Subscription(String address, X32SubscriptionListener listener) {
        this.address = address;
        this.listener = listener;
        this.value = null;
    }

    public void onData(T newValue) {
        if (value == null || !value.equals(newValue)) {
            value = newValue;

            fireValueChanged();
        }
    }

    public void fireValueChanged() {
        listener.valueChanged(value);
    }

}
