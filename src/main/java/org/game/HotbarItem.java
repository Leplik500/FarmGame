package org.game;

public record HotbarItem(String id, String iconPath, int count) {
    public HotbarItem(String id, String iconPath) {
        this(id, iconPath, 1);
    }

    public HotbarItem withCount(int newCount) {
        return new HotbarItem(id, iconPath, newCount);
    }
}
