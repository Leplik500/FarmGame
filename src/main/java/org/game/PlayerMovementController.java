package org.game;

import com.jme3.math.*;
import com.jme3.scene.Spatial;
import com.jme3.renderer.Camera;
import java.util.Set;

public class PlayerMovementController {
    private final Spatial player;
    private final Camera camera;
    private final AutoMovementController autoMovementController;

    public PlayerMovementController(Spatial player, Camera camera) {
        this.player = player;
        this.camera = camera;
        this.autoMovementController = new AutoMovementController(); 
    }
    
    public void handleMovement(Set<String> pressedKeys, boolean isRunning, float tpf) {
        if (!pressedKeys.isEmpty()) {
            autoMovementController.cancelAutoMovement();
        }

        Vector3f movement = calculateMovementDirection(pressedKeys);

        if (movement.lengthSquared() == 0) {
            Vector3f autoMovement = autoMovementController.updateAutoMovement(
                    player.getWorldTranslation());
            if (autoMovement != null) {
                movement = autoMovement;
                isRunning = true;
            }
        }

        if (movement.lengthSquared() > 0f) {
            movement.normalizeLocal();
            float currentSpeed = isRunning ? GameConfig.RUN_SPEED : GameConfig.WALK_SPEED;
            Vector3f step = movement.mult(currentSpeed * tpf);

            Vector3f currentPos = player.getWorldTranslation();
            Vector3f newPos = currentPos.add(step);
            Vector3f clampedPos = WorldBounds.clampToWorldBounds(newPos);
            Vector3f clampedStep = clampedPos.subtract(currentPos);

            if (clampedStep.lengthSquared() > 0.0001f) {
                player.move(clampedStep);
                rotateTowardsMovement(clampedStep, tpf);
            }
        }
    }



    private Vector3f calculateMovementDirection(Set<String> pressedKeys) {
        Vector3f forward = camera.getDirection().clone();
        forward.y = 0f;
        forward.normalizeLocal();

        Vector3f left = camera.getLeft().clone();
        left.y = 0f;
        left.normalizeLocal();

        Vector3f movement = new Vector3f();
        if (pressedKeys.contains("moveForward"))  movement.addLocal(forward);
        if (pressedKeys.contains("moveBackward")) movement.addLocal(forward.negate());
        if (pressedKeys.contains("moveRight"))    movement.addLocal(left.negate());
        if (pressedKeys.contains("moveLeft"))     movement.addLocal(left);
        return movement;
    }

    private void rotateTowardsMovement(Vector3f step, float tpf) {
        float targetYaw = (float) Math.atan2(step.x, step.z);
        float[] angles = player.getLocalRotation().toAngles(null);
        float currentYaw = angles[1];
        float diff = targetYaw - currentYaw;
        diff = (diff + FastMath.PI) % FastMath.TWO_PI - FastMath.PI;
        float lerp = Math.min(1f, GameConfig.ROTATION_SPEED * tpf);
        float newYaw = currentYaw + diff * lerp;
        player.setLocalRotation(new Quaternion().fromAngles(0f, newYaw, 0f));
    }

    public AutoMovementController getAutoMovementController() {
        return autoMovementController;
    }
}
