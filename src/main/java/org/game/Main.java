package org.game;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.ChaseCamera;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
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
    private Spatial player;
    private SimpleBlockWorld blockWorld;
    private Spatial faceHighlight;
    private AnimComposer animComposer;
    private PlayerInputHandler inputHandler;
    private PlayerMovementController movementController;
    private PlayerAnimationController animationController;
    private Hotbar hotbar;
    private ChaseCamera chaseCam;

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
        player = assetManager.loadModel(GameConfig.MODEL_PLAYER);
        player.depthFirstTraversal(spatial -> {
            if (spatial.getControl(AnimComposer.class) != null) {
                animComposer = spatial.getControl(AnimComposer.class);
            }
        });

        if (animComposer != null) {
            System.out.println("Available animations: " + animComposer.getAnimClipsNames());
            String targetAnimation = GameConfig.ANIM_IDLE;
            setAndLockAnimation(targetAnimation);
        } else {
            System.err.println("No AnimComposer found!");
        }

        rootNode.attachChild(player);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(GameConfig.LIGHT_DIRECTION.normalizeLocal());
        light.setColor(ColorRGBA.White.mult(GameConfig.LIGHT_INTENSITY));
        rootNode.addLight(light);

        blockWorld = new SimpleBlockWorld(rootNode, assetManager);
        blockWorld.generateFlatWorld(GameConfig.WORLD_SIZE_X, GameConfig.WORLD_SIZE_Z);
        FarmlandMoistureState moisture = new FarmlandMoistureState(blockWorld);
        stateManager.attach(moisture);

        addSky();
        rootNode.attachChild(camTarget);
        camTarget.setLocalTranslation(player.getLocalTranslation());
        camTarget.setLocalRotation(player.getLocalRotation());
        configureCamera();
        createFaceHighlight();
        setupCustomCursor();

        hotbar = new Hotbar(guiNode, assetManager, cam.getWidth(), cam.getHeight());
        hotbar.setItem(0, new HotbarItem("apple_seeds", "Textures/apple_seeds" +
                ".png"));
        hotbar.setItem(1, new HotbarItem("tomato_seeds", "Textures" +
                "/tomato_seeds" +
                ".png"));
        hotbar.setItem(2, new HotbarItem("watering_can", "Textures" +
                "/watering_can" +
                ".png"));
        hotbar.setItem(3, new HotbarItem("hoe", "Textures/hoe.png"));
        inputHandler = new PlayerInputHandler();
        movementController = new PlayerMovementController(player, cam);
        animationController = new PlayerAnimationController(animComposer);

        GameInputRouter router = new GameInputRouter(inputManager);
        CameraInput cameraInput = new CameraInput(chaseCam);
        HotbarInput hotbarInput = new HotbarInput(hotbar, cameraInput);
        MovementInput movementInput = new MovementInput(inputHandler);
        WorldInteractionInput worldInput = new WorldInteractionInput(
                blockWorld, hotbar, cam, inputManager, moisture);

        router.addActionModule(cameraInput);
        router.addAnalogModule(cameraInput);
        router.addActionModule(hotbarInput);
        router.addAnalogModule(hotbarInput);
        router.addActionModule(movementInput);
        router.addActionModule(worldInput);
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            inputHandler.handleKeyPress(name);
        } else {
            inputHandler.handleKeyRelease(name);
        }

        if (inputHandler.updateWalkingState()) {
            animationController.forceUnlockIfMoving(inputHandler.isWalking());
        }
    }


    @Override
    public void simpleUpdate(float tpf) {
        if (inputHandler.isWalking()) {
            movementController.handleMovement(inputHandler.getPressedKeys(), inputHandler.isRunning(), tpf);
        }

        animationController.update(tpf, inputHandler.isWalking(), inputHandler.isRunning());

        Vector3f to = player.getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();
        float camFollowPosSpeed = GameConfig.CAM_FOLLOW_SPEED;
        Vector3f posDelta = to.subtract(from).multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);

        updateFaceHighlight();
        if (hotbar != null) {
            hotbar.updateViewportSizeIfChanged(cam.getWidth(), cam.getHeight());
        }
    }


    private void setAndLockAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            try {
                animComposer.setCurrentAction(animationName);

                float animationLockTime;
                if (animationName.equals("animation.lael.idlemain")) {
                    animationLockTime = GameConfig.IDLE_ANIMATION_DURATION;
                } else {
                    animationLockTime = GameConfig.MIN_ANIMATION_DURATION;
                }

                System.out.println("Animation set and locked: " + animationName + " for " + animationLockTime + "s");
            } catch (Exception e) {
                System.err.println("Error setting animation: " + e.getMessage());
            }
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

}
