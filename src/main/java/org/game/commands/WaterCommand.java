package org.game.commands;

import com.jme3.math.Vector3f;
import org.game.*;

public class WaterCommand implements InteractionCommand {
    private final Vector3i targetBlock;
    private final SimpleBlockWorld world;
    private final FarmlandMoistureState moisture;

    public WaterCommand(Vector3i targetBlock, SimpleBlockWorld world, FarmlandMoistureState moisture) {
        this.targetBlock = targetBlock;
        this.world = world;
        this.moisture = moisture;
    }

    @Override
    public boolean execute() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (type == BlockType.PLOWED_DRY) {
            world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_WET);
            moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
            System.out.println("Watered soil at: " + targetBlock);
            return true;
        } else if (type == BlockType.PLOWED_WET) {
            moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
            System.out.println("Refreshed moisture at: " + targetBlock);
            return true;
        }
        return false;
    }

    @Override
    public Vector3f getTargetPosition() {
        return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
    }

    @Override
    public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return ActionRange.isWithinRange(playerPos, targetBlock);
    }

    @Override
    public String getDescription() {
        return "Water at " + targetBlock;
    }

    @Override
    public boolean isValidTarget() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        return type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET;
    }
}
