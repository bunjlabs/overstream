package com.overstreamapp.messagebus;

@FunctionalInterface
public interface MessageListener<T extends Message> {

    void onEvent(T message);
}
