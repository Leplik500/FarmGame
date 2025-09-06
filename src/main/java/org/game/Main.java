package org.game;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import org.game.input.*;

import java.awt.*;

public class Main extends SimpleApplication implements ActionListener {

    private final Node camTarget = new Node("CamTarget");
    private final Node plantsRoot = new Node("PlantsRoot");
    private Spatial player;
    private SimpleBlockWorld blockWorld;
    private Spatial faceHighlight;
    private AnimComposer animComposer;
    private PlayerInputHandler inputHandler;
    private PlayerMovementController movementController;
    private PlayerAnimationController animationController;
    private Hotbar hotbar;
    private ChaseCamera chaseCam;
    private MoneyDisplay moneyDisplay;
    private Spatial shopModel;
    private Spatial houseModel;
    private ShopUI shopUI;
    private PlantGrowthState growth;
    private FarmlandMoistureState moisture;
    private PlantFactory plantFactory;
    private DayNightCycle dayNightCycle;
    private DirectionalLight sunLight;
    private AmbientLight ambientLight;
    private InventoryManager inventoryManager;

    private record InputModules(CameraInput cameraInput,
                                HotbarInput hotbarInput,
                                MovementInput movementInput, UIInput uiInput,
                                WorldInteractionInput worldInput,
                                GameSaveManager saveManager) {
    }


    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);

        if (GameConfig.FULLSCREEN) {
            settings = createFullscreenSettings();
        } else {
            settings.setResolution(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
            settings.setFullscreen(false);
        }

        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    private static AppSettings createFullscreenSettings() {
        AppSettings settings = new AppSettings(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();

        // Use the first (typically highest resolution) display mode
        DisplayMode mode = modes[0];
        settings.setResolution(mode.getWidth(), mode.getHeight());
        settings.setFrequency(mode.getRefreshRate());
        settings.setBitsPerPixel(mode.getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());

        return settings;
    }

    @Override
    public void simpleInitApp() {
        initPlayer();
        initWorld();
        initUI();
        initInput();
    }

    private void initPlayer() {
        player = assetManager.loadModel(GameConfig.MODEL_PLAYER);
        player.depthFirstTraversal(spatial -> {
            if (spatial.getControl(AnimComposer.class) != null) {
                animComposer = spatial.getControl(AnimComposer.class);
            }
        });
        player.setLocalTranslation(0f, -0.5f, 0f);

        if (animComposer != null) {
            System.out.println("Available animations: " + animComposer.getAnimClipsNames());
        }

        rootNode.attachChild(player);
    }

    private void initWorld() {
        sunLight = new DirectionalLight();
        sunLight.setDirection(GameConfig.LIGHT_DIRECTION.normalizeLocal());
        sunLight.setColor(ColorRGBA.White.mult(GameConfig.DAY_LIGHT_INTENSITY));
        rootNode.addLight(sunLight);

        ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.15f));
        rootNode.addLight(ambientLight);

        blockWorld = new SimpleBlockWorld(rootNode, assetManager);
        blockWorld.generateFlatWorld(GameConfig.WORLD_SIZE_X, GameConfig.WORLD_SIZE_Z);

        moisture = new FarmlandMoistureState(blockWorld);
        stateManager.attach(moisture);

        dayNightCycle = new DayNightCycle();
        stateManager.attach(dayNightCycle);

        rootNode.attachChild(plantsRoot);
        plantFactory = new PlantFactory(assetManager, plantsRoot);
        growth = new PlantGrowthState(blockWorld, assetManager, plantsRoot, dayNightCycle);
        stateManager.attach(growth);

        addShopToScene();
        addHouseToScene();
        createWorldBoundaries();
        addSky();
    }


    private void initUI() {
        rootNode.attachChild(camTarget);
        camTarget.setLocalTranslation(player.getLocalTranslation());
        camTarget.setLocalRotation(player.getLocalRotation());
        configureCamera();
        createFaceHighlight();
        setupCustomCursor();

        hotbar = new Hotbar(guiNode, assetManager, cam.getWidth(), cam.getHeight());
        hotbar.addItemToSlot(2, new HotbarItem(ItemIds.WATERING_CAN, "Textures/watering_can.png"));
        hotbar.addItemToSlot(3, new HotbarItem(ItemIds.HOE, "Textures/hoe.png"));

        moneyDisplay = new MoneyDisplay(guiNode, assetManager, cam.getWidth());
        moneyDisplay.addMoney(10);

        inventoryManager = new InventoryManager(hotbar);
        shopUI = new ShopUI(guiNode, assetManager, moneyDisplay, inventoryManager);
    }

    private void initInput() {
        initInputControllers();
        GameInputRouter router = createInputRouter();
        InputModules modules = createInputModules();
        registerInputModules(router, modules);
    }

    private void initInputControllers() {
        inputHandler = new PlayerInputHandler();
        movementController = new PlayerMovementController(player, cam);
        animationController = new PlayerAnimationController(animComposer);
    }

    private GameInputRouter createInputRouter() {
        return new GameInputRouter(inputManager);
    }

    private InputModules createInputModules() {
        CameraInput cameraInput = new CameraInput(chaseCam);
        HotbarInput hotbarInput = new HotbarInput(hotbar, cameraInput);
        MovementInput movementInput = new MovementInput(inputHandler);
        UIInput uiInput = new UIInput(shopUI);
        GameSaveManager saveManager = new GameSaveManager();

        WorldInteractionInput worldInput = new WorldInteractionInput(
                blockWorld, hotbar, cam, inputManager, moisture, assetManager,
                growth, shopModel, shopUI, plantFactory, houseModel, dayNightCycle,
                player, movementController, inventoryManager);

        return new InputModules(cameraInput, hotbarInput, movementInput,
                uiInput, worldInput, saveManager);
    }

    private void registerInputModules(GameInputRouter router, InputModules modules) {
        router.addActionModule(modules.cameraInput);
        router.addAnalogModule(modules.cameraInput);
        router.addActionModule(modules.hotbarInput);
        router.addAnalogModule(modules.hotbarInput);
        router.addActionModule(modules.movementInput);
        router.addActionModule(modules.worldInput);
        router.addActionModule(modules.uiInput);
        router.addActionModule(new SaveLoadInput(this, modules.saveManager));
    }


    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            inputHandler.handleKeyPress(name);
        } else {
            inputHandler.handleKeyRelease(name);
        }

        inputHandler.updateWalkingState();
    }


    @Override
    public void simpleUpdate(float tpf) {
        boolean hasManualInput = inputHandler.isWalking();
        boolean hasAutoMovement = movementController.getAutoMovementController().isAutoMoving();

        if (hasManualInput || hasAutoMovement) {
            movementController.handleMovement(inputHandler.getPressedKeys(), inputHandler.isRunning(), tpf);
        }

        boolean isMoving = hasManualInput || hasAutoMovement;
        boolean isRunning = hasManualInput ? inputHandler.isRunning() : hasAutoMovement;
        animationController.update(isMoving, isRunning);

        Vector3f to = player.getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();
        float camFollowPosSpeed = GameConfig.CAM_FOLLOW_SPEED;
        Vector3f posDelta = to.subtract(from).multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);

        updateLighting();

        updateFaceHighlight();
        if (hotbar != null) {
            hotbar.updateViewportSizeIfChanged(cam.getWidth(), cam.getHeight());
            moneyDisplay.updatePosition(cam.getWidth());
        }
    }



    private void addSky() {
        Texture px = assetManager.loadTexture(GameConfig.SKYBOX_PX);
        Texture nx = assetManager.loadTexture(GameConfig.SKYBOX_NX);
        Texture py = assetManager.loadTexture(GameConfig.SKYBOX_PY);
        Texture ny = assetManager.loadTexture(GameConfig.SKYBOX_NY);
        Texture pz = assetManager.loadTexture(GameConfig.SKYBOX_PZ);
        Texture nz = assetManager.loadTexture(GameConfig.SKYBOX_NZ);
        Spatial sky = SkyFactory.createSky(assetManager, px, nx, py, ny, pz, nz);
        rootNode.attachChild(sky);
    }

    private void configureCamera() {
        flyCam.setEnabled(false);
        chaseCam = new ChaseCamera(cam, camTarget, inputManager);
        chaseCam.setDefaultDistance(GameConfig.CAM_DEFAULT_DISTANCE);
        chaseCam.setMinDistance(GameConfig.CAM_MIN_DISTANCE);
        chaseCam.setMaxDistance(GameConfig.CAM_MAX_DISTANCE);
        chaseCam.setDefaultVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCam.setMinVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCam.setMaxVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCam.setDefaultHorizontalRotation(GameConfig.CAM_HORIZONTAL_ANGLE);
        chaseCam.setLookAtOffset(GameConfig.CAM_LOOK_OFFSET);
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        chaseCam.setZoomInTrigger();
        chaseCam.setZoomOutTrigger();
        chaseCam.setDragToRotate(true);
        chaseCam.setRotationSpeed(GameConfig.CAM_ROTATION_SPEED);
        chaseCam.setZoomSensitivity(GameConfig.CAM_ZOOM_SENSITIVITY);
        chaseCam.setSmoothMotion(false);
        float fovDegrees = GameConfig.CAM_FOV_DEGREES;
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustumPerspective(fovDegrees, aspect, 0.1f, 1500);
    }

    private void createFaceHighlight() {
        Node highlightNode = new Node("ThickHighlight");
        float thickness = GameConfig.HIGHLIGHT_THICKNESS;
        float size = GameConfig.HIGHLIGHT_SIZE;

        Box topLine = new Box(size, thickness / 2, thickness / 2);
        Geometry topGeo = new Geometry("TopLine", topLine);
        topGeo.setLocalTranslation(0, size, 0);

        Box bottomLine = new Box(size, thickness / 2, thickness / 2);
        Geometry bottomGeo = new Geometry("BottomLine", bottomLine);
        bottomGeo.setLocalTranslation(0, -size, 0);

        Box leftLine = new Box(thickness / 2, size, thickness / 2);
        Geometry leftGeo = new Geometry("LeftLine", leftLine);
        leftGeo.setLocalTranslation(-size, 0, 0);

        Box rightLine = new Box(thickness / 2, size, thickness / 2);
        Geometry rightGeo = new Geometry("RightLine", rightLine);
        rightGeo.setLocalTranslation(size, 0, 0);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", GameConfig.HIGHLIGHT_COLOR);
        mat.getAdditionalRenderState().setDepthTest(false);
        mat.getAdditionalRenderState().setDepthWrite(false);

        topGeo.setMaterial(mat);
        bottomGeo.setMaterial(mat);
        leftGeo.setMaterial(mat);
        rightGeo.setMaterial(mat);

        highlightNode.attachChild(topGeo);
        highlightNode.attachChild(bottomGeo);
        highlightNode.attachChild(leftGeo);
        highlightNode.attachChild(rightGeo);

        highlightNode.setQueueBucket(RenderQueue.Bucket.Transparent);
        faceHighlight = highlightNode;
        rootNode.attachChild(faceHighlight);
        faceHighlight.setCullHint(Spatial.CullHint.Always);
    }

    private void updateFaceHighlight() {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = cam.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = cam.getWorldCoordinates(cursorPos, 1f).subtract(origin).normalizeLocal();
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(0.1f);

        for (int i = 0; i < 1000; i++) {
            currentPos.addLocal(step);
            int bx = (int) Math.floor(currentPos.x);
            int by = (int) Math.floor(currentPos.y);
            int bz = (int) Math.floor(currentPos.z);

            if (blockWorld.getBlock(bx, by, bz) != BlockType.AIR) {
                Vector3f prevPos = currentPos.subtract(step);
                int placeX = (int) Math.floor(prevPos.x);
                int placeY = (int) Math.floor(prevPos.y);
                int placeZ = (int) Math.floor(prevPos.z);

                if (blockWorld.getBlock(placeX, placeY, placeZ) == BlockType.AIR) {
                    Vector3f attachDirection = new Vector3f(placeX - bx, placeY - by, placeZ - bz);
                    positionFaceHighlight(bx, by, bz, attachDirection);
                    faceHighlight.setCullHint(Spatial.CullHint.Never);
                }
                return;
            }
        }
        faceHighlight.setCullHint(Spatial.CullHint.Always);
    }

    private void positionFaceHighlight(int blockX, int blockY, int blockZ, Vector3f attachDir) {
        Vector3f faceCenter = new Vector3f(blockX, blockY, blockZ);

        if (attachDir.x > 0) faceCenter.x += GameConfig.HIGHLIGHT_OFFSET;
        else if (attachDir.x < 0) faceCenter.x -= GameConfig.HIGHLIGHT_OFFSET;
        else if (attachDir.y > 0) faceCenter.y += GameConfig.HIGHLIGHT_OFFSET;
        else if (attachDir.y < 0) faceCenter.y -= GameConfig.HIGHLIGHT_OFFSET;
        else if (attachDir.z > 0) faceCenter.z += GameConfig.HIGHLIGHT_OFFSET;
        else if (attachDir.z < 0) faceCenter.z -= GameConfig.HIGHLIGHT_OFFSET;

        faceHighlight.setLocalTranslation(faceCenter);

        Quaternion rotation = new Quaternion();
        if (attachDir.y != 0) {
            rotation.fromAngles(FastMath.HALF_PI, 0, 0);
        } else if (attachDir.x != 0) {
            rotation.fromAngles(0, FastMath.HALF_PI, 0);
        } else {
            rotation.fromAngles(0, 0, 0);
        }
        faceHighlight.setLocalRotation(rotation);
    }

    private void setupCustomCursor() {
        JmeCursor cursor = (JmeCursor) assetManager.loadAsset(GameConfig.CURSOR_PATH);
        cursor.setHeight(GameConfig.CURSOR_SIZE);
        cursor.setWidth(GameConfig.CURSOR_SIZE);
        inputManager.setMouseCursor(cursor);
    }

    private void addShopToScene() {
        shopModel = assetManager.loadModel(GameConfig.SHOP_MODEL);
        shopModel.setLocalTranslation(15, -1, 15);
        shopModel.setLocalScale(1f);
        rootNode.attachChild(shopModel);
    }

    private void addHouseToScene() {
        houseModel = assetManager.loadModel(GameConfig.HOUSE_MODEL);
        houseModel.setLocalTranslation(10, -1, 10);
        houseModel.setLocalScale(0.3f);
        rootNode.attachChild(houseModel);
    }
    
    private void updateLighting() {
        float intensity = dayNightCycle.getLightIntensity();
        sunLight.setColor(ColorRGBA.White.mult(intensity));

        float ambientIntensity = intensity * 0.1f + 0.05f; 
        ambientLight.setColor(ColorRGBA.White.mult(ambientIntensity));
    }

    private void createWorldBoundaries() {
        Material boundaryMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boundaryMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.3f)); // Белый полупрозрачный
        boundaryMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        float wallHeight = 25f;
        float wallThickness = 0.5f;

        float minX = GameConfig.WORLD_BOUNDARY_MIN_X;
        float maxX = GameConfig.WORLD_BOUNDARY_MAX_X;
        float minZ = GameConfig.WORLD_BOUNDARY_MIN_Z;
        float maxZ = GameConfig.WORLD_BOUNDARY_MAX_Z;

        float worldWidth = maxX - minX + 1 + wallThickness * 2; // Добавляем толщину стен
        float worldDepth = maxZ - minZ + 1 + wallThickness * 2;

        Box northWall = new Box(worldWidth/2f, wallHeight/2f, wallThickness/2f);
        Geometry northGeo = new Geometry("NorthBoundary", northWall);
        northGeo.setLocalTranslation((minX + maxX)/2f, wallHeight/2f, maxZ + 0.5f + wallThickness/2f);
        northGeo.setMaterial(boundaryMat);
        northGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(northGeo);

        Box southWall = new Box(worldWidth/2f, wallHeight/2f, wallThickness/2f);
        Geometry southGeo = new Geometry("SouthBoundary", southWall);
        southGeo.setLocalTranslation((minX + maxX)/2f, wallHeight/2f, minZ - 0.5f - wallThickness/2f);
        southGeo.setMaterial(boundaryMat);
        southGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(southGeo);

        Box eastWall = new Box(wallThickness/2f, wallHeight/2f, (worldDepth - wallThickness * 2)/2f);
        Geometry eastGeo = new Geometry("EastBoundary", eastWall);
        eastGeo.setLocalTranslation(maxX + 0.5f + wallThickness/2f, wallHeight/2f, (minZ + maxZ)/2f);
        eastGeo.setMaterial(boundaryMat);
        eastGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(eastGeo);

        Box westWall = new Box(wallThickness/2f, wallHeight/2f, (worldDepth - wallThickness * 2)/2f);
        Geometry westGeo = new Geometry("WestBoundary", westWall);
        westGeo.setLocalTranslation(minX - 0.5f - wallThickness/2f, wallHeight/2f, (minZ + maxZ)/2f);
        westGeo.setMaterial(boundaryMat);
        westGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(westGeo);
    }


    public Spatial getPlayer() { return player; }
    public MoneyDisplay getMoneyDisplay() { return moneyDisplay; }
    public Hotbar getHotbar() { return hotbar; }
    public SimpleBlockWorld getBlockWorld() { return blockWorld; }
    public PlantGrowthState getPlantGrowthState() { return growth; }
    public PlantFactory getPlantFactory() { return plantFactory; }
    public DayNightCycle getDayNightCycle() { return dayNightCycle; }
}
