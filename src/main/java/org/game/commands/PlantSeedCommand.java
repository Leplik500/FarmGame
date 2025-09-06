package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.*;

public class PlantSeedCommand implements InteractionCommand {
    private final PlantKind plantKind;
    private final Vector3i targetBlock;
    private final SimpleBlockWorld world;
    private final PlantGrowthState growth;
    private final PlantFactory plantFactory;
    private final InventoryManager inventoryManager;

    public PlantSeedCommand(PlantKind plantKind, Vector3i targetBlock, SimpleBlockWorld world,
                            PlantGrowthState growth, PlantFactory plantFactory,
                            InventoryManager inventoryManager) {
        {
            this.plantKind = plantKind;
            this.targetBlock = targetBlock;
            this.world = world;
            this.growth = growth;
            this.plantFactory = plantFactory;
            this.inventoryManager = inventoryManager;
        }
    }

    @Override
    public boolean execute() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
            Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
            if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                    && growth.HasNotPlantAt(aboveBlock)) {
                Spatial s = plantFactory.createPlant(plantKind, targetBlock);
                growth.registerPlanted(plantKind, targetBlock, aboveBlock, s);

                return inventoryManager.tryRemoveItem(getPlantSeedId(), 1);
            }
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
        return "Plant " + plantKind + " at " + targetBlock;
    }

    @Override
    public boolean isValidTarget() {
        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (type != BlockType.PLOWED_DRY && type != BlockType.PLOWED_WET) {
            return false;
        }
        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
        return world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                && growth.HasNotPlantAt(aboveBlock);
    }

    private String getPlantSeedId() {
        return switch (plantKind) {
            case PUMPKIN -> ItemIds.PUMPKIN_SEEDS;
            case TOMATO -> ItemIds.TOMATO_SEEDS;
        };
    }
}
