package com.overstreamapp.x32mixer;


import com.overstreamapp.osc.types.OscType;

public interface X32SubscriptionListener<T extends OscType> {

    void valueChanged(T value);
}
