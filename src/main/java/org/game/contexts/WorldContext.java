package org.game.contexts;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial;
import org.game.states.DayNightCycle;
import org.game.states.FarmlandMoistureState;
import org.game.states.PlantGrowthState;
import org.game.systems.InventoryManager;
import org.game.world.BlockWorld;
import org.game.world.PlantFactory;

public record WorldContext(BlockWorld world, PlantGrowthState growth,
                           FarmlandMoistureState moisture,
                           PlantFactory plantFactory,
                           InventoryManager inventoryManager, Spatial shopModel,
                           Spatial houseModel, DayNightCycle dayNightCycle,
                           AssetManager assetManager) {
}
