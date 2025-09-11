package org.game.events;

import org.game.HotbarItem;

@FunctionalInterface
public interface InventoryEventHandler {
    void handleInventoryChanged(int slot, HotbarItem item);
}