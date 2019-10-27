package com.overstreamapp.messagebus;

public interface MessageBus {

    <T extends Message> void publish(T event);

    <T extends Message> void subscribe(Class<T> messageType, MessageListener<T> listener);

    void subscribeAll(MessageListener<Message> listener);
}

