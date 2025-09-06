package org.game;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public final class GameConfig {

    // ========== DISPLAY SETTINGS ==========
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 814;
    public static final boolean FULLSCREEN = false;

    // ========== PLAYER MOVEMENT ==========
    public static final float WALK_SPEED = 5f;
    public static final float RUN_SPEED = 10f;
    public static final float ROTATION_SPEED = 10f;
    public static final float MAX_INTERACTION_RANGE = 1f;

    // ========== INPUT SETTINGS ==========
    public static final long DOUBLE_TAP_WINDOW_NS = 600_000_000L; // 600ms

    // ========== ANIMATION SETTINGS ==========
    public static final String ANIM_IDLE = "animation.lael.idlemain";
    public static final String ANIM_WALK = "animation.lael.walk";
    public static final String ANIM_RUN = "animation.lael.fast_run";

    // ========== CAMERA SETTINGS ==========
    public static final float CAM_DEFAULT_DISTANCE = 40f;
    public static final float CAM_MIN_DISTANCE = 24f;
    public static final float CAM_MAX_DISTANCE = 80f;
    public static final float CAM_VERTICAL_ANGLE = FastMath.DEG_TO_RAD * 77f;
    public static final float CAM_HORIZONTAL_ANGLE = FastMath.DEG_TO_RAD * 35f;
    public static final Vector3f CAM_LOOK_OFFSET = new Vector3f(0, 1.2f, 0);
    public static final float CAM_ROTATION_SPEED = 1f;
    public static final float CAM_ZOOM_SENSITIVITY = 0.6f;
    public static final float CAM_FOLLOW_SPEED = 7f;
    public static final float CAM_FOV_DEGREES = 40f;

    // ========== LIGHTING SETTINGS ==========
    public static final Vector3f LIGHT_DIRECTION = new Vector3f(-1f, -1f, -1f);
    public static final float DAY_DURATION_SECONDS = 300f;
    public static final float NIGHT_DURATION_SECONDS = 180f;
    public static final float DAY_LIGHT_INTENSITY = 2f;
    public static final float NIGHT_LIGHT_INTENSITY = 0.3f;


    // ========== WORLD SETTINGS ==========
    public static final int WORLD_SIZE_X = 100;
    public static final int WORLD_SIZE_Z = 100;
    public static final float WORLD_BOUNDARY_MIN_X = -(WORLD_SIZE_X / 2f);
    public static final float WORLD_BOUNDARY_MAX_X = (WORLD_SIZE_X / 2f) - 1;
    public static final float WORLD_BOUNDARY_MIN_Z = -(WORLD_SIZE_Z / 2f);
    public static final float WORLD_BOUNDARY_MAX_Z = (WORLD_SIZE_Z / 2f) - 1;
    


    // ========== UI SETTINGS ==========
    public static final float HIGHLIGHT_THICKNESS = 0.2f;
    public static final float HIGHLIGHT_SIZE = 0.52f;
    public static final float HIGHLIGHT_OFFSET = 0.501f;
    public static final ColorRGBA HIGHLIGHT_COLOR = new ColorRGBA(1f, 1f, 0f, 1.0f);
    public static final int CURSOR_SIZE = 32;
    
    // ========== ASSET PATHS ==========
    public static final String MODEL_PLAYER = "Models/rhea_wilson.glb";
    public static final String CURSOR_PATH = "Cursors/hand.cur";
    public static final String[] PUMPKIN_GROWTH_MODELS = {
            "Models/Pumpkin_1.glb",
            "Models/Pumpkin_2.glb",
            "Models/Pumpkin_3.glb",
            "Models/Pumpkin_4.glb"
    };

    public static final String PUMPKIN_HARVESTED_MODEL = "Models" +
            "/Pumpkin_Harvested.glb";
    public static final String TOMATO_HARVESTED_MODEL = "Models" +
            "/Tomato_Harvested.glb";

    public static final String[] TOMATO_GROWTH_MODELS = {
            "Models/Tomato_1.glb",
            "Models/Tomato_2.glb",
            "Models/Tomato_3.glb",
            "Models/Tomato_4.glb"
    };
    public static final String SHOP_MODEL = "Models/store6.glb";
    public static final String HOUSE_MODEL = "Models/house1.glb";

    public static final String PUMPKIN_ITEM = "Textures/pumpkin.png";
    public static final String TOMATO_SEEDS_ITEM = "Textures/tomato_seeds.png";
    public static final String TOMATO_ITEM = "Textures/tomato.png";
    public static final String PUMPKIN_SEEDS_ITEM = "Textures/pumpkin_seeds" +
            ".png";
    public static final String WATERING_CAN_ITEM = "Textures/watering_can.png";
    public static final String HOE_ITEM = "Textures/hoe.png";
    public static final String DEFAULT_ITEM = "Textures/default_item.png";
    public static final String MONEY_ICON = "Textures/money.png";

    public static final String SKYBOX_PX = "SkyBox/px.png";
    public static final String SKYBOX_NX = "SkyBox/nx.png";
    public static final String SKYBOX_PY = "SkyBox/py.png";
    public static final String SKYBOX_NY = "SkyBox/ny.png";
    public static final String SKYBOX_PZ = "SkyBox/pz.png";
    public static final String SKYBOX_NZ = "SkyBox/nz.png";

    public static final float[] GROWTH_STAGE_SECONDS = { 15f, 20f, 30f };
    public static final float FRUIT_REGROW_SECONDS = 45f;

    // ========== SHOP SETTINGS ==========
    public static final int PUMPKIN_SEEDS_PRICE = 5;
    public static final int TOMATO_SEEDS_PRICE = 3;
    public static final int PUMPKIN_SELL_PRICE = 20;
    public static final int TOMATO_SELL_PRICE = 15;

    // ========== RAYCAST SETTINGS ==========
    public static final int MAX_RAYCAST_ITERATIONS = 1000;
    public static final float RAYCAST_STEP_SIZE = 0.1f;

    private GameConfig() {}
}

