package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.ActionRange;
import org.game.DayNightCycle;
import org.game.InteractionCommand;
import org.game.ShopUI;

public class ShopCommand implements InteractionCommand {
    private final Spatial shopModel;
    private final ShopUI shopUI;
    private final DayNightCycle dayNightCycle;

    public ShopCommand(Spatial shopModel, ShopUI shopUI, DayNightCycle dayNightCycle) {
        this.shopModel = shopModel;
        this.shopUI = shopUI;
        this.dayNightCycle = dayNightCycle;
    }

    @Override
    public boolean execute() {
        if (dayNightCycle.isNight()) {
            System.out.println("The shop is closed at night. Come back during the day!");
            return false;
        }

        shopUI.setVisible(true);
        System.out.println("Welcome to the shop!");
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
    public boolean isValidTarget() {
        return dayNightCycle.isDay(); 
    }

    @Override
    public String getDescription() {
        return dayNightCycle.isDay() ? "Open shop" : "Shop is closed (nighttime)";
    }
}
