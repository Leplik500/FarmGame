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
import org.game.events.EventBus;

public final class ShopUI {
    private final Node root = new Node("ShopUI");
    private final MoneyDisplay moneyDisplay;
    private final AssetManager assetManager;
    private final InventoryManager inventoryManager;
    private boolean visible;

    public ShopUI(Node guiRoot, AssetManager assets,
                  MoneyDisplay money, InventoryManager inv) {
        this.assetManager = assets;
        this.moneyDisplay = money;
        this.inventoryManager = inv;

        root.setQueueBucket(RenderQueue.Bucket.Gui);
        buildUI();
        guiRoot.attachChild(root);
        setVisible(false);
    }

    private void buildUI() {
        Geometry bg = new Geometry("bg", new Quad(400, 300));
        Material m  = new Material(assetManager, "Common/MatDefs/Gui/Gui.j3md");
        m.setColor("Color", new ColorRGBA(.18f,.23f,.30f,.75f));
        bg.setMaterial(m);
        bg.setLocalTranslation(200, 200, -1);
        root.attachChild(bg);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        addLabel(font, "Shop", ColorRGBA.White, 24, 350, 470);

        buildSection(font, "Buy:",  ColorRGBA.Green, 430,
                ShopArticle.BUY_PUMPKIN_SEEDS, ShopArticle.BUY_TOMATO_SEEDS);
        buildSection(font, "Sell:", ColorRGBA.Yellow, 320,
                ShopArticle.SELL_PUMPKIN,     ShopArticle.SELL_TOMATO);

        addLabel(font, "[X] Close", ColorRGBA.Red, 16, 500, 220);
    }

    private void buildSection(BitmapFont f, String title, ColorRGBA color,
                              float y, ShopArticle... articles) {
        addLabel(f, title, color, 18, 220, y);
        for (int i = 0; i < articles.length; i++)
            addItem(articles[i], 220 + i * 80, y - 50);
    }

    private void addLabel(BitmapFont font, String txt, ColorRGBA col,
                          int size, float x, float y) {
        BitmapText t = new BitmapText(font);
        t.setText(txt); t.setColor(col); t.setSize(size);
        t.setLocalTranslation(x, y, 0);
        root.attachChild(t);
    }

    private void addItem(ShopArticle art, float x, float y) {
        Picture pic = new Picture(art.id());
        pic.setImage(assetManager, art.icon(), true);
        pic.setWidth(32); pic.setHeight(32); pic.setPosition(x, y);
        root.attachChild(pic);

        BitmapText price = new BitmapText(assetManager
                .loadFont("Interface/Fonts/Default.fnt"));
        price.setText("$" + art.price());
        price.setColor(ColorRGBA.White);
        price.setSize(12);
        price.setLocalTranslation(x, y - 10, 0);
        root.attachChild(price);
    }

    public void setVisible(boolean v) {
        visible = v;
        root.setCullHint(v ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
    }
    public boolean isVisible() { return visible; }

    public void handleClick(float x, float y) {
        if (!visible) return;

        if (x>=500 && x<=580 && y>=200 && y<=230) { setVisible(false); return; }

        for (ShopArticle art : ShopArticle.values()) {
            float baseX = 220 + (art.ordinal()%2)*80;
            float baseY = (art.isBuy()?430:320) - 50;
            if (x>=baseX && x<=baseX+32 && y>=baseY && y<=baseY+32) {
                if (art.isBuy()) buy(art); else sell(art);
                return;
            }
        }
    }

    private void buy(ShopArticle a) {
        int price = a.price();
        if (moneyDisplay.getMoney() < price) {                  // not enough $
            System.out.println("Not enough money to buy "+a.id());
            return;
        }
        EventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney() - price);
        if (inventoryManager.tryAddItem(a.id(), 1)) {
            System.out.println("Bought "+a.id()+" for $"+price);
        } else {                                               // inventory full
            EventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney()+price);
            System.out.println("Inventory full. Purchase refunded.");
        }
    }

    private void sell(ShopArticle a) {
        if (!inventoryManager.tryRemoveItem(a.id(),1)) {
            System.out.println("No "+a.id()+" to sell");
            return;
        }
        EventBus.INSTANCE.publishMoneyChanged(moneyDisplay.getMoney()+a.price());
        System.out.println("Sold "+a.id()+" for $"+a.price());
    }
}
