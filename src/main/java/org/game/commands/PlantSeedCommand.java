package org.game.commands;

import com.jme3.scene.Spatial;
import org.game.*;

public class PlantSeedCommand extends AbstractBlockCommand {
    private final PlantKind plantKind;
    private final PlantGrowthState growth;
    private final PlantFactory plantFactory;
    private final InventoryManager inventoryManager;

    public PlantSeedCommand(PlantKind plantKind, Vector3i targetBlock, SimpleBlockWorld world,
                            PlantGrowthState growth, PlantFactory plantFactory,
                            InventoryManager inventoryManager) {
        super(targetBlock, world);
        this.plantKind = plantKind;
        this.growth = growth;
        this.plantFactory = plantFactory;
        this.inventoryManager = inventoryManager;
    }

    @Override
    public boolean isValidTarget() {
        if (!super.isValidTarget()) {
            return false;
        }

        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
        return world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                && growth.HasNotPlantAt(aboveBlock);
    }

    @Override
    protected boolean performAction() {
        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
        Spatial spatial = plantFactory.createPlant(plantKind, targetBlock);
        growth.registerPlanted(plantKind, targetBlock, aboveBlock, spatial);

        return inventoryManager.tryRemoveItem(getPlantSeedId(), 1);
    }

    @Override
    protected void logSuccess() {
        System.out.println("Planted " + plantKind + " seeds at: " + targetBlock);
    }

    @Override
    protected boolean isTargetBlockTypeValid(int blockType) {
        return blockType == BlockType.PLOWED_DRY || blockType == BlockType.PLOWED_WET;
    }

    @Override
    public String getDescription() {
        return "Plant " + plantKind + " at " + targetBlock;
    }

    private String getPlantSeedId() {
        return switch (plantKind) {
            case PUMPKIN -> ItemIds.PUMPKIN_SEEDS;
            case TOMATO -> ItemIds.TOMATO_SEEDS;
        };
    }
}
