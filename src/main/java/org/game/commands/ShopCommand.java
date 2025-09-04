package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.ActionRange;
import org.game.InteractionCommand;
import org.game.ShopUI;

public class ShopCommand implements InteractionCommand {
    private final Spatial shopModel;
    private final ShopUI shopUI;

    public ShopCommand(Spatial shopModel, ShopUI shopUI) {
        this.shopModel = shopModel;
        this.shopUI = shopUI;
    }

    @Override
    public boolean execute() {
        shopUI.setVisible(true);
        return true;
    }

    @Override
    public Vector3f getTargetPosition() {
        return shopModel.getWorldTranslation();
    }

    @Override
    public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return ActionRange.isWithinRange(playerPos, shopModel.getWorldTranslation());
    }

    @Override
    public String getDescription() {
        return "Open shop";
    }

    @Override
    public boolean isValidTarget() {
        return true;
    }
}
