package org.game.contexts;

import com.jme3.scene.Spatial;
import org.game.PlayerMovementController;

public record PlayerContext(Spatial player,
                            PlayerMovementController movementController) {
}
