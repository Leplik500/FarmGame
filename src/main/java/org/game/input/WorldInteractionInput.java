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
    private final SimpleBlockWorld world;
    private final Hotbar hotbar;
    private final Camera camera;
    private final InputManager inputManager;
    private final FarmlandMoistureState moisture;
    private final AssetManager assetManager;
    private final PlantGrowthState growth;
    private final Spatial shopModel;
    private final ShopUI shopUI;
    private final PlantFactory plantFactory;
    private final DayNightCycle dayNightCycle;
    private final Spatial houseModel;
    private final Spatial player;
    private final PlayerMovementController movementController;
    

    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture,
                                 AssetManager assetManager,
                                 PlantGrowthState growth, Spatial shopModel,
                                 ShopUI shopUI, PlantFactory plantFactory,
                                 Spatial houseModel,
                                 DayNightCycle dayNightCycle, Spatial player,
                                 PlayerMovementController movementController
                                 ) {
        this.world = world;
        this.hotbar = hotbar;
        this.camera = camera;
        this.inputManager = inputManager;
        this.moisture = moisture;
        this.assetManager = assetManager;
        this.growth = growth;
        this.shopModel = shopModel;
        this.shopUI = shopUI;
        this.plantFactory = plantFactory;
        this.houseModel = houseModel;
        this.dayNightCycle = dayNightCycle;
        this.player = player;
        this.movementController = movementController;
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
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(0.1f);
        for (int i = 0; i < 1000; i++) {
            currentPos.addLocal(step);
            int bx = (int)Math.floor(currentPos.x);
            int by = (int)Math.floor(currentPos.y);
            int bz = (int)Math.floor(currentPos.z);
            if (world.getBlock(bx, by, bz) != BlockType.AIR) {
                return new Vector3i(bx, by, bz);
            }
        }
        return null;
    }


    private Vector3f getPlayerPosition() {
        return player.getWorldTranslation();
    }

    private InteractionCommand createCommandForItem(HotbarItem selectedItem, Vector3i targetBlock) {
        return switch (selectedItem.id()) {
            case "pumpkin_seeds" -> new PlantSeedCommand(
                    PlantKind.PUMPKIN, targetBlock, world, growth, plantFactory, hotbar);
            case "tomato_seeds" -> new PlantSeedCommand(
                    PlantKind.TOMATO, targetBlock, world, growth, plantFactory, hotbar);
            case "hoe" -> new HoeCommand(targetBlock, world);
            case "watering_can" -> new WaterCommand(targetBlock, world, moisture);
            default -> null;
        };
        
    }
    private Spatial getClickedObject() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(0.1f);

        for (int i = 0; i < 1000; i++) {
            currentPos.addLocal(step);

            // Проверяем попадание в магазин
            if (isPositionInObject(currentPos, shopModel)) {
                return shopModel;
            }

            // Проверяем попадание в дом
            if (isPositionInObject(currentPos, houseModel)) {
                return houseModel;
            }

            // Если попали в блок, прекращаем поиск
            int bx = (int)Math.floor(currentPos.x);
            int by = (int)Math.floor(currentPos.y);
            int bz = (int)Math.floor(currentPos.z);
            if (world.getBlock(bx, by, bz) != BlockType.AIR) {
                break;
            }
        }
        return null;
    }

    private boolean isPositionInObject(Vector3f pos, Spatial object) {
        Vector3f objectPos = object.getWorldTranslation();
        float scale = object.getWorldScale().x;
        float radius = 2.0f * scale; 

        return pos.distance(objectPos) <= radius;
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
            InteractionCommand harvestCommand = new HarvestCommand(aboveBlock, growth, assetManager, hotbar);
            executeOrQueueCommandWithValidation(harvestCommand, playerPos);
            return;
        }
        if (growth.getPlantAt(targetBlock) != null && growth.getPlantAt(targetBlock).stageIndex == 3) {
            InteractionCommand harvestCommand = new HarvestCommand(targetBlock, growth, assetManager, hotbar);
            executeOrQueueCommandWithValidation(harvestCommand, playerPos);
        }
    }

}
