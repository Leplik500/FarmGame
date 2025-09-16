package org.game.commands;

import com.jme3.scene.Spatial;
import org.game.states.DayNightCycle;
import org.game.ui.ShopUI;

public class ShopCommand extends AbstractLocationCommand {
    private final ShopUI shopUI;

    public ShopCommand(Spatial shopModel, ShopUI shopUI, DayNightCycle dayNightCycle) {
        super(shopModel, dayNightCycle);
        this.shopUI = shopUI;
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
    public boolean isValidTarget() {
        return dayNightCycle.isDay();
    }

    @Override
    public String getDescription() {
        return dayNightCycle.isDay() ? "Open shop" : "Shop is closed (nighttime)";
    }
}

