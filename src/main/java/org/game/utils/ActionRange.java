package org.game.utils;

import com.jme3.math.Vector3f;
import org.game.world.Vector3i;

public final class ActionRange {
    private static final float PRECISION_TOLERANCE = 0.05f;

    private ActionRange() {}

    public static boolean isWithinRange(Vector3f playerPos, Vector3f targetPos) {
        float distance = playerPos.distance(targetPos);
        return distance <= (GameConfig.MAX_INTERACTION_RANGE + PRECISION_TOLERANCE);
    }

    public static boolean isWithinRange(Vector3f playerPos, Vector3i blockPos) {
        Vector3f targetPos = new Vector3f(blockPos.x(), blockPos.y(), blockPos.z());
        return isWithinRange(playerPos, targetPos);
    }
}

