package org.game;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

import java.util.Set;

public class PlayerMovementController {
    private static final float WALK_SPEED = 5f;
    private static final float RUN_SPEED = 10f;
    private static final float ROTATION_SPEED = 10f;

    private final Spatial player;
    private final Camera camera;

    public PlayerMovementController(Spatial player, Camera camera) {
        this.player = player;
        this.camera = camera;
    }

    public void handleMovement(Set<String> pressedKeys, boolean isRunning, float tpf) {
        Vector3f movement = calculateMovementDirection(pressedKeys);

        if (movement.lengthSquared() > 0f) {
            movement.normalizeLocal();
            float currentSpeed = isRunning ? RUN_SPEED : WALK_SPEED;
            Vector3f step = movement.mult(currentSpeed * tpf);

            player.move(step);
            rotateTowardsMovement(step, tpf);
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

        if (pressedKeys.contains("moveForward")) movement.addLocal(forward);
        if (pressedKeys.contains("moveBackward"))
            movement.addLocal(forward.negate());
        if (pressedKeys.contains("moveRight")) movement.addLocal(left.negate());
        if (pressedKeys.contains("moveLeft")) movement.addLocal(left);

        return movement;
    }

    private void rotateTowardsMovement(Vector3f step, float tpf) {
        float targetYaw = (float) Math.atan2(step.x, step.z);
        float[] angles = player.getLocalRotation().toAngles(null);
        float currentYaw = angles[1];
        float diff = targetYaw - currentYaw;
        diff = (diff + FastMath.PI) % FastMath.TWO_PI - FastMath.PI;
        float lerp = Math.min(1f, ROTATION_SPEED * tpf);
        float newYaw = currentYaw + diff * lerp;
        player.setLocalRotation(new Quaternion().fromAngles(0f, newYaw, 0f));
    }
}

