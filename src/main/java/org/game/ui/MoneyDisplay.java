package org.game.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import org.game.utils.GameConfig;
import org.game.events.EventBus;

public class MoneyDisplay {
    private final Node root = new Node("MoneyDisplay");
    private final BitmapText moneyText;
    private int money = 0;

    public MoneyDisplay(Node guiNode, AssetManager assetManager, int screenW) {
        EventBus.INSTANCE.subscribeToMoney("moneyDisplay", this::updateMoneyDisplay);
        
        root.setQueueBucket(RenderQueue.Bucket.Gui);

        Picture moneyIcon = new Picture("MoneyIcon");
        moneyIcon.setImage(assetManager, GameConfig.MONEY_ICON, true);
        moneyIcon.setWidth(32);
        moneyIcon.setHeight(32);
        moneyIcon.setPosition(0, 0);
        moneyIcon.setQueueBucket(RenderQueue.Bucket.Gui);

        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        moneyText = new BitmapText(font);
        moneyText.setSize(20);
        moneyText.setColor(ColorRGBA.Yellow);
        moneyText.setText("0");
        moneyText.setLocalTranslation(32, 25, 0);
        moneyText.setQueueBucket(RenderQueue.Bucket.Gui);

        root.attachChild(moneyIcon);
        root.attachChild(moneyText);
        guiNode.attachChild(root);

        positionRightOfHotbar(screenW);
    }

    private void positionRightOfHotbar(int screenW) {
        int slotSize = 64;
        int slotGap = 6;
        int slotCount = 9;
        int totalHotbarW = slotCount * slotSize + (slotCount - 1) * slotGap;
        int hotbarX = (screenW - totalHotbarW) / 2;
        int marginBottom = 12;

        int x = hotbarX + totalHotbarW + 20;
        root.setLocalTranslation(x, marginBottom, 0);
    }

    public void setMoney(int amount) {
        EventBus.INSTANCE.publishMoneyChanged(amount);
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        EventBus.INSTANCE.publishMoneyChanged(money + amount);
    }


    public void updatePosition(int screenW) {
        positionRightOfHotbar(screenW);
    }
    
    private void updateMoneyDisplay(int newAmount) {
        this.money = Math.max(0, newAmount);
        moneyText.setText(String.valueOf(this.money));
    }
    
    
}
