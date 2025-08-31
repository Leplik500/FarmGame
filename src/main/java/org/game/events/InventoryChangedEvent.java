package org.game.events;

import org.game.HotbarItem;

public record InventoryChangedEvent(int slot, HotbarItem item) {}
