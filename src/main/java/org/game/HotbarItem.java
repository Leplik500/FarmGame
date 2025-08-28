package org.game;

public record HotbarItem(String id, String iconPath, int count) {
    // Конструктор для совместимости с существующим кодом
    public HotbarItem(String id, String iconPath) {
        this(id, iconPath, 1);
    }

    // Метод для создания копии с новым количеством
    public HotbarItem withCount(int newCount) {
        return new HotbarItem(id, iconPath, newCount);
    }
}
