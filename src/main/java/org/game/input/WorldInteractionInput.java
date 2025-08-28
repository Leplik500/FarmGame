package org.game.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.input.InputManager;
import org.game.*;

public class WorldInteractionInput implements ActionListener {
    private final SimpleBlockWorld world;
    private final Hotbar hotbar;
    private final Camera camera;
    private final InputManager inputManager;
    private final FarmlandMoistureState moisture; 

    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager,
                                 FarmlandMoistureState moisture) {
        this.world = world;
        this.hotbar = hotbar;
        this.camera = camera;
        this.inputManager = inputManager;
        this.moisture = moisture;
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

        // 1) Мотыга: трава -> вспаханная
        if ("hoe".equals(selectedItem.id)) {
            if (type == BlockType.GRASS) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
                System.out.println("Tilled soil at: " + targetBlock);
            }
            return; // другие типы игнорируем
        }

        // 2) Лейка: сухая вспаханная -> влажная (и запустить/обновить таймер)
        if ("watering_can".equals(selectedItem.id)) {
            if (type == BlockType.PLOWED_DRY) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_WET);
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null); // дефолтный таймер
                System.out.println("Watered soil at: " + targetBlock);
            } else if (type == BlockType.PLOWED_WET) {
                // Обновить таймер увлажнения повторным поливом
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Refreshed moisture at: " + targetBlock);
            }
            return; // иные типы игнорируем
        }

        // Иные предметы — ничего не происходит
    }

    private Vector3i getBlockUnderCursor() {
        // Луч из позиции курсора: convert 2D -> 3D и шагать вдоль направления
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(0.1f);

        for (int i = 0; i < 1000; i++) {
            currentPos.addLocal(step);
            int bx = (int) Math.floor(currentPos.x);
            int by = (int) Math.floor(currentPos.y);
            int bz = (int) Math.floor(currentPos.z);
            if (world.getBlock(bx, by, bz) != BlockType.AIR) {
                return new Vector3i(bx, by, bz);
            }
        }
        return null;
    }
}
