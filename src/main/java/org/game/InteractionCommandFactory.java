package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.commands.*;
import org.game.services.WorldService;
import org.game.services.PlayerService;

public class InteractionCommandFactory {
    private final WorldService worldService;
    private final PlayerService playerService;
    private final Hotbar hotbar;
    private final ShopUI shopUI;

    public InteractionCommandFactory(WorldService worldService, PlayerService playerService,
                                     Hotbar hotbar, ShopUI shopUI) {
        this.worldService = worldService;
        this.playerService = playerService;
        this.hotbar = hotbar;
        this.shopUI = shopUI;
    }

    public InteractionCommand createCommand(Vector3i targetBlock, Spatial clickedObject) {
        Vector3f playerPos = playerService.getPlayer().getWorldTranslation();

        // Handle object interactions first
        if (clickedObject != null) {
            return createObjectCommand(clickedObject, playerPos);
        }

        // Handle block interactions
        return createBlockCommand(targetBlock, playerPos);
    }

    private InteractionCommand createObjectCommand(Spatial clickedObject, Vector3f playerPos) {
        if (clickedObject == worldService.getShopModel()) {
            ShopCommand shopCommand = new ShopCommand(worldService.getShopModel(),
                    shopUI, worldService.getDayNightCycle());
            return shouldAutoMove(shopCommand, playerPos) ?
                    new AutoMoveCommand(shopCommand, playerService.getMovementController()) :
                    shopCommand;
        }

        if (clickedObject == worldService.getHouseModel()) {
            HouseCommand houseCommand = new HouseCommand(worldService.getHouseModel(),
                    worldService.getDayNightCycle());
            return shouldAutoMove(houseCommand, playerPos) ?
                    new AutoMoveCommand(houseCommand, playerService.getMovementController()) :
                    houseCommand;
        }

        return null;
    }

    private InteractionCommand createBlockCommand(Vector3i targetBlock, Vector3f playerPos) {
        HotbarItem selectedItem = hotbar.getSelectedItem();
        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());

        // Handle harvest attempts (no selected item)
        if (selectedItem == null) {
            return createHarvestCommandIfPossible(aboveBlock, targetBlock, playerPos);
        }

        // Handle item-based commands
        InteractionCommand command = createItemCommand(selectedItem.id(), targetBlock);
        if (command != null && shouldAutoMove(command, playerPos)) {
            return new AutoMoveCommand(command, playerService.getMovementController());
        }

        return command;
    }

    private InteractionCommand createHarvestCommandIfPossible(Vector3i aboveBlock, Vector3i targetBlock, Vector3f playerPos) {
        // Check above block first
        if (worldService.getGrowth().getPlantAt(aboveBlock) != null &&
                worldService.getGrowth().getPlantAt(aboveBlock).stageIndex == 3) {

            HarvestCommand harvestCommand = new HarvestCommand(aboveBlock, worldService.getGrowth(),
                    worldService.getAssetManager(),
                    worldService.getInventoryManager());
            return shouldAutoMove(harvestCommand, playerPos) ?
                    new AutoMoveCommand(harvestCommand, playerService.getMovementController()) :
                    harvestCommand;
        }

        // Check target block
        if (worldService.getGrowth().getPlantAt(targetBlock) != null &&
                worldService.getGrowth().getPlantAt(targetBlock).stageIndex == 3) {

            HarvestCommand harvestCommand = new HarvestCommand(targetBlock, worldService.getGrowth(),
                    worldService.getAssetManager(),
                    worldService.getInventoryManager());
            return shouldAutoMove(harvestCommand, playerPos) ?
                    new AutoMoveCommand(harvestCommand, playerService.getMovementController()) :
                    harvestCommand;
        }

        return null;
    }

    public InteractionCommand createItemCommand(String itemId, Vector3i targetBlock) {
        return switch (itemId) {
            case ItemIds.PUMPKIN_SEEDS -> new PlantSeedCommand(
                    PlantKind.PUMPKIN, targetBlock, worldService.getWorld(),
                    worldService.getGrowth(), worldService.getPlantFactory(),
                    worldService.getInventoryManager());
            case ItemIds.TOMATO_SEEDS -> new PlantSeedCommand(
                    PlantKind.TOMATO, targetBlock, worldService.getWorld(),
                    worldService.getGrowth(), worldService.getPlantFactory(),
                    worldService.getInventoryManager());
            case ItemIds.HOE -> new HoeCommand(targetBlock, worldService.getWorld());
            case ItemIds.WATERING_CAN -> new WaterCommand(targetBlock, worldService.getWorld(),
                    worldService.getMoisture());
            default -> null;
        };
    }

    public InteractionCommand createHarvestCommand(Vector3i targetBlock) {
        return new HarvestCommand(targetBlock, worldService.getGrowth(),
                worldService.getAssetManager(), worldService.getInventoryManager());
    }

    private boolean shouldAutoMove(InteractionCommand command, Vector3f playerPos) {
        return !command.canExecuteAtCurrentPosition(playerPos);
    }
}
