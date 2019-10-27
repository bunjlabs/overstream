package com.overstreamapp.messagebus;

import com.bunjlabs.fuga.inject.Inject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleMessageBus implements MessageBus {

    private final List<MessageListener<Message>> listenersList;
    private final Map<Class<?>, List<MessageListener>> listenersMap;
    private final Logger log;

    @Inject
    public SimpleMessageBus(Logger log) {
        this.log = log;
        this.listenersList = new CopyOnWriteArrayList<>();
        this.listenersMap = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends Message> void publish(T event) {
        log.debug("Publishing event {}", event);

        listenersList.forEach(l -> fireEvent(l, event));

        var listeners = listenersMap.get(event.getClass());
        if (listeners != null) {
            listeners.forEach(l -> fireEvent(l, event));
        }
    }

    @Override
    public <T extends Message> void subscribe(Class<T> messageType, MessageListener<T> listener) {
        var listeners = listenersMap.get(messageType);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            listenersMap.put(messageType, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public void subscribeAll(MessageListener<Message> listener) {
        listenersList.add(listener);
    }

    @SuppressWarnings("unchecked")
    private void fireEvent(MessageListener listener, Message message) {
        try {
            listener.onEvent(message);
        } catch (Throwable t) {
            log.error("Error while firing message to {}: {}", listener, t);
        }
    }
}
