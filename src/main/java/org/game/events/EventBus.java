package org.game.events;

import org.game.ui.HotbarItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum EventBus {
    INSTANCE;

    private final Map<String, MoneyEventHandler> moneyHandlers = new ConcurrentHashMap<>();
    private final Map<String, InventoryEventHandler> inventoryHandlers = new ConcurrentHashMap<>();

    public void subscribeToMoney(String subscriberId, MoneyEventHandler handler) {
        moneyHandlers.put(subscriberId, handler);
    }

    public void subscribeToInventory(String subscriberId, InventoryEventHandler handler) {
        inventoryHandlers.put(subscriberId, handler);
    }

    public void publishMoneyChanged(int newAmount) {
        moneyHandlers.values().forEach(handler ->
                handler.handleMoneyChanged(newAmount));
    }

    public void publishInventoryChanged(int slot, HotbarItem item) {
        inventoryHandlers.values().forEach(handler ->
                handler.handleInventoryChanged(slot, item));
    }
}
