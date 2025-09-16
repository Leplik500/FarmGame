package org.game.systems;

import org.game.utils.GameConfig;
import org.game.utils.ItemIds;
import org.game.events.EventBus;
import org.game.ui.Hotbar;
import org.game.ui.HotbarItem;

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
                EventBus.INSTANCE.publishInventoryChanged(i, updated);
                return true;
            }
        }

        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            if (hotbar.getSlotItem(i) == null) {
                String iconPath = getIconPath(itemId);
                HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                EventBus.INSTANCE.publishInventoryChanged(i, newItem);
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
                    EventBus.INSTANCE.publishInventoryChanged(i, null);
                } else {
                    HotbarItem updated = item.withCount(newCount);
                    EventBus.INSTANCE.publishInventoryChanged(i, updated);
                }
                return true;
            }
        }
        return false;
    }

    private String getIconPath(String itemId) {
        return switch (itemId) {
            case ItemIds.PUMPKIN_SEEDS -> GameConfig.PUMPKIN_SEEDS_ITEM;
            case ItemIds.TOMATO_SEEDS -> GameConfig.TOMATO_SEEDS_ITEM;
            case ItemIds.PUMPKIN -> GameConfig.PUMPKIN_ITEM;
            case ItemIds.TOMATO -> GameConfig.TOMATO_ITEM;
            default -> GameConfig.DEFAULT_ITEM;
        };
    }
}
