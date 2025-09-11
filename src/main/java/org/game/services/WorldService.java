package org.game.services;

import org.game.*;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;

public class WorldService {
    private final BlockWorld world;
    private final PlantGrowthState growth;
    private final FarmlandMoistureState moisture;
    private final PlantFactory plantFactory;
    private final InventoryManager inventoryManager;
    private final Spatial shopModel;
    private final Spatial houseModel;
    private final DayNightCycle dayNightCycle;
    private final AssetManager assetManager;

    public WorldService(BlockWorld world, PlantGrowthState growth,
                        FarmlandMoistureState moisture, PlantFactory plantFactory,
                        InventoryManager inventoryManager, Spatial shopModel,
                        Spatial houseModel, DayNightCycle dayNightCycle,
                        AssetManager assetManager) {
        this.world = world;
        this.growth = growth;
        this.moisture = moisture;
        this.plantFactory = plantFactory;
        this.inventoryManager = inventoryManager;
        this.shopModel = shopModel;
        this.houseModel = houseModel;
        this.dayNightCycle = dayNightCycle;
        this.assetManager = assetManager;
    }

    public BlockWorld getWorld() { return world; }
    public PlantGrowthState getGrowth() { return growth; }
    public FarmlandMoistureState getMoisture() { return moisture; }
    public PlantFactory getPlantFactory() { return plantFactory; }
    public InventoryManager getInventoryManager() { return inventoryManager; }
    public Spatial getShopModel() { return shopModel; }
    public Spatial getHouseModel() { return houseModel; }
    public DayNightCycle getDayNightCycle() { return dayNightCycle; }
    public AssetManager getAssetManager() { return assetManager; }
}
