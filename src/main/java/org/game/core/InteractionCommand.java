package org.game.core;

import com.jme3.math.Vector3f;

public interface InteractionCommand {

    boolean execute();

    Vector3f getTargetPosition();

    boolean canExecuteAtCurrentPosition(Vector3f playerPos);

    boolean isValidTarget();

    String getDescription();
}
