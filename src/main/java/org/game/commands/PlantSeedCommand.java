package org.game.commands;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.*;
import org.game.events.SimpleEventBus;

public class PlantSeedCommand implements InteractionCommand {
    private final PlantKind plantKind;
    private final Vector3i targetBlock;
    private final SimpleBlockWorld world;
    private final PlantGrowthState growth;
    private final PlantFactory plantFactory;
    private final Hotbar hotbar;

    public PlantSeedCommand(PlantKind plantKind, Vector3i targetBlock, SimpleBlockWorld world,
                            PlantGrowthState growth, PlantFactory plantFactory, Hotbar hotbar) {
        this.plantKind = plantKind;
        this.targetBlock = targetBlock;
        this.world = world;
        this.growth = growth;
        this.plantFactory = plantFactory;
        this.hotbar = hotbar;
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

                int selectedSlot = hotbar.getSelectedSlot();
                HotbarItem currentItem = hotbar.getSlotItem(selectedSlot);
                if (currentItem != null && currentItem.count() >= 1) {
                    int newCount = currentItem.count() - 1;
                    if (newCount <= 0) {
                        SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, null);
                    } else {
                        HotbarItem updatedItem = currentItem.withCount(newCount);
                        SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, updatedItem);
                    }
                }
                return true;
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
}
