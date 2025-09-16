package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.utils.ActionRange;
import org.game.states.DayNightCycle;
import org.game.core.InteractionCommand;

public abstract class AbstractLocationCommand implements InteractionCommand {
    protected final Spatial model;
    protected final DayNightCycle dayNightCycle;

    protected AbstractLocationCommand(Spatial model, DayNightCycle dayNightCycle) {
        this.model = model;
        this.dayNightCycle = dayNightCycle;
    }

    @Override
    public final Vector3f getTargetPosition() {
        return model.getWorldTranslation();
    }

    @Override
    public final boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return ActionRange.isWithinRange(playerPos, model.getWorldTranslation());
    }

    @Override
    public abstract boolean execute();
    @Override
    public abstract boolean isValidTarget();
    @Override
    public abstract String getDescription();
}
