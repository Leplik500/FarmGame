package org.game.contexts;

import com.jme3.scene.Spatial;
import org.game.player.PlayerMovementController;

public record PlayerContext(Spatial player,
                            PlayerMovementController movementController) {
}
