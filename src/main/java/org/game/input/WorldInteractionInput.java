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

    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture,
                                 AssetManager assetManager,
                                 PlantGrowthState growth, Spatial shopModel,
                                 ShopUI shopUI, PlantFactory plantFactory) {
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
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (InputNames.WORLD_INTERACT.equals(name)) {
            handleWorldClick();
        }
    }

    private void handleWorldClick() {
        if (shopUI.isVisible()) {
            Vector2f cursorPos = inputManager.getCursorPosition();
            shopUI.handleClick(cursorPos.x, cursorPos.y);
            return;
        }

        if (isClickOnShop()) {
            shopUI.setVisible(true);
            return;
        }
        
        HotbarItem selectedItem = hotbar.getSelectedItem();

        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return;

        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
        if (selectedItem == null) {
            if (harvestPlantAt(aboveBlock)) {
                return;
            }
            if (harvestPlantAt(targetBlock)) {
                return;
            }
            return;
        }
        

        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());

        if ("pumpkin_seeds".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                        && !growth.hasPlantAt(aboveBlock)) {
                    Spatial s = plantFactory.createPlant(PlantKind.PUMPKIN, targetBlock);
                    growth.registerPlanted(PlantKind.PUMPKIN, targetBlock, aboveBlock, s);
                    reduceSelectedItemCount();
                }
            }
            return;
        }

        if ("tomato_seeds".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                        && !growth.hasPlantAt(aboveBlock)) {
                    Spatial s = plantFactory.createPlant(PlantKind.TOMATO, targetBlock);
                    growth.registerPlanted(PlantKind.TOMATO, targetBlock, aboveBlock, s);
                    reduceSelectedItemCount();
                }
            }
            return;
        }

        if ("hoe".equals(selectedItem.id())) {
            if (type == BlockType.GRASS) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
                System.out.println("Tilled soil at: " + targetBlock);
            }
            return;
        }

        if ("watering_can".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_WET);
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Watered soil at: " + targetBlock);
            } else if (type == BlockType.PLOWED_WET) {
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Refreshed moisture at: " + targetBlock);
            }
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


    private boolean isClickOnShop() {
        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return false;

        Vector3f shopPos = shopModel.getWorldTranslation();
        float distance = new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z())
                .distance(shopPos);

        return distance < 5.0f; // 5 unit radius around shop
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
}
