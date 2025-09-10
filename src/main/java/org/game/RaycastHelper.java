package org.game;

import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

public class RaycastHelper {
    private final Camera camera;
    private final InputManager inputManager;
    private final BlockWorld world;

    public RaycastHelper(Camera camera, InputManager inputManager, BlockWorld world) {
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

    public Spatial getClickedObject(Spatial shopModel, Spatial houseModel) {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(GameConfig.RAYCAST_STEP_SIZE);

        for (int i = 0; i < GameConfig.MAX_RAYCAST_ITERATIONS; i++) {
            currentPos.addLocal(step);

            if (isPositionInObject(currentPos, shopModel, GameConfig.SHOP_COLLISION_RADIUS)) {
                return shopModel;
            }
            if (isPositionInObject(currentPos, houseModel, GameConfig.HOUSE_COLLISION_RADIUS)) {
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

    private boolean isPositionInObject(Vector3f pos, Spatial object, float radius) {
        Vector3f objectPos = object.getWorldTranslation();
        return pos.distance(objectPos) <= radius;
    }
    
    public RaycastResult getRaycastForHighlight() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = camera.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = camera.getWorldCoordinates(cursorPos, 1f)
                .subtract(origin).normalizeLocal();

        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(GameConfig.RAYCAST_STEP_SIZE);

        for (int i = 0; i < GameConfig.MAX_RAYCAST_ITERATIONS; i++) {
            currentPos.addLocal(step);
            int bx = (int) Math.floor(currentPos.x);
            int by = (int) Math.floor(currentPos.y);
            int bz = (int) Math.floor(currentPos.z);

            if (world.getBlock(bx, by, bz) != BlockType.AIR) {
                Vector3f prevPos = currentPos.subtract(step);
                int placeX = (int) Math.floor(prevPos.x);
                int placeY = (int) Math.floor(prevPos.y);
                int placeZ = (int) Math.floor(prevPos.z);

                if (world.getBlock(placeX, placeY, placeZ) == BlockType.AIR) {
                    Vector3f attachDirection = new Vector3f(placeX - bx, placeY - by, placeZ - bz);
                    return new RaycastResult(new Vector3i(bx, by, bz), attachDirection);
                }
                return null;
            }
        }
        return null;
    }

    public record RaycastResult(Vector3i blockPos, Vector3f attachDirection) {}
}
