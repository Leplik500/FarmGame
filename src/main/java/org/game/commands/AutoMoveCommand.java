package org.game.commands;

import com.jme3.math.Vector3f;
import org.game.InteractionCommand;
import org.game.PlayerMovementController;

public class AutoMoveCommand implements InteractionCommand {
    private final InteractionCommand targetCommand;
    private final PlayerMovementController movementController;

    public AutoMoveCommand(InteractionCommand targetCommand, PlayerMovementController movementController) {
        this.targetCommand = targetCommand;
        this.movementController = movementController;
    }

    @Override
    public boolean execute() {
        System.out.println("Out of range, starting auto-movement to: " + targetCommand.getDescription());
        movementController.getAutoMovementController().startAutoMovement(targetCommand);
        return true;
    }

    @Override
    public Vector3f getTargetPosition() {
        return targetCommand.getTargetPosition();
    }

    @Override
    public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
        return targetCommand.canExecuteAtCurrentPosition(playerPos);
    }

    @Override
    public boolean isValidTarget() {
        return targetCommand.isValidTarget();
    }

    @Override
    public String getDescription() {
        return targetCommand.getDescription();
    }
}
