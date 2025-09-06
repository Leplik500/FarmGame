package org.game.movement;

import com.jme3.math.Vector3f;
import org.game.CollisionChecker;

public interface MovementStrategy {
    Vector3f computeNextDirection(Vector3f currentPos, Vector3f targetPos, CollisionChecker collisionChecker);
}
