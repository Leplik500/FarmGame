package org.game;

import com.jme3.math.Vector3f;

public class AutoMovementController {

    private InteractionCommand currentCommand;
    private boolean isAutoMoving = false;
    private static final float MOVEMENT_THRESHOLD =
            GameConfig.MAX_INTERACTION_RANGE;

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

        Vector3f targetPos = currentCommand.getTargetPosition();
        Vector3f direction = targetPos.subtract(playerPos);

        if (direction.length() < MOVEMENT_THRESHOLD) {
            stopAutoMovement();
            return null;
        }

        direction.y = 0;
        return direction.normalizeLocal();
    }

    public boolean isAutoMoving() {
        return isAutoMoving;
    }

    private void stopAutoMovement() {
        this.isAutoMoving = false;
        this.currentCommand = null;
    }
    
    
}
