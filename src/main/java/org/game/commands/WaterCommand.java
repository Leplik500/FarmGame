package org.game.commands;

import org.game.*;

public class WaterCommand extends AbstractBlockCommand {
    private final FarmlandMoistureState moisture;

    public WaterCommand(Vector3i targetBlock, BlockWorld world, FarmlandMoistureState moisture) {
        super(targetBlock, world);
        this.moisture = moisture;
    }

    @Override
    protected boolean performAction() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());

        if (type == BlockType.PLOWED_DRY) {
            world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_WET);
            moisture.markWet(targetBlock, null);
            return true;
        } else if (type == BlockType.PLOWED_WET) {
            moisture.markWet(targetBlock, null);
            return true;
        }
        return false;
    }

    @Override
    protected void logSuccess() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (type == BlockType.PLOWED_WET) {
            System.out.println("Watered soil at: " + targetBlock);
        } else {
            System.out.println("Refreshed moisture at: " + targetBlock);
        }
    }

    @Override
    protected boolean isTargetBlockTypeValid(int blockType) {
        return blockType == BlockType.PLOWED_DRY || blockType == BlockType.PLOWED_WET;
    }

    @Override
    public String getDescription() {
        return "Water at " + targetBlock;
    }
}
