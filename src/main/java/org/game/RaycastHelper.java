package org.game;

import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

public class RaycastHelper {
    private final Camera camera;
    private final InputManager inputManager;
    private final SimpleBlockWorld world;

    public RaycastHelper(Camera camera, InputManager inputManager, SimpleBlockWorld world) {
        this.camera = camera;
        this.inputManager = inputManager;
        this.world = world;
    }

    public Vector3i getBlockUnderCursor() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();

        return performRaycast(origin, direction);
    }

    private Vector3i performRaycast(Vector3f origin, Vector3f direction) {
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(GameConfig.RAYCAST_STEP_SIZE);

        for (int i = 0; i < GameConfig.MAX_RAYCAST_ITERATIONS; i++) {
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

    // Также можно добавить метод в RaycastHelper:
    public Spatial getClickedObject(Spatial shopModel, Spatial houseModel) {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(GameConfig.RAYCAST_STEP_SIZE);

        for (int i = 0; i < GameConfig.MAX_RAYCAST_ITERATIONS; i++) {
            currentPos.addLocal(step);

            if (isPositionInObject(currentPos, shopModel)) {
                return shopModel;
            }
            if (isPositionInObject(currentPos, houseModel)) {
                return houseModel;
            }

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
}
