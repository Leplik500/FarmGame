package org.game;

import java.util.HashSet;
import java.util.Set;

public class PlayerInputHandler {
    private static final long DOUBLE_TAP_NS = 600_000_000L;

    private final Set<String> pressedKeys = new HashSet<>();
    private boolean isWalking = false;
    private boolean isRunning = false;
    private long lastWPressTime = 0L;

    public void handleKeyPress(String keyName) {
        pressedKeys.add(keyName);

        if ("moveForward".equals(keyName)) {
            checkForDoubleTab();
        }
    }

    public void handleKeyRelease(String keyName) {
        pressedKeys.remove(keyName);

        if ("moveForward".equals(keyName) && isRunning) {
            isRunning = false;
        }
    }

    public boolean updateWalkingState() {
        boolean shouldBeWalking = !pressedKeys.isEmpty();
        boolean stateChanged = shouldBeWalking != isWalking;
        isWalking = shouldBeWalking;
        return stateChanged;
    }

    private void checkForDoubleTab() {
        long now = System.nanoTime();
        if (now - lastWPressTime <= DOUBLE_TAP_NS) {
            isRunning = true;
        }
        lastWPressTime = now;
    }

    // Getters
    public Set<String> getPressedKeys() {
        return new HashSet<>(pressedKeys);
    }

    public boolean isWalking() {
        return isWalking;
    }

    public boolean isRunning() {
        return isRunning && isWalking;
    }
}

