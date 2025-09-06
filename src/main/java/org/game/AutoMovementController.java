package org.game;

import com.jme3.math.Vector3f;
import org.game.movement.MovementStrategy;
import org.game.movement.ObstacleAvoidanceMovementStrategy;

public class AutoMovementController {
    private InteractionCommand currentCommand;
    private boolean isAutoMoving = false;
    private MovementStrategy movementStrategy;
    private final CollisionChecker collisionChecker;

    public AutoMovementController(CollisionChecker collisionChecker) {
        this.collisionChecker = collisionChecker;
        this.movementStrategy = new ObstacleAvoidanceMovementStrategy();
    }

    public void setMovementStrategy(MovementStrategy strategy) {
        this.movementStrategy = strategy;
    }

    public void startAutoMovement(InteractionCommand command) {
        this.currentCommand = command;
        this.isAutoMoving = true;
        System.out.println("Auto-moving to: " + command.getDescription());
    }

    public void cancelAutoMovement() {
        if (isAutoMoving) {
            System.out.println("Auto-movement cancelled by manual input");
            this.isAutoMoving = false;
            this.currentCommand = null;
        }
    }

    public Vector3f updateAutoMovement(Vector3f playerPos) {
        if (!isAutoMoving || currentCommand == null) {
            return null;
        }

        if (currentCommand.canExecuteAtCurrentPosition(playerPos)) {
            boolean executed = currentCommand.execute();
            if (executed) {
                System.out.println("Command executed: " + currentCommand.getDescription());
            }
            stopAutoMovement();
            return null;
        }

        Vector3f nextDirection = movementStrategy.computeNextDirection(
                playerPos,
                currentCommand.getTargetPosition(),
                collisionChecker
        );

        if (nextDirection == null) {
            float distanceToTarget = playerPos.distance(currentCommand.getTargetPosition());

            if (distanceToTarget <= GameConfig.MAX_INTERACTION_RANGE) {
                System.out.println("Path blocked but within interaction range, attempting execution");
                boolean executed = currentCommand.execute();
                if (executed) {
                    System.out.println("Command executed despite blocked path: " + currentCommand.getDescription());
                }
                stopAutoMovement();
                return null;
            }

            System.out.println("No path found to target, stopping auto-movement (distance: " + distanceToTarget + ")");
            stopAutoMovement();
            return null;
        }

        return nextDirection;
    }


    public boolean isAutoMoving() {
        return isAutoMoving;
    }

    private void stopAutoMovement() {
        this.isAutoMoving = false;
        this.currentCommand = null;
    }
}
