package org.game;

import com.jme3.asset.AssetManager;
import org.game.commands.*;

public class InteractionCommandFactory {
    private final SimpleBlockWorld world;
    private final PlantGrowthState growth;
    private final PlantFactory plantFactory;
    private final FarmlandMoistureState moisture;
    private final InventoryManager inventoryManager;
    private final AssetManager assetManager;

    public InteractionCommandFactory(SimpleBlockWorld world, PlantGrowthState growth,
                                     PlantFactory plantFactory, FarmlandMoistureState moisture,
                                     InventoryManager inventoryManager, AssetManager assetManager) {
        this.world = world;
        this.growth = growth;
        this.plantFactory = plantFactory;
        this.moisture = moisture;
        this.inventoryManager = inventoryManager;
        this.assetManager = assetManager;
    }

    public InteractionCommand createItemCommand(String itemId, Vector3i targetBlock) {
        return switch (itemId) {
            case ItemIds.PUMPKIN_SEEDS -> new PlantSeedCommand(
                    PlantKind.PUMPKIN, targetBlock, world, growth, plantFactory, inventoryManager);
            case ItemIds.TOMATO_SEEDS -> new PlantSeedCommand(
                    PlantKind.TOMATO, targetBlock, world, growth, plantFactory, inventoryManager);
            case ItemIds.HOE -> new HoeCommand(targetBlock, world);
            case ItemIds.WATERING_CAN -> new WaterCommand(targetBlock, world, moisture);
            default -> null;
        };
    }

    public InteractionCommand createHarvestCommand(Vector3i targetBlock) {
        return new HarvestCommand(targetBlock, growth, assetManager, inventoryManager);
    }
}
