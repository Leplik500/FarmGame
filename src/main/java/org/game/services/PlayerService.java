package org.game.services;

import org.game.PlayerMovementController;
import com.jme3.scene.Spatial;

public class PlayerService {
    private final Spatial player;
    private final PlayerMovementController movementController;

    public PlayerService(Spatial player, PlayerMovementController movementController) {
        this.player = player;
        this.movementController = movementController;
    }

    public Spatial getPlayer() { return player; }
    public PlayerMovementController getMovementController() { return movementController; }
}
