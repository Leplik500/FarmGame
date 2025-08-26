package org.game;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
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

public class Main extends SimpleApplication implements ActionListener {

    private static final float MIN_ANIMATION_DURATION = 0.5f; // Minimum time 
    private final Node camTarget = new Node("CamTarget");
    private Spatial player;
    private SimpleBlockWorld blockWorld;
    private Spatial faceHighlight;
    private AnimComposer animComposer;
    // before allowing animation change
    private PlayerInputHandler inputHandler;
    private PlayerMovementController movementController;
    private PlayerAnimationController animationController;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(800, 814);
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        player = assetManager.loadModel("Models/rhea_wilson.glb");
        player.depthFirstTraversal(spatial -> {
            if (spatial.getControl(AnimComposer.class) != null) {
                animComposer = spatial.getControl(AnimComposer.class);
            }
        });

        if (animComposer != null) {
            System.out.println("Available animations: " + animComposer.getAnimClipsNames());

            // Set initial animation and lock it briefly
            // Animation state - completely different approach
            String targetAnimation = "animation.lael.idlemain";
            setAndLockAnimation(targetAnimation);
        } else {
            System.err.println("No AnimComposer found!");
        }

        rootNode.attachChild(player);

        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(-1f, -1f, -1f).normalizeLocal());
        light.setColor(ColorRGBA.White.mult(2f));
        rootNode.addLight(light);

        blockWorld = new SimpleBlockWorld(rootNode, assetManager);
        blockWorld.generateFlatWorld(100, 100);
        addSky();
        rootNode.attachChild(camTarget);
        camTarget.setLocalTranslation(player.getLocalTranslation());
        camTarget.setLocalRotation(player.getLocalRotation());
        configureCamera();
        createFaceHighlight();
        registerInput();
        setupCustomCursor();

        inputHandler = new PlayerInputHandler();
        movementController = new PlayerMovementController(player, cam);
        animationController = new PlayerAnimationController(animComposer);
    }

    private void registerInput() {
        inputManager.addMapping("moveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("moveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("moveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("moveRight", new KeyTrigger(KeyInput.KEY_D));

        inputManager.addListener(this, "moveForward", "moveBackward", "moveLeft", "moveRight");
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

        // Update animation
        animationController.update(tpf, inputHandler.isWalking(), inputHandler.isRunning());

        // Update camera
        Vector3f to = player.getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();
        float camFollowPosSpeed = 7f;
        Vector3f posDelta = to.subtract(from).multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);

        updateFaceHighlight();
    }


    private void setAndLockAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            try {
                animComposer.setCurrentAction(animationName);

                // SHORTER lock time for idle animations
                float animationLockTime;
                if (animationName.equals("animation.lael.idlemain")) {
                    animationLockTime = 0.15f; // Shorter for idle
                } else {
                    animationLockTime = MIN_ANIMATION_DURATION; // Normal for walk/run
                }

                System.out.println("Animation set and locked: " + animationName + " for " + animationLockTime + "s");
            } catch (Exception e) {
                System.err.println("Error setting animation: " + e.getMessage());
            }
        }
    }


    // All other methods remain the same...
    private void addSky() {
        Texture px = assetManager.loadTexture("SkyBox/px.png");
        Texture nx = assetManager.loadTexture("SkyBox/nx.png");
        Texture py = assetManager.loadTexture("SkyBox/py.png");
        Texture ny = assetManager.loadTexture("SkyBox/ny.png");
        Texture pz = assetManager.loadTexture("SkyBox/pz.png");
        Texture nz = assetManager.loadTexture("SkyBox/nz.png");
        Spatial sky = SkyFactory.createSky(assetManager, px, nx, py, ny, pz, nz);
        rootNode.attachChild(sky);
    }

    private void configureCamera() {
        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, camTarget, inputManager);
        chaseCam.setDefaultDistance(40f);
        chaseCam.setMinDistance(24f);
        chaseCam.setMaxDistance(80f);
        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 77f);
        chaseCam.setMinVerticalRotation(FastMath.DEG_TO_RAD * 77f);
        chaseCam.setMaxVerticalRotation(FastMath.DEG_TO_RAD * 77f);
        chaseCam.setDefaultHorizontalRotation(FastMath.DEG_TO_RAD * 35f);
        chaseCam.setLookAtOffset(new Vector3f(0, 1.2f, 0));
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        chaseCam.setDragToRotate(true);
        chaseCam.setRotationSpeed(1f);
        chaseCam.setZoomSensitivity(0.6f);
        chaseCam.setSmoothMotion(false);
        float fovDegrees = 40f;
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustumPerspective(fovDegrees, aspect, 0.1f, 1500);
    }

    private void createFaceHighlight() {
        Node highlightNode = new Node("ThickHighlight");
        float thickness = 0.2f;
        float size = 0.52f;

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
        mat.setColor("Color", new ColorRGBA(1f, 1f, 0f, 1.0f));
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

        if (attachDir.x > 0) faceCenter.x += 0.501f;
        else if (attachDir.x < 0) faceCenter.x -= 0.501f;
        else if (attachDir.y > 0) faceCenter.y += 0.501f;
        else if (attachDir.y < 0) faceCenter.y -= 0.501f;
        else if (attachDir.z > 0) faceCenter.z += 0.501f;
        else if (attachDir.z < 0) faceCenter.z -= 0.501f;

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
        JmeCursor cursor = (JmeCursor) assetManager.loadAsset("Cursors/hand.cur");
        cursor.setHeight(32);
        cursor.setWidth(32);
        inputManager.setMouseCursor(cursor);
    }
}
