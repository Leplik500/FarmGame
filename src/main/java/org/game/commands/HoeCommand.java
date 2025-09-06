package org.game.commands;

import org.game.*;

public class HoeCommand extends AbstractBlockCommand {

    public HoeCommand(Vector3i targetBlock, SimpleBlockWorld world) {
        super(targetBlock, world);
    }

    @Override
    protected boolean performAction() {
        world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
        return true;
    }

    @Override
    protected void logSuccess() {
        System.out.println("Tilled soil at: " + targetBlock);
    }

    @Override
    protected boolean isTargetBlockTypeValid(int blockType) {
        return blockType == BlockType.GRASS;
    }

    @Override
    public String getDescription() {
        return "Hoe at " + targetBlock;
    }
}
