package org.game.input;

import com.jme3.asset.AssetManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.input.InputManager;
import com.jme3.scene.Spatial;
import org.game.*;
import org.game.commands.*;

public class WorldInteractionInput implements ActionListener {
    private final Hotbar hotbar;
    private final InputManager inputManager;
    private final PlantGrowthState growth;
    private final Spatial shopModel;
    private final ShopUI shopUI;
    private final DayNightCycle dayNightCycle;
    private final Spatial houseModel;
    private final Spatial player;
    private final PlayerMovementController movementController;
    private final InteractionCommandFactory commandFactory;
    private final RaycastHelper raycastHelper;

    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture,
                                 AssetManager assetManager,
                                 PlantGrowthState growth, Spatial shopModel,
                                 ShopUI shopUI, PlantFactory plantFactory,
                                 Spatial houseModel,
                                 DayNightCycle dayNightCycle, Spatial player,
                                 PlayerMovementController movementController,
                                 InventoryManager inventoryManager
                                 ) {
        this.hotbar = hotbar;
        this.inputManager = inputManager;
        this.growth = growth;
        this.shopModel = shopModel;
        this.shopUI = shopUI;
        this.houseModel = houseModel;
        this.dayNightCycle = dayNightCycle;
        this.player = player;
        this.movementController = movementController;
        this.raycastHelper = new RaycastHelper(camera, inputManager, world);
        this.commandFactory = new InteractionCommandFactory(
                world, growth, plantFactory, moisture, inventoryManager, assetManager);
    }


    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (InputNames.WORLD_INTERACT.equals(name)) {
            handleWorldClick();
        }
    }

    private void handleWorldClick() {
        Vector3f playerPos = getPlayerPosition();

        if (handleUIClick()) return;
        if (handleObjectClick(playerPos)) return;
        handleBlockClick(playerPos);
    }

    private Vector3i getBlockUnderCursor() {
        return raycastHelper.getBlockUnderCursor();
    }


    private Vector3f getPlayerPosition() {
        return player.getWorldTranslation();
    }

    private InteractionCommand createCommandForItem(HotbarItem selectedItem, Vector3i targetBlock) {
        return commandFactory.createItemCommand(selectedItem.id(), targetBlock);
    }

    private Spatial getClickedObject() {
        return raycastHelper.getClickedObject(shopModel, houseModel);
    }

    private void executeOrQueueCommandWithValidation(InteractionCommand command, Vector3f playerPos) {
        if (!command.isValidTarget()) {
            System.out.println("Cannot perform action: " + command.getDescription() + " - invalid target");
            return;
        }

        if (command.canExecuteAtCurrentPosition(playerPos)) {
            command.execute();
        } else {
            movementController.getAutoMovementController().startAutoMovement(command);
        }
    }

    private boolean handleUIClick() {
        if (shopUI.isVisible()) {
            Vector2f cursorPos = inputManager.getCursorPosition();
            shopUI.handleClick(cursorPos.x, cursorPos.y);
            return true;
        }
        return false;
    }

    private boolean handleObjectClick(Vector3f playerPos) {
        Spatial clickedObject = getClickedObject();
        if (clickedObject == shopModel) {
            executeOrQueueCommandWithValidation(new ShopCommand(shopModel, shopUI), playerPos);
            return true;
        }
        if (clickedObject == houseModel) {
            executeOrQueueCommandWithValidation(new HouseCommand(houseModel, dayNightCycle), playerPos);
            return true;
        }
        return false;
    }

    private void handleBlockClick(Vector3f playerPos) {
        HotbarItem selectedItem = hotbar.getSelectedItem();
        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return;

        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());

        if (selectedItem == null) {
            handleHarvestAttempt(playerPos, aboveBlock, targetBlock);
            return;
        }

        InteractionCommand command = createCommandForItem(selectedItem, targetBlock);
        if (command != null) {
            executeOrQueueCommandWithValidation(command, playerPos);
        }
    }

    private void handleHarvestAttempt(Vector3f playerPos, Vector3i aboveBlock, Vector3i targetBlock) {
        if (growth.getPlantAt(aboveBlock) != null && growth.getPlantAt(aboveBlock).stageIndex == 3) {
            InteractionCommand harvestCommand = commandFactory.createHarvestCommand(aboveBlock);
            executeOrQueueCommandWithValidation(harvestCommand, playerPos);
            return;
        }
        if (growth.getPlantAt(targetBlock) != null && growth.getPlantAt(targetBlock).stageIndex == 3) {
            InteractionCommand harvestCommand = commandFactory.createHarvestCommand(targetBlock);
            executeOrQueueCommandWithValidation(harvestCommand, playerPos);
        }
    }
}
