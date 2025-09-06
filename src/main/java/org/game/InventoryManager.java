package org.game;

import org.game.events.SimpleEventBus;

public class InventoryManager {
    private final Hotbar hotbar;

    public InventoryManager(Hotbar hotbar) {
        this.hotbar = hotbar;
    }

    public boolean tryAddItem(String itemId, int count) {
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing != null && existing.id().equals(itemId)) {
                HotbarItem updated = existing.withCount(existing.count() + count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                return true;
            }
        }

        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            if (hotbar.getSlotItem(i) == null) {
                String iconPath = getIconPath(itemId);
                HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, newItem);
                return true;
            }
        }

        return false;
    }

    public boolean tryRemoveItem(String itemId, int count) {
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            HotbarItem item = hotbar.getSlotItem(i);
            if (item != null && item.id().equals(itemId) && item.count() >= count) {
                int newCount = item.count() - count;
                if (newCount <= 0) {
                    SimpleEventBus.INSTANCE.publishInventoryChanged(i, null);
                } else {
                    HotbarItem updated = item.withCount(newCount);
                    SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                }
                return true;
            }
        }
        return false;
    }

    private String getIconPath(String itemId) {
        return switch (itemId) {
            case ItemIds.PUMPKIN_SEEDS -> "Textures/pumpkin_seeds.png";
            case ItemIds.TOMATO_SEEDS -> "Textures/tomato_seeds.png";
            case ItemIds.PUMPKIN -> GameConfig.PUMPKIN_ITEM;
            case ItemIds.TOMATO -> GameConfig.TOMATO_ITEM;
            default -> GameConfig.DEFAULT_ITEM;
        };
    }
}
