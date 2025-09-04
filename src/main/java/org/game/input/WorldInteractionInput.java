package org.game.input;

import com.jme3.asset.AssetManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.input.InputManager;
import com.jme3.scene.Spatial;
import org.game.*;
import org.game.events.SimpleEventBus;

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

        if (shopUI.isVisible()) {
            Vector2f cursorPos = inputManager.getCursorPosition();
            shopUI.handleClick(cursorPos.x, cursorPos.y);
            return;
        }

        Spatial clickedObject = getClickedObject();

        if (clickedObject == shopModel) {
            InteractionCommand shopCommand = new InteractionCommands.ShopCommand(shopModel, shopUI);
            executeOrQueueCommandWithValidation(shopCommand, playerPos); // ИЗМЕНЕНО: добавлена валидация
            return;
        }

        if (clickedObject == houseModel) {
            InteractionCommand houseCommand = new InteractionCommands.HouseCommand(houseModel, dayNightCycle);
            executeOrQueueCommandWithValidation(houseCommand, playerPos); // ИЗМЕНЕНО: добавлена валидация
            return;
        }

        HotbarItem selectedItem = hotbar.getSelectedItem();
        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return;

        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());

        if (selectedItem == null) {
            if (growth.getPlantAt(aboveBlock) != null && growth.getPlantAt(aboveBlock).stageIndex == 3) {
                InteractionCommand harvestCommand = new InteractionCommands.HarvestCommand(
                        aboveBlock, growth, assetManager, hotbar);
                executeOrQueueCommandWithValidation(harvestCommand, playerPos);
                return;
            }
            if (growth.getPlantAt(targetBlock) != null && growth.getPlantAt(targetBlock).stageIndex == 3) {
                InteractionCommand harvestCommand = new InteractionCommands.HarvestCommand(
                        targetBlock, growth, assetManager, hotbar);
                executeOrQueueCommandWithValidation(harvestCommand, playerPos);
                return;
            }
            return;
        }

        InteractionCommand command = createCommandForItem(selectedItem, targetBlock);
        if (command != null) {
            executeOrQueueCommandWithValidation(command, playerPos);
        }
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

    private boolean harvestPlantAt(Vector3i position) {
        PlantGrowthState.Plant plant = growth.getPlantAt(position);
        if (plant == null) return false;
        if (plant.stageIndex != 3) return false;

        if (plant.kind == PlantKind.PUMPKIN) {
            addToInventoryViaEvents("pumpkin", 1);
            System.out.println("Harvested 1 pumpkin!");
        } else if (plant.kind == PlantKind.TOMATO) {
            int count = 1 + (int)(Math.random() * 4);
            addToInventoryViaEvents("tomato", count);
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


    private void addToInventoryViaEvents(String itemId, int count) {
        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing != null && existing.id().equals(itemId)) {
                HotbarItem updated = existing.withCount(existing.count() + count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                return;
            }
        }

        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing == null) {
                String iconPath = getIconPath(itemId);
                HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                SimpleEventBus.INSTANCE.publishInventoryChanged(i, newItem);
                return;
            }
        }

        System.out.println("Inventory full! Couldn't pick up " + count + " " + itemId);
    }

    private String getIconPath(String itemId) {
        return switch (itemId) {
            case "pumpkin" -> GameConfig.PUMPKIN_ITEM;
            case "tomato" -> GameConfig.TOMATO_ITEM;
            default -> GameConfig.DEFAULT_ITEM;
        };
    }


    private boolean isClickOnShop(Vector3f playerPos) {
        Vector3f shopPos = shopModel.getWorldTranslation();
        return ActionRange.isWithinRange(playerPos, shopPos);
    }

    private boolean isClickOnHouse(Vector3f playerPos) {
        Vector3f housePos = houseModel.getWorldTranslation();
        return ActionRange.isWithinRange(playerPos, housePos);
    }



    private void handleHouseClick() {
        if (dayNightCycle.isNight()) {
            dayNightCycle.skipToDay();
            System.out.println("You slept through the night. Good morning!");
        } else {
            System.out.println("You can only sleep at night.");
        }
    }


    private void reduceSelectedItemCount() {
        int selectedSlot = hotbar.getSelectedSlot();
        HotbarItem currentItem = hotbar.getSlotItem(selectedSlot);

        if (currentItem != null && currentItem.count() >= 1) {
            int newCount = currentItem.count() - 1;
            if (newCount <= 0) {
                SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, null);
            } else {
                HotbarItem updatedItem = currentItem.withCount(newCount);
                SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, updatedItem);
            }
        }
    }

    private Vector3f getPlayerPosition() {
        return player.getWorldTranslation();
    }

    private void executeOrQueueCommand(InteractionCommand command, Vector3f playerPos) {
        if (command.canExecuteAtCurrentPosition(playerPos)) {
            command.execute();
        } else {
            movementController.getAutoMovementController().startAutoMovement(command);
        }
    }
        
    private InteractionCommand createCommandForItem(HotbarItem selectedItem, Vector3i targetBlock) {
        return switch (selectedItem.id()) {
            case "pumpkin_seeds" -> new InteractionCommands.PlantSeedCommand(
                    PlantKind.PUMPKIN, targetBlock, world, growth, plantFactory, hotbar);
            case "tomato_seeds" -> new InteractionCommands.PlantSeedCommand(
                    PlantKind.TOMATO, targetBlock, world, growth, plantFactory, hotbar);
            case "hoe" -> new InteractionCommands.HoeCommand(targetBlock, world);
            case "watering_can" -> new InteractionCommands.WaterCommand(targetBlock, world, moisture);
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

}
