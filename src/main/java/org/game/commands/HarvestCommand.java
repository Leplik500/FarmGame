package org.game.commands;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.*;
import org.game.events.SimpleEventBus;

public class HarvestCommand implements InteractionCommand {
    private final Vector3i targetBlock;
    private final PlantGrowthState growth;
    private final AssetManager assetManager;
    private final Hotbar hotbar;

    public HarvestCommand(Vector3i targetBlock, PlantGrowthState growth, AssetManager assetManager, Hotbar hotbar) {
        this.targetBlock = targetBlock;
        this.growth = growth;
        this.assetManager = assetManager;
        this.hotbar = hotbar;
    }

    @Override
    public boolean execute() {
        PlantGrowthState.Plant plant = growth.getPlantAt(targetBlock);
        if (plant == null || plant.stageIndex != 3) return false;

        if (plant.kind == PlantKind.PUMPKIN) {
            addToInventory("pumpkin", 1);
            System.out.println("Harvested 1 pumpkin!");
        } else if (plant.kind == PlantKind.TOMATO) {
            int count = 1 + (int)(Math.random() * 4);
            addToInventory("tomato", count);
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

    private void addToInventory(String itemId, int count) {
        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing != null && existing.id().equals(itemId)) {
                HotbarItem updated = existing.withCount(existing.count() + count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                return;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (hotbar.getSlotItem(i) == null) {
                String iconPath = switch (itemId) {
                    case "pumpkin" -> GameConfig.PUMPKIN_ITEM;
                    case "tomato" -> GameConfig.TOMATO_ITEM;
                    default -> GameConfig.DEFAULT_ITEM;
                };
                HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, newItem);
                return;
            }
        }

        System.out.println("Inventory full! Couldn't pick up " + count + " " + itemId);
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
