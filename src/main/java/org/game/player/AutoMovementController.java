package org.game.player;

import com.jme3.math.Vector3f;
import org.game.utils.GameConfig;
import org.game.core.InteractionCommand;

public class AutoMovementController {
    private InteractionCommand currentCommand;
    private boolean isAutoMoving = false;
    private final CollisionChecker collisionChecker;

    private static final float BASE_AVOIDANCE_ANGLE = (float) Math.toRadians(15);
    private static final int PATH_CHECK_STEPS = 3;
    private static final float STEP_DISTANCE = 0.5f;
    private static final int MAX_AVOIDANCE_ATTEMPTS = 5;

    public AutoMovementController(CollisionChecker collisionChecker) {
        this.collisionChecker = collisionChecker;
    }

    public void startAutoMovement(InteractionCommand command) {
        this.currentCommand = command;
        this.isAutoMoving = true;
        System.out.println("Auto-moving to: " + command.getDescription());
    }

    public void cancelAutoMovement() {
        if (isAutoMoving) {
            System.out.println("Auto-movement cancelled by manual input");
            stopAutoMovement();
        }
    }

    public Vector3f updateAutoMovement(Vector3f playerPos) {
        if (!canContinueAutoMovement()) {
            return null;
        }

        if (hasReachedTarget(playerPos)) {
            executeCommand();
            return null;
        }

        Vector3f direction = findMovementDirection(playerPos);
        if (direction == null) {
            handleNoPathFound(playerPos);
        }

        return direction;
    }

    public boolean isAutoMoving() {
        return isAutoMoving;
    }

    private boolean canContinueAutoMovement() {
        return isAutoMoving && currentCommand != null;
    }

    private boolean hasReachedTarget(Vector3f playerPos) {
        return currentCommand.canExecuteAtCurrentPosition(playerPos);
    }

    private void executeCommand() {
        boolean executed = currentCommand.execute();
        if (executed) {
            System.out.println("Command executed: " + currentCommand.getDescription());
        }
        stopAutoMovement();
    }

    private void handleNoPathFound(Vector3f playerPos) {
        float distance = playerPos.distance(currentCommand.getTargetPosition());

        if (distance <= GameConfig.MAX_INTERACTION_RANGE) {
            System.out.println("Path blocked but within range, attempting execution");
            executeCommand();
        } else {
            System.out.println("No path found, stopping auto-movement (distance: " + distance + ")");
            stopAutoMovement();
        }
    }

    private Vector3f findMovementDirection(Vector3f currentPos) {
        Vector3f targetDirection = calculateTargetDirection(currentPos);

        if (targetDirection == null) {
            return null;
        }

        if (isPathClear(currentPos, targetDirection)) {
            return targetDirection;
        }

        return findAvoidanceDirection(currentPos, targetDirection);
    }

    private Vector3f calculateTargetDirection(Vector3f currentPos) {
        Vector3f toTarget = currentCommand.getTargetPosition().subtract(currentPos);
        toTarget.y = 0; 

        float distance = toTarget.length();
        if (distance < GameConfig.MAX_INTERACTION_RANGE) {
            return null;
        }

        return toTarget.normalizeLocal();
    }

    private Vector3f findAvoidanceDirection(Vector3f currentPos, Vector3f preferredDirection) {
        for (int attempt = 1; attempt <= MAX_AVOIDANCE_ATTEMPTS; attempt++) {
            float angle = BASE_AVOIDANCE_ANGLE * attempt;

            Vector3f leftDirection = rotateVectorY(preferredDirection, angle);
            if (isPathClear(currentPos, leftDirection)) {
                return leftDirection;
            }

            Vector3f rightDirection = rotateVectorY(preferredDirection, -angle);
            if (isPathClear(currentPos, rightDirection)) {
                return rightDirection;
            }
        }

        Vector3f backwardDirection = preferredDirection.negate();
        if (isPathClear(currentPos, backwardDirection)) {
            return backwardDirection;
        }

        return null; 
    }

    private boolean isPathClear(Vector3f startPos, Vector3f direction) {
        for (int step = 1; step <= PATH_CHECK_STEPS; step++) {
            Vector3f checkPos = startPos.add(direction.mult(STEP_DISTANCE * step));

            if (collisionChecker.isPositionBlockedWithRadius(checkPos, 0.3f)) {
                return false;
            }
        }
        return true;
    }

    private Vector3f rotateVectorY(Vector3f vector, float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        return new Vector3f(
                vector.x * cos - vector.z * sin,
                0,
                vector.x * sin + vector.z * cos
        );
    }

    private void stopAutoMovement() {
        this.isAutoMoving = false;
        this.currentCommand = null;
    }
}
