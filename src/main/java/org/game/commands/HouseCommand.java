package org.game.commands;

import com.jme3.scene.Spatial;
import org.game.states.DayNightCycle;

public class HouseCommand extends AbstractLocationCommand {
    public HouseCommand(Spatial houseModel, DayNightCycle dayNightCycle) {
        super(houseModel, dayNightCycle);
    }

    @Override
    public boolean execute() {
        if (dayNightCycle.isNight()) {
            dayNightCycle.skipToDay();
            System.out.println("You slept through the night. Good morning!");
            return true;
        } else {
            System.out.println("You can only sleep at night.");
            return false;
        }
    }

    @Override
    public boolean isValidTarget() {
        return dayNightCycle.isNight();
    }

    @Override
    public String getDescription() {
        return "Sleep in house";
    }
}

