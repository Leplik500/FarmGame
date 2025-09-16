package org.game.commands;

import org.game.world.BlockType;
import org.game.world.BlockWorld;
import org.game.world.Vector3i;

public class HoeCommand extends AbstractBlockCommand {

    public HoeCommand(Vector3i targetBlock, BlockWorld world) {
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
