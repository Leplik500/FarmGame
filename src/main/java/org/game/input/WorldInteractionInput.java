package org.game.input;

import com.jme3.asset.AssetManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.game.*;

public class WorldInteractionInput implements ActionListener {
    private final SimpleBlockWorld world;
    private final Hotbar hotbar;
    private final Camera camera;
    private final InputManager inputManager;
    private final FarmlandMoistureState moisture;
    private final AssetManager assetManager;
    private final Node plantsRoot;
    private final PlantGrowthState growth;


    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture,
                                 AssetManager assetManager, Node plantsRoot, PlantGrowthState growth) {
        this.world = world;
        this.hotbar = hotbar;
        this.camera = camera;
        this.inputManager = inputManager;
        this.moisture = moisture;
        this.assetManager = assetManager;
        this.plantsRoot = plantsRoot;
        this.growth = growth;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (InputNames.WORLD_INTERACT.equals(name)) {
            handleWorldClick();
        }
    }

    private void handleWorldClick() {
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
                    Spatial s = plantPumpkinSapling(targetBlock);
                    growth.registerPlanted(PlantKind.PUMPKIN, targetBlock, aboveBlock, s);
                    hotbar.reduceItemCount(hotbar.getSelectedSlot(), 1);
                }
            }
            return;
        }

        if ("tomato_seeds".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                        && !growth.hasPlantAt(aboveBlock)) {
                    Spatial s = plantTomatoSapling(targetBlock);
                    growth.registerPlanted(PlantKind.TOMATO, targetBlock, aboveBlock, s);
                    hotbar.reduceItemCount(hotbar.getSelectedSlot(), 1);
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
    
    private Spatial plantPumpkinSapling(Vector3i soil) {
        Spatial sapling =
                assetManager.loadModel(GameConfig.PUMPKIN_GROWTH_MODELS[0]); 
        float yTop = soil.y() + 0.5f;
        sapling.setLocalTranslation(new Vector3f(soil.x(), yTop, soil.z()));
        sapling.scale(3f);
        plantsRoot.attachChild(sapling); 
        System.out.println("Planted pumpkin sapling at: " + soil + " (top@" + yTop + ")");
        return sapling;
    }

    private Spatial plantTomatoSapling(Vector3i soil) {
        Spatial sapling =
                assetManager.loadModel(GameConfig.TOMATO_GROWTH_MODELS[0]);
        float yTop = soil.y() + 0.5f;
        sapling.setLocalTranslation(new Vector3f(soil.x(), yTop, soil.z()));
        sapling.scale(3f);
        plantsRoot.attachChild(sapling); 
        System.out.println("Planted tomato sapling at: " + soil + " (top@" + yTop + ")");
        return sapling;
    }

    private boolean harvestPlantAt(Vector3i position) {
        PlantGrowthState.Plant plant = growth.getPlantAt(position);
        if (plant == null) return false;
        if (plant.stageIndex != 3) return false; 

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

        growth.removePlant(position);

        if (plant.kind == PlantKind.PUMPKIN) {
            addToInventory("pumpkin", 1);
            System.out.println("Harvested 1 pumpkin!");
        } else if (plant.kind == PlantKind.TOMATO) {
            int count = 1 + (int)(Math.random() * 4); 
            addToInventory("tomato", count);
            System.out.println("Harvested " + count + " tomatoes!");
        }

        return true;
    }

    private void addToInventory(String itemId, int count) {
        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing != null && existing.id().equals(itemId)) {
                HotbarItem updated = existing.withCount(existing.count() + count);
                hotbar.setItem(i, updated);
                return;
            }
        }

        for (int i = 0; i < 9; i++) {
            HotbarItem existing = hotbar.getSlotItem(i);
            if (existing == null) {
                String iconPath = getIconPath(itemId);
                hotbar.setItem(i, new HotbarItem(itemId, iconPath, count));
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

}
