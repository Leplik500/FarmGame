package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.ActionRange;
import org.game.DayNightCycle;
import org.game.InteractionCommand;

public class HouseCommand implements InteractionCommand {
    private final Spatial houseModel;
    private final DayNightCycle dayNightCycle;

    public HouseCommand(Spatial houseModel, DayNightCycle dayNightCycle) {
        this.houseModel = houseModel;
        this.dayNightCycle = dayNightCycle;
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
    public Vector3f getTargetPosition() {
        return houseModel.getWorldTranslation();
    }

    @Override
    public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return ActionRange.isWithinRange(playerPos, houseModel.getWorldTranslation());
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
