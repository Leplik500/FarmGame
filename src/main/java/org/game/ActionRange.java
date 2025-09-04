package org.game;

import com.jme3.math.Vector3f;

public final class ActionRange {

    private ActionRange() {}

    public static boolean isWithinRange(Vector3f playerPos, Vector3f targetPos) {
        float distance = playerPos.distance(targetPos);
        return distance <= GameConfig.MAX_INTERACTION_RANGE;
    }

    public static boolean isWithinRange(Vector3f playerPos, Vector3i blockPos) {
        Vector3f targetPos = new Vector3f(blockPos.x(), blockPos.y(), blockPos.z());
        return isWithinRange(playerPos, targetPos);
    }

    public static float getDistance(Vector3f playerPos, Vector3f targetPos) {
        return playerPos.distance(targetPos);
    }
}
