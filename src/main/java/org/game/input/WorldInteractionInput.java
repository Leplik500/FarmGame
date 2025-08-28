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

    public WorldInteractionInput(SimpleBlockWorld world, Hotbar hotbar,
                                 Camera camera, InputManager inputManager) {
        this.world = world;
        this.hotbar = hotbar;
        this.camera = camera;
        this.inputManager = inputManager;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (InputNames.WORLD_INTERACT.equals(name)) {
            handleWorldClick();
        }
    }

    private void handleWorldClick() {
        // Получаем выбранный предмет
        HotbarItem selectedItem = hotbar.getSelectedItem();
        if (selectedItem == null) return;

        // Проверяем, что это мотыга
        if (!"hoe".equals(selectedItem.id)) return;

        // Находим блок под курсором (аналогично updateFaceHighlight)
        Vector3i targetBlock = getBlockUnderCursor();
        if (targetBlock == null) return;

        // Проверяем, что это трава
        int blockType = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
        if (blockType == BlockType.GRASS) {
            // Превращаем в вспаханную землю
            world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
            System.out.println("Tilled soil at: " + targetBlock);
        }
    }

    private Vector3i getBlockUnderCursor() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f).subtract(origin).normalizeLocal();
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
        return null; // ничего не найдено
    }
}
