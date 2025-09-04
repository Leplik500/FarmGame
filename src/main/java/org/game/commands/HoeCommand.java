package org.game.commands;

import com.jme3.math.Vector3f;
import org.game.*;

public class HoeCommand implements InteractionCommand {
    private final Vector3i targetBlock;
    private final SimpleBlockWorld world;

    public HoeCommand(Vector3i targetBlock, SimpleBlockWorld world) {
        this.targetBlock = targetBlock;
        this.world = world;
    }

    @Override
    public boolean execute() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (type == BlockType.GRASS) {
            world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
            System.out.println("Tilled soil at: " + targetBlock);
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
        return "Hoe at " + targetBlock;
    }

    @Override
    public boolean isValidTarget() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        return type == BlockType.GRASS;
    }
}