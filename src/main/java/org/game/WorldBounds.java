package org.game;

import com.jme3.math.Vector3f;

public final class WorldBounds {

    private WorldBounds() {}

    public static Vector3f clampToWorldBounds(Vector3f position) {
        return new Vector3f(
                Math.max(GameConfig.WORLD_BOUNDARY_MIN_X,
                        Math.min(GameConfig.WORLD_BOUNDARY_MAX_X, position.x)),
                position.y, 
                Math.max(GameConfig.WORLD_BOUNDARY_MIN_Z,
                        Math.min(GameConfig.WORLD_BOUNDARY_MAX_Z, position.z))
        );
    }

    public static boolean isWithinBounds(Vector3f position) {
        return position.x >= GameConfig.WORLD_BOUNDARY_MIN_X &&
                position.x <= GameConfig.WORLD_BOUNDARY_MAX_X &&
                position.z >= GameConfig.WORLD_BOUNDARY_MIN_Z &&
                position.z <= GameConfig.WORLD_BOUNDARY_MAX_Z;
    }
}
