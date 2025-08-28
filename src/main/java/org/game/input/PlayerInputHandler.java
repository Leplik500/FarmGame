package org.game.input;

import org.game.GameConfig;

import java.util.HashSet;
import java.util.Set;

public class PlayerInputHandler {
    private final Set<String> pressedKeys = new HashSet<>();
    private boolean isWalking = false;
    private boolean isRunning = false;
    private long lastWPressTime = 0L;

    public void handleKeyPress(String keyName) {
        pressedKeys.add(keyName);
        if (InputNames.MOVE_FWD.equals(keyName)) {
            checkForDoubleTab();
        }
    }

    public void handleKeyRelease(String keyName) {
        pressedKeys.remove(keyName);
        if (InputNames.MOVE_FWD.equals(keyName) && isRunning) {
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
        if (now - lastWPressTime <= GameConfig.DOUBLE_TAP_WINDOW_NS) {
            isRunning = true;
        }
        lastWPressTime = now;
    }

    public Set<String> getPressedKeys() { return new HashSet<>(pressedKeys); }
    public boolean isWalking() { return isWalking; }
    public boolean isRunning() { return isRunning && isWalking; }
}
