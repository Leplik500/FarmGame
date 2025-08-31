package org.game.events;

import java.util.*;
import java.util.function.Consumer;

public enum GameEventManager {
    INSTANCE;

    private final Map<Class<?>, List<Consumer<?>>> observers = new HashMap<>();

    public <T> void subscribe(Class<T> eventType, Consumer<T> observer) {
        observers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
    }

    public <T> void publish(T event) {
        List<Consumer<?>> eventObservers = observers.get(event.getClass());
        if (eventObservers != null) {
            for (Consumer<?> observer : eventObservers) {
                ((Consumer<T>) observer).accept(event);
            }
        }
    }
}
