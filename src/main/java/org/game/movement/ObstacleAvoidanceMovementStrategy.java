package org.game.movement;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import org.game.CollisionChecker;
import org.game.GameConfig;

public class ObstacleAvoidanceMovementStrategy implements MovementStrategy {

    private static final float BASE_AVOIDANCE_ANGLE = FastMath.DEG_TO_RAD * 15f;
    private static final int PATH_CHECK_STEPS = 3; 
    private static final float STEP_DISTANCE = 0.5f;

    @Override
    public Vector3f computeNextDirection(Vector3f currentPos, Vector3f targetPos, CollisionChecker collisionChecker) {
        Vector3f toTarget = targetPos.subtract(currentPos);
        toTarget.y = 0;

        float distance = toTarget.length();
        if (distance < GameConfig.MAX_INTERACTION_RANGE) {
            return null; 
        }

        Vector3f normalizedDirection = toTarget.normalizeLocal();

        if (isPathClearMultiStep(currentPos, normalizedDirection, collisionChecker)) {
            return normalizedDirection;
        }

        for (int angle = 1; angle <= 5; angle++) {
            float currentAngle = BASE_AVOIDANCE_ANGLE * angle;

            Vector3f leftDirection = rotateVector(normalizedDirection, currentAngle);
            if (isPathClearMultiStep(currentPos, leftDirection, collisionChecker)) {
                return leftDirection;
            }

            Vector3f rightDirection = rotateVector(normalizedDirection, -currentAngle);
            if (isPathClearMultiStep(currentPos, rightDirection, collisionChecker)) {
                return rightDirection;
            }
        }

        Vector3f backwardDirection = normalizedDirection.negate();
        if (isPathClearMultiStep(currentPos, backwardDirection, collisionChecker)) {
            return backwardDirection;
        }

        return null; 
    }

    private boolean isPathClearMultiStep(Vector3f startPos, Vector3f direction, CollisionChecker collisionChecker) {
        for (int step = 1; step <= PATH_CHECK_STEPS; step++) {
            float checkDistance = STEP_DISTANCE * step;
            Vector3f checkPos = startPos.add(direction.mult(checkDistance));

            if (collisionChecker.isPositionBlockedWithRadius(checkPos, 0.3f)) {
                return false;
            }
        }
        return true;
    }


    private Vector3f rotateVector(Vector3f vector, float angleRadians) {
        Quaternion rotation = new Quaternion().fromAngleAxis(angleRadians, Vector3f.UNIT_Y);
        return rotation.mult(vector);
    }
    
    
}
