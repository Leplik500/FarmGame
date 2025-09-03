package org.game.input;

public final class InputNames {
    private InputNames(){}

    public static final String MOVE_FWD = "moveForward";
    public static final String MOVE_BACK = "moveBackward";
    public static final String MOVE_LEFT = "moveLeft";
    public static final String MOVE_RIGHT = "moveRight";

    public static final String CTRL = "ctrl";

    public static final String WHEEL_UP = "wheel_up";
    public static final String WHEEL_DOWN = "wheel_down";

    public static String hotbarSlot(int idx) { return "hotbar_" + idx; }

    public static final String HOTBAR_NEXT = "hotbar_next";
    public static final String HOTBAR_PREV = "hotbar_prev";

    public static final String CAMERA_RESET_ZOOM = "camera_reset_zoom";

    public static final String WORLD_INTERACT = "world_interact";
    
    public static final String LOAD_GAME = "load_game";
    public static final String SAVE_GAME = "save_game";
}
