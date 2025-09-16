package org.game.core;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.commands.*;
import org.game.contexts.WorldContext;
import org.game.contexts.PlayerContext;
import org.game.ui.Hotbar;
import org.game.ui.HotbarItem;
import org.game.ui.ShopUI;
import org.game.utils.ItemIds;
import org.game.world.PlantKind;
import org.game.world.Vector3i;

public class InteractionCommandFactory {
    private final WorldContext worldContext;
    private final PlayerContext playerContext;
    private final Hotbar hotbar;
    private final ShopUI shopUI;

    public InteractionCommandFactory(WorldContext worldContext, PlayerContext playerContext,
                                     Hotbar hotbar, ShopUI shopUI) {
        this.worldContext = worldContext;
        this.playerContext = playerContext;
        this.hotbar = hotbar;
        this.shopUI = shopUI;
    }

    public InteractionCommand createCommand(Vector3i targetBlock, Spatial clickedObject) {
        Vector3f playerPos = playerContext.player().getWorldTranslation();

        if (clickedObject != null) {
            return createObjectCommand(clickedObject, playerPos);
        }

        return createBlockCommand(targetBlock, playerPos);
    }

    private InteractionCommand createObjectCommand(Spatial clickedObject, Vector3f playerPos) {
        if (clickedObject == worldContext.shopModel()) {
            ShopCommand shopCommand = new ShopCommand(worldContext.shopModel(),
                    shopUI, worldContext.dayNightCycle());
            return shouldAutoMove(shopCommand, playerPos) ?
                    new AutoMoveCommand(shopCommand, playerContext.movementController()) :
                    shopCommand;
        }

        if (clickedObject == worldContext.houseModel()) {
            HouseCommand houseCommand = new HouseCommand(worldContext.houseModel(),
                    worldContext.dayNightCycle());
            return shouldAutoMove(houseCommand, playerPos) ?
                    new AutoMoveCommand(houseCommand, playerContext.movementController()) :
                    houseCommand;
        }

        return null;
    }

    private InteractionCommand createBlockCommand(Vector3i targetBlock, Vector3f playerPos) {
        HotbarItem selectedItem = hotbar.getSelectedItem();
        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());

        if (selectedItem == null) {
            return createHarvestCommandIfPossible(aboveBlock, targetBlock, playerPos);
        }

        InteractionCommand command = createItemCommand(selectedItem.id(), targetBlock);
        if (command != null && shouldAutoMove(command, playerPos)) {
            return new AutoMoveCommand(command, playerContext.movementController());
        }

        return command;
    }

    private InteractionCommand createHarvestCommandIfPossible(Vector3i aboveBlock, Vector3i targetBlock, Vector3f playerPos) {
        if (worldContext.growth().getPlantAt(aboveBlock) != null &&
                worldContext.growth().getPlantAt(aboveBlock).stageIndex == 3) {

            HarvestCommand harvestCommand = new HarvestCommand(aboveBlock, worldContext.growth(),
                    worldContext.assetManager(),
                    worldContext.inventoryManager());
            return shouldAutoMove(harvestCommand, playerPos) ?
                    new AutoMoveCommand(harvestCommand, playerContext.movementController()) :
                    harvestCommand;
        }

        if (worldContext.growth().getPlantAt(targetBlock) != null &&
                worldContext.growth().getPlantAt(targetBlock).stageIndex == 3) {

            HarvestCommand harvestCommand = new HarvestCommand(targetBlock, worldContext.growth(),
                    worldContext.assetManager(),
                    worldContext.inventoryManager());
            return shouldAutoMove(harvestCommand, playerPos) ?
                    new AutoMoveCommand(harvestCommand, playerContext.movementController()) :
                    harvestCommand;
        }

        return null;
    }

    public InteractionCommand createItemCommand(String itemId, Vector3i targetBlock) {
        return switch (itemId) {
            case ItemIds.PUMPKIN_SEEDS -> new PlantSeedCommand(
                    PlantKind.PUMPKIN, targetBlock, worldContext.world(),
                    worldContext.growth(), worldContext.plantFactory(),
                    worldContext.inventoryManager());
            case ItemIds.TOMATO_SEEDS -> new PlantSeedCommand(
                    PlantKind.TOMATO, targetBlock, worldContext.world(),
                    worldContext.growth(), worldContext.plantFactory(),
                    worldContext.inventoryManager());
            case ItemIds.HOE -> new HoeCommand(targetBlock, worldContext.world());
            case ItemIds.WATERING_CAN -> new WaterCommand(targetBlock, worldContext.world(),
                    worldContext.moisture());
            default -> null;
        };
    }

    private boolean shouldAutoMove(InteractionCommand command, Vector3f playerPos) {
        return !command.canExecuteAtCurrentPosition(playerPos);
    }
}
