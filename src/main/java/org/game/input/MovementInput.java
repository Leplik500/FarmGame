package org.game.input;

import com.jme3.input.controls.*;

public class MovementInput implements ActionListener {
    private final PlayerInputHandler handler;

    public MovementInput(PlayerInputHandler handler) { this.handler = handler; }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case InputNames.MOVE_FWD:
            case InputNames.MOVE_BACK:
            case InputNames.MOVE_LEFT:
            case InputNames.MOVE_RIGHT:
                if (isPressed) handler.handleKeyPress(name);
                else           handler.handleKeyRelease(name);
                handler.updateWalkingState(); 
                break;
            default:
        }
    }
}

