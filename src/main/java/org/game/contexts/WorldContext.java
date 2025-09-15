package org.game.contexts;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import org.game.*;

public record WorldContext(BlockWorld world, PlantGrowthState growth,
                           FarmlandMoistureState moisture,
                           PlantFactory plantFactory,
                           InventoryManager inventoryManager, Spatial shopModel,
                           Spatial houseModel, DayNightCycle dayNightCycle,
                           AssetManager assetManager) {
}
