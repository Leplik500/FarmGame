package org.game.movement;

import com.jme3.math.Vector3f;
import org.game.CollisionChecker;
import org.game.GameConfig;

public class StraightLineMovementStrategy implements MovementStrategy {

    @Override
    public Vector3f computeNextDirection(Vector3f currentPos, Vector3f targetPos, CollisionChecker collisionChecker) {
        Vector3f direction = targetPos.subtract(currentPos);
        direction.y = 0;

        float distance = direction.length();
        if (distance < GameConfig.MAX_INTERACTION_RANGE) {
            return null;
        }

        return direction.normalizeLocal();
    }
}
