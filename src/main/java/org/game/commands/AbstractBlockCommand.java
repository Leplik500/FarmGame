package org.game.commands;

import com.jme3.math.Vector3f;
import org.game.*;

public abstract class AbstractBlockCommand implements InteractionCommand {
    protected final Vector3i targetBlock;
    protected final SimpleBlockWorld world;

    protected AbstractBlockCommand(Vector3i targetBlock, SimpleBlockWorld world) {
        this.targetBlock = targetBlock;
        this.world = world;
    }

    @Override
    public final boolean execute() {
        if (!isValidTarget()) {
            return false;
        }

        boolean result = performAction();
        if (result) {
            logSuccess();
        }
        return result;
    }

    @Override
    public final Vector3f getTargetPosition() {
        return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
    }

    @Override
    public final boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return ActionRange.isWithinRange(playerPos, targetBlock);
    }

    @Override
    public boolean isValidTarget() {
        int blockType = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        return isTargetBlockTypeValid(blockType);
    }

    protected abstract boolean performAction();
    protected abstract void logSuccess();
    protected abstract boolean isTargetBlockTypeValid(int blockType);
    public abstract String getDescription();
}
