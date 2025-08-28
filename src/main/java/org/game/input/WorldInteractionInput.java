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

import java.util.HashSet;
import java.util.Set;

public class WorldInteractionInput implements ActionListener {
    private final SimpleBlockWorld world;
    private final Hotbar hotbar;
    private final Camera camera;
    private final InputManager inputManager;
    private final FarmlandMoistureState moisture;
    private final AssetManager assetManager;
    private final Node plantsRoot;
    private final Set<Vector3i> planted = new HashSet<>();


    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture,
                                 AssetManager assetManager, Node plantsRoot) {
        this.world = world;
        this.hotbar = hotbar;
        this.camera = camera;
        this.inputManager = inputManager;
        this.moisture = moisture;
        this.assetManager = assetManager;
        this.plantsRoot = plantsRoot;
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
        if (selectedItem == null) return;

        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return;

        int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());

        Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
        if ("apple_seeds".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR && !planted.contains(aboveBlock)) {
                    plantAppleSapling(aboveBlock, targetBlock);
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
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null); // дефолтный таймер
                System.out.println("Watered soil at: " + targetBlock);
            } else if (type == BlockType.PLOWED_WET) {
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Refreshed moisture at: " + targetBlock);
            }
        }

        if ("tomato_seeds".equals(selectedItem.id())) {
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR && !planted.contains(aboveBlock)) {
                    plantTomatoSapling(aboveBlock, targetBlock);
                }
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

    private void plantAppleSapling(Vector3i above, Vector3i soil) {
        Spatial sapling = assetManager.loadModel(GameConfig.MODEL_APPLE_SAPLING_S1);
        float yTop = soil.y() + 0.5f;
        sapling.setLocalTranslation(new Vector3f(soil.x(), yTop, soil.z()));
        sapling.scale(3f);

        plantsRoot.attachChild(sapling);
        planted.add(above);
        System.out.println("Planted apple sapling at: " + soil + " (top@" + yTop + ")");
    }

    private void plantTomatoSapling(Vector3i above, Vector3i soil) {
        Spatial sapling = assetManager.loadModel(GameConfig.MODEL_TOMATO_SAPLING_S1);
        float yTop = soil.y() + 0.5f;             
        sapling.setLocalTranslation(new Vector3f(soil.x(), yTop, soil.z()));
        sapling.scale(3f);                      
        plantsRoot.attachChild(sapling);
        planted.add(above);
        System.out.println("Planted tomato sapling at: " + soil + " (top@" + yTop + ")");
    }

}
