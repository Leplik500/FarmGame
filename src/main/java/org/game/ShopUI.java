package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;
import org.game.events.SimpleEventBus;

public class ShopUI {
    private final Node root = new Node("ShopUI");
    private final Hotbar hotbar;
    private final MoneyDisplay moneyDisplay;
    private final AssetManager assetManager;
    private final InventoryManager inventoryManager;
    private boolean isVisible = false;

    private static final String[] BUY_ITEMS = {ItemIds.PUMPKIN_SEEDS, ItemIds.TOMATO_SEEDS};
    private static final String[] BUY_ICONS = {"Textures/pumpkin_seeds.png", "Textures/tomato_seeds.png"};
    private static final int[] BUY_PRICES = {GameConfig.PUMPKIN_SEEDS_PRICE, GameConfig.TOMATO_SEEDS_PRICE};

    private static final String[] SELL_ITEMS = {ItemIds.PUMPKIN, ItemIds.TOMATO};
    private static final String[] SELL_ICONS = {GameConfig.PUMPKIN_ITEM, GameConfig.TOMATO_ITEM};
    private static final int[] SELL_PRICES = {GameConfig.PUMPKIN_SELL_PRICE, GameConfig.TOMATO_SELL_PRICE};

    public ShopUI(Node guiNode, AssetManager assetManager, Hotbar hotbar,
                  MoneyDisplay moneyDisplay, InventoryManager inventoryManager) {
        this.assetManager = assetManager;
        this.hotbar = hotbar;
        this.moneyDisplay = moneyDisplay;
        this.inventoryManager = inventoryManager;

        root.setQueueBucket(RenderQueue.Bucket.Gui);
        buildUI();
        guiNode.attachChild(root);
        setVisible(false);
    }

    private void buildUI() {
        Quad background = new Quad(400, 300);
        Geometry bg = new Geometry("ShopBackground", background);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 0.2f, 0.2f, 0.8f));
        bg.setMaterial(mat);
        bg.setLocalTranslation(200, 200, -1);
        root.attachChild(bg);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText title = new BitmapText(font);
        title.setSize(24);
        title.setColor(ColorRGBA.White);
        title.setText("Shop");
        title.setLocalTranslation(350, 470, 0);
        root.attachChild(title);

        createBuySection(font);

        createSellSection(font);

        createCloseButton(font);
    }

    private void createBuySection(BitmapFont font) {
        BitmapText buyLabel = new BitmapText(font);
        buyLabel.setSize(18);
        buyLabel.setColor(ColorRGBA.Green);
        buyLabel.setText("Buy:");
        buyLabel.setLocalTranslation(220, 430, 0);
        root.attachChild(buyLabel);

        for (int i = 0; i < BUY_ITEMS.length; i++) {
            createBuyItem(i, 220 + i * 80, 380);
        }
    }

    private void createSellSection(BitmapFont font) {
        BitmapText sellLabel = new BitmapText(font);
        sellLabel.setSize(18);
        sellLabel.setColor(ColorRGBA.Yellow);
        sellLabel.setText("Sell:");
        sellLabel.setLocalTranslation(220, 320, 0);
        root.attachChild(sellLabel);

        for (int i = 0; i < SELL_ITEMS.length; i++) {
            createSellItem(i, 220 + i * 80, 270);
        }
    }

    private void createBuyItem(int index, float x, float y) {
        Picture icon = new Picture("buy_icon_" + index);
        icon.setImage(assetManager, BUY_ICONS[index], true);
        icon.setWidth(32);
        icon.setHeight(32);
        icon.setPosition(x, y);
        root.attachChild(icon);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText price = new BitmapText(font);
        price.setSize(12);
        price.setColor(ColorRGBA.White);
        price.setText("$" + BUY_PRICES[index]);
        price.setLocalTranslation(x, y - 10, 0);
        root.attachChild(price);
    }

    private void createSellItem(int index, float x, float y) {
        Picture icon = new Picture("sell_icon_" + index);
        icon.setImage(assetManager, SELL_ICONS[index], true);
        icon.setWidth(32);
        icon.setHeight(32);
        icon.setPosition(x, y);
        root.attachChild(icon);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText price = new BitmapText(font);
        price.setSize(12);
        price.setColor(ColorRGBA.White);
        price.setText("$" + SELL_PRICES[index]);
        price.setLocalTranslation(x, y - 10, 0);
        root.attachChild(price);
    }

    private void createCloseButton(BitmapFont font) {
        BitmapText closeBtn = new BitmapText(font);
        closeBtn.setSize(16);
        closeBtn.setColor(ColorRGBA.Red);
        closeBtn.setText("[X] Close");
        closeBtn.setLocalTranslation(500, 220, 0);
        root.attachChild(closeBtn);
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
        root.setCullHint(visible ? Spatial.CullHint.Never : Spatial.CullHint.Always);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void handleClick(float x, float y) {
        if (!isVisible) return;

        if (x >= 500 && x <= 580 && y >= 200 && y <= 230) {
            setVisible(false);
            return;
        }

        for (int i = 0; i < BUY_ITEMS.length; i++) {
            float itemX = 220 + i * 80;
            if (x >= itemX && x <= itemX + 32 && y >= 380 && y <= 412) {
                buyItem(i);
                return;
            }
        }

        for (int i = 0; i < SELL_ITEMS.length; i++) {
            float itemX = 220 + i * 80;
            if (x >= itemX && x <= itemX + 32 && y >= 270 && y <= 302) {
                sellItem(i);
                return;
            }
        }

    }

    private void buyItem(int index) {
        String itemId = BUY_ITEMS[index];
        int price = BUY_PRICES[index];

        if (moneyDisplay.getMoney() >= price) {
            SimpleEventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney() - price);

            if (inventoryManager.tryAddItem(itemId, 1)) {
                System.out.println("Bought " + itemId + " for $" + price);
            } else {
                SimpleEventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney() + price);
                System.out.println("Inventory full! Purchase refunded.");
            }
        } else {
            System.out.println("Not enough money to buy " + itemId);
        }
    }

    private void sellItem(int index) {
        String itemId = SELL_ITEMS[index];
        int price = SELL_PRICES[index];

        if (inventoryManager.tryRemoveItem(itemId, 1)) {
            SimpleEventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney() + price);
            System.out.println("Sold " + itemId + " for $" + price);
        } else {
            System.out.println("No " + itemId + " to sell!");
        }
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

    private boolean addItemToInventoryViaEvents(String itemId, int count) {
        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing != null && existing.id().equals(itemId)) {
                HotbarItem updated = existing.withCount(existing.count() + count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                return true;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (hotbar.getSlotItem(i) == null) {
                String iconPath = getIconPath(itemId);
                HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, newItem);
                return true;
            }
        }

        return false; 
    }

}
