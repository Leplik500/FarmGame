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
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import java.util.HashSet;
import java.util.Set;

public class Main extends SimpleApplication implements ActionListener {

    private Spatial player;
    private final Node camTarget = new Node("CamTarget");
    private SimpleBlockWorld blockWorld;
    private Spatial faceHighlight;
    private AnimComposer animComposer;

    // Movement state
    private final Set<String> pressedKeys = new HashSet<>();
    private boolean isWalking = false;
    private boolean isRunning = false;

    // Animation state - completely different approach
    private String targetAnimation = "animation.lael.idlemain";
    private boolean animationLocked = false;
    private float animationLockTime = 0f;
    private static final float MIN_ANIMATION_DURATION = 0.5f; // Minimum time 
    // before allowing animation change

    // Double-tap detection for W
    private long lastWPressTime = 0L;
    private static final long DOUBLE_TAP_NS = 600_000_000L; // 300 ms

    // Speeds
    private float walkSpeed = 5f;
    private float runSpeed = 10f;

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
    }

    private void registerInput() {
        inputManager.addMapping("moveForward",  new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("moveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("moveLeft",     new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("moveRight",    new KeyTrigger(KeyInput.KEY_D));

        inputManager.addListener(this, "moveForward", "moveBackward", "moveLeft", "moveRight");
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            pressedKeys.add(name);

            if ("moveForward".equals(name)) {
                long now = System.nanoTime();
                if (now - lastWPressTime <= DOUBLE_TAP_NS) {
                    isRunning = true;
                    System.out.println("Run mode activated");
                }
                lastWPressTime = now;
            }
        } else {
            pressedKeys.remove(name);

            if ("moveForward".equals(name)) {
                if (isRunning) {
                    isRunning = false;
                    System.out.println("Run mode deactivated");
                }
            }
        }

        boolean shouldBeWalking = !pressedKeys.isEmpty();
        if (shouldBeWalking != isWalking) {
            isWalking = shouldBeWalking;

            // REFINED: Only unlock if switching from idle to movement AND lock time is almost expired
            if (isWalking && animationLocked &&
                    targetAnimation.equals("animation.lael.idlemain") &&
                    animationLockTime < 0.1f) { // Only unlock if less than 0.1s remaining
                animationLocked = false;
                System.out.println("Animation unlocked due to movement start");
            }

            updateTargetAnimation();
        }
    }


    @Override
    public void simpleUpdate(float tpf) {
        // Update animation lock timer
        if (animationLocked) {
            animationLockTime -= tpf;
            if (animationLockTime <= 0) {
                animationLocked = false;
            }
        }

        // Handle movement
        if (!pressedKeys.isEmpty()) {
            handleMovement(tpf);
            updateTargetAnimation();
        }

        // Update camera
        Vector3f to = player.getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();
        float camFollowPosSpeed = 7f;
        Vector3f posDelta = to.subtract(from).multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);

        updateFaceHighlight();
    }

    private void updateTargetAnimation() {
        String newTarget;
        if (isWalking) {
            newTarget = isRunning ? "animation.lael.run" : "animation.lael.walk";
        } else {
            newTarget = "animation.lael.idlemain";
            isRunning = false;
        }

        if (!newTarget.equals(targetAnimation)) {
            targetAnimation = newTarget;
            if (!animationLocked) {
                setAndLockAnimation(targetAnimation);
            }
        }
    }



    private void setAndLockAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            try {
                animComposer.setCurrentAction(animationName);
                animationLocked = true;

                // SHORTER lock time for idle animations
                if (animationName.equals("animation.lael.idlemain")) {
                    animationLockTime = 0.15f; // Shorter for idle
                } else {
                    animationLockTime = MIN_ANIMATION_DURATION; // Normal for walk/run
                }

                System.out.println("Animation set and locked: " + animationName +
                        " for " + animationLockTime + "s");
            } catch (Exception e) {
                System.err.println("Error setting animation: " + e.getMessage());
            }
        }
    }


    private void handleMovement(float tpf) {
        Vector3f forward = cam.getDirection().clone();
        forward.y = 0f;
        forward.normalizeLocal();

        Vector3f left = cam.getLeft().clone();
        left.y = 0f;
        left.normalizeLocal();

        Vector3f movement = new Vector3f();

        if (pressedKeys.contains("moveForward"))  movement.addLocal(forward);
        if (pressedKeys.contains("moveBackward")) movement.addLocal(forward.negate());
        if (pressedKeys.contains("moveRight"))    movement.addLocal(left.negate());
        if (pressedKeys.contains("moveLeft"))     movement.addLocal(left);

        if (movement.lengthSquared() > 0f) {
            movement.normalizeLocal();
            float currentSpeed = isRunning ? runSpeed : walkSpeed;
            Vector3f step = movement.mult(currentSpeed * tpf);
            player.move(step);

            float targetYaw = (float) Math.atan2(step.x, step.z);
            float[] angles = player.getLocalRotation().toAngles(null);
            float currentYaw = angles[1];
            float diff = targetYaw - currentYaw;
            diff = (diff + FastMath.PI) % FastMath.TWO_PI - FastMath.PI;
            float lerp = Math.min(1f, 10f * tpf);
            float newYaw = currentYaw + diff * lerp;
            player.setLocalRotation(new Quaternion().fromAngles(0f, newYaw, 0f));
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

        Box topLine = new Box(size, thickness/2, thickness/2);
        Geometry topGeo = new Geometry("TopLine", topLine);
        topGeo.setLocalTranslation(0, size, 0);

        Box bottomLine = new Box(size, thickness/2, thickness/2);
        Geometry bottomGeo = new Geometry("BottomLine", bottomLine);
        bottomGeo.setLocalTranslation(0, -size, 0);

        Box leftLine = new Box(thickness/2, size, thickness/2);
        Geometry leftGeo = new Geometry("LeftLine", leftLine);
        leftGeo.setLocalTranslation(-size, 0, 0);

        Box rightLine = new Box(thickness/2, size, thickness/2);
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
        JmeCursor cursor = (JmeCursor) assetManager.loadAsset("Textures/Cursors/KOFJI/hand.cur");
        cursor.setHeight(32);
        cursor.setWidth(32);
        inputManager.setMouseCursor(cursor);
    }
}
