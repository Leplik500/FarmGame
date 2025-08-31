package org.game.events;

import org.game.HotbarItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum SimpleEventBus {
    INSTANCE;

    private final Map<String, MoneyEventHandlers> moneyHandlers = new ConcurrentHashMap<>();
    private final Map<String, InventoryEventHandlers> inventoryHandlers = new ConcurrentHashMap<>();

    public void subscribeToMoney(String subscriberId, MoneyEventHandler handler) {
        moneyHandlers.put(subscriberId, new MoneyEventHandlers(handler));
    }

    public void subscribeToInventory(String subscriberId, InventoryEventHandler handler) {
        inventoryHandlers.put(subscriberId, new InventoryEventHandlers(handler));
    }

    public void publishMoneyChanged(int newAmount) {
        MoneyChangedEvent event = new MoneyChangedEvent(newAmount);
        moneyHandlers.values().forEach(handler -> handler.handle(event));
    }

    public void publishInventoryChanged(int slot, HotbarItem item) {
        InventoryChangedEvent event = new InventoryChangedEvent(slot, item);
        inventoryHandlers.values().forEach(handler -> handler.handle(event));
    }

    public interface MoneyEventHandler {
        void handleMoneyChanged(int newAmount);
    }

    @FunctionalInterface
    public interface InventoryEventHandler {
        void handleInventoryChanged(int slot, HotbarItem item);
    }

    private record MoneyEventHandlers(MoneyEventHandler handler) {
        void handle(MoneyChangedEvent event) {
            handler.handleMoneyChanged(event.newAmount());
        }
    }

    private record InventoryEventHandlers(InventoryEventHandler handler) {
        void handle(InventoryChangedEvent event) {
            handler.handleInventoryChanged(event.slot(), event.item());
        }
    }
}
