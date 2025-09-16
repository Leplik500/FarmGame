package org.game.ui;

import org.game.utils.GameConfig;
import org.game.utils.ItemIds;

enum ShopArticle {
    BUY_PUMPKIN_SEEDS(ItemIds.PUMPKIN_SEEDS, GameConfig.PUMPKIN_SEEDS_ITEM,
            GameConfig.PUMPKIN_SEEDS_PRICE),
    BUY_TOMATO_SEEDS(ItemIds.TOMATO_SEEDS, GameConfig.TOMATO_SEEDS_ITEM,
            GameConfig.TOMATO_SEEDS_PRICE),
    SELL_PUMPKIN(ItemIds.PUMPKIN, GameConfig.PUMPKIN_ITEM,
            GameConfig.PUMPKIN_SELL_PRICE),
    SELL_TOMATO(ItemIds.TOMATO, GameConfig.TOMATO_ITEM,
            GameConfig.TOMATO_SELL_PRICE);

    private final String itemId;
    private final String icon;
    private final int    price;
    ShopArticle(String id, String iconPath, int price) {
        this.itemId = id; this.icon = iconPath; this.price = price;
    }
    String id()    { return itemId; }
    String icon()  { return icon;  }
    int    price() { return price; }
    boolean isBuy()  { return name().startsWith("BUY"); }
}