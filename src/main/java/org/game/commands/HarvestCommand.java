package org.game.commands;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.core.InteractionCommand;
import org.game.states.PlantGrowthState;
import org.game.systems.InventoryManager;
import org.game.utils.ActionRange;
import org.game.utils.GameConfig;
import org.game.utils.ItemIds;
import org.game.world.PlantKind;
import org.game.world.Vector3i;

public class HarvestCommand implements InteractionCommand {
    private final Vector3i targetBlock;
    private final PlantGrowthState growth;
    private final AssetManager assetManager;
    private final InventoryManager inventoryManager;
    
    public HarvestCommand(Vector3i targetBlock, PlantGrowthState growth,
                          AssetManager assetManager, InventoryManager inventoryManager) {
        this.targetBlock = targetBlock;
        this.growth = growth;
        this.assetManager = assetManager;
        this.inventoryManager = inventoryManager;
    }

    @Override
    public boolean execute() {
        PlantGrowthState.Plant plant = growth.getPlantAt(targetBlock);
        if (plant == null || plant.stageIndex != 3) return false;

        if (plant.kind == PlantKind.PUMPKIN) {
            if (!inventoryManager.tryAddItem(ItemIds.PUMPKIN, 1)) {
                System.out.println("Inventory full! Can't harvest pumpkin!");
                return false;
            }
            System.out.println("Harvested 1 pumpkin!");
        } else if (plant.kind == PlantKind.TOMATO) {
            int count = 1 + (int)(Math.random() * 4);
            if (!inventoryManager.tryAddItem(ItemIds.TOMATO, count)) {
                System.out.println("Inventory full! Can't harvest tomatoes!");
                return false;
            }
            System.out.println("Harvested " + count + " tomatoes!");
        }

        plant.stageIndex = 4;
        plant.timeLeft = GameConfig.FRUIT_REGROW_SECONDS;

        String harvestedModelPath = switch (plant.kind) {
            case PUMPKIN -> GameConfig.PUMPKIN_HARVESTED_MODEL;
            case TOMATO -> GameConfig.TOMATO_HARVESTED_MODEL;
        };

        Spatial harvestedModel = assetManager.loadModel(harvestedModelPath);
        harvestedModel.setLocalTranslation(plant.spatial.getLocalTranslation());
        harvestedModel.setLocalRotation(plant.spatial.getLocalRotation());
        harvestedModel.setLocalScale(plant.spatial.getLocalScale());

        if (plant.spatial.getParent() != null) {
            plant.spatial.removeFromParent();
        }
        growth.getPlantsRoot().attachChild(harvestedModel);
        plant.spatial = harvestedModel;

        return true;
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
        return "Harvest at " + targetBlock;
    }

    @Override
    public boolean isValidTarget() {
        PlantGrowthState.Plant plant = growth.getPlantAt(targetBlock);
        return plant != null && plant.stageIndex == 3;
    }
}
