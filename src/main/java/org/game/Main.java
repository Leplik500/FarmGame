package org.game;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main extends SimpleApplication implements AnalogListener, ActionListener {

    private Geometry player;          // «персонаж» — синий куб
    private final Node camTarget = new Node("CamTarget"); // сглаженная цель для камеры
    private SimpleBlockWorld blockWorld;
    private int selectedBlockType = BlockType.GRASS;
    private Geometry highlightBlock;
    private Spatial faceHighlight;
    private final Vector3f[] faceNormals = {
            new Vector3f(0, 1, 0),  // TOP
            new Vector3f(0, -1, 0), // BOTTOM  
            new Vector3f(1, 0, 0),  // RIGHT
            new Vector3f(-1, 0, 0), // LEFT
            new Vector3f(0, 0, 1),  // FRONT
            new Vector3f(0, 0, -1)  // BACK
    };


    public static void main(String[] args) {
//        AppSettings settings = CreateFullscreenSettings();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(800, 814);
        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    private static AppSettings CreateFullscreenSettings() {
        AppSettings settings = new AppSettings(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        int i = 0;
        settings.setResolution(modes[i].getWidth(), modes[i].getHeight());
        settings.setFrequency(modes[i].getRefreshRate());
        settings.setBitsPerPixel(modes[i].getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());
        return settings;
    }

    @Override
    public void simpleInitApp() {
        // Куб (игрок)
        Box box = new Box(1f, 1f, 1f);
        player = new Geometry("PlayerBox", box);
        Material matTea = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        player.setMaterial(matTea);
        rootNode.attachChild(player);

        // Инициализируем блочный мир
        blockWorld = new SimpleBlockWorld(rootNode, assetManager);

        // Создаём плоскую землю 20x20
        blockWorld.generateFlatWorld(100, 100);

        addSky();

        rootNode.attachChild(camTarget);
        camTarget.setLocalTranslation(player.getLocalTranslation());
        camTarget.setLocalRotation(player.getLocalRotation());

        configureCamera();
//        createHighlight();
        createFaceHighlight();
        registerInput();
    }

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

        chaseCam.setDefaultDistance(60f);
        chaseCam.setMinDistance(24f);
        chaseCam.setMaxDistance(80f);

        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 77f);
        chaseCam.setMinVerticalRotation(FastMath.DEG_TO_RAD * 77f);
        chaseCam.setMaxVerticalRotation(FastMath.DEG_TO_RAD * 77f);

        chaseCam.setDefaultHorizontalRotation(FastMath.DEG_TO_RAD * 35f);
        chaseCam.setLookAtOffset(new Vector3f(0, 1.2f, 0));

        // Вращение мышью только при MMB — оставляем
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        chaseCam.setDragToRotate(true);

        chaseCam.setRotationSpeed(1f);
        chaseCam.setZoomSensitivity(0.6f);
        chaseCam.setSmoothMotion(false);

        float fovDegrees = 40f; 
        float aspect = (float) cam.getWidth() / cam.getHeight();
        cam.setFrustumPerspective(fovDegrees, aspect, 0.1f, 1500);
    }

    private void registerInput() {
        inputManager.addMapping("moveForward",  new KeyTrigger(KeyInput.KEY_W), new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("moveBackward", new KeyTrigger(KeyInput.KEY_S), new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("moveRight",    new KeyTrigger(KeyInput.KEY_D), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("moveLeft",     new KeyTrigger(KeyInput.KEY_A), new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("displayPosition", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(this, "moveForward", "moveBackward", "moveRight", "moveLeft", "displayPosition");

        inputManager.addMapping("placeBlock", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("removeBlock", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("switchBlock", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "placeBlock", "removeBlock", "switchBlock");
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        float speed = 5f * tpf;

        Vector3f forward = cam.getDirection().clone();
        forward.y = 0f;
        if (forward.lengthSquared() > 0f) forward.normalizeLocal();

        Vector3f left = cam.getLeft().clone();
        left.y = 0f;
        if (left.lengthSquared() > 0f) left.normalizeLocal();

        Vector3f move = new Vector3f();
        if ("moveForward".equals(name))  move.addLocal(forward.mult(speed));
        if ("moveBackward".equals(name)) move.addLocal(forward.mult(-speed));
        if ("moveRight".equals(name))    move.addLocal(left.mult(-speed)); 
        if ("moveLeft".equals(name))     move.addLocal(left.mult(speed));  

        if (move.lengthSquared() > 0f) {
            player.move(move);

            // Поворачиваем игрока лицом к движению
            float targetYaw = (float) Math.atan2(move.x, move.z);
            float[] angles = player.getLocalRotation().toAngles(null);
            float currentYaw = angles[1];
            float diff = targetYaw - currentYaw;
            diff = (diff + FastMath.PI) % FastMath.TWO_PI - FastMath.PI;
            float step = diff * Math.min(1f, 10f * tpf);
            float newYaw = currentYaw + step;
            player.setLocalRotation(new Quaternion().fromAngles(0f, newYaw, 0f));
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;

        if ("placeBlock".equals(name) || "removeBlock".equals(name)) {
            handleBlockEdit("placeBlock".equals(name));
        }

        if ("switchBlock".equals(name)) {
            selectedBlockType++;
            if (selectedBlockType > BlockType.STONE) selectedBlockType = BlockType.GRASS;
            System.out.println("Selected: " + selectedBlockType);
        }
        
        if ("displayPosition".equals(name)) {
            System.out.println("Pos: " + player.getWorldTranslation());
        }
    }

    private void handleBlockEdit(boolean place) {
        Vector2f cursorPos = inputManager.getCursorPosition();
        Vector3f origin = cam.getWorldCoordinates(cursorPos, 0f);
        Vector3f direction = cam.getWorldCoordinates(cursorPos, 1f).subtract(origin).normalizeLocal();

        // Пошагово идём по лучу с шагом 0.1 единицы
        Vector3f currentPos = origin.clone();
        Vector3f step = direction.mult(0.1f);

        for (int i = 0; i < 1000; i++) { // максимум 100 единиц дистанции
            currentPos.addLocal(step);

            // Проверяем блок в этой позиции
            int bx = (int) Math.floor(currentPos.x);
            int by = (int) Math.floor(currentPos.y);
            int bz = (int) Math.floor(currentPos.z);

            int blockId = blockWorld.getBlock(bx, by, bz);
            if (blockId != BlockType.AIR) {
                if (place) {
                    // Берём предыдущую позицию (где был воздух)
                    Vector3f prevPos = currentPos.subtract(step);
                    int placeX = (int) Math.floor(prevPos.x);
                    int placeY = (int) Math.floor(prevPos.y);
                    int placeZ = (int) Math.floor(prevPos.z);

                    if (blockWorld.getBlock(placeX, placeY, placeZ) == BlockType.AIR) {
                        blockWorld.setBlock(placeX, placeY, placeZ, selectedBlockType);
                    }
                } else {
                    blockWorld.setBlock(bx, by, bz, BlockType.AIR);
                }
                return;
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f to   = player.getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();

        float camFollowPosSpeed = 7f;
        Vector3f posDelta = to.subtract(from)
                .multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);
//        updateHighlight();
        updateFaceHighlight();

    }

    private void createHighlight() {
        Box highlightMesh = new Box(0.51f, 0.51f, 0.51f); // чуть больше блока
        highlightBlock = new Geometry("Highlight", highlightMesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.3f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        highlightBlock.setMaterial(mat);
        highlightBlock.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(highlightBlock);
    }

    private void updateHighlight() {
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
                // Показываем где будет размещён блок
                Vector3f prevPos = currentPos.subtract(step);
                int placeX = (int) Math.floor(prevPos.x);
                int placeY = (int) Math.floor(prevPos.y);
                int placeZ = (int) Math.floor(prevPos.z);

                highlightBlock.setLocalTranslation(placeX, placeY, placeZ);
                highlightBlock.setCullHint(Spatial.CullHint.Never);
                return;
            }
        }

        highlightBlock.setCullHint(Spatial.CullHint.Always); // скрыть если ничего не найдено
    }

    private void createFaceHighlight() {
        Node highlightNode = new Node("ThickHighlight");

        // Создаём 4 тонких прямоугольника для "толстых" линий
        float thickness = 0.2f; // толщина "линии"
        float size = 0.52f;

        // Горизонтальные линии (верх и низ)
        Box topLine = new Box(size, thickness/2, thickness/2);
        Geometry topGeo = new Geometry("TopLine", topLine);
        topGeo.setLocalTranslation(0, size, 0);

        Box bottomLine = new Box(size, thickness/2, thickness/2);
        Geometry bottomGeo = new Geometry("BottomLine", bottomLine);
        bottomGeo.setLocalTranslation(0, -size, 0);

        // Вертикальные линии (лево и право)
        Box leftLine = new Box(thickness/2, size, thickness/2);
        Geometry leftGeo = new Geometry("LeftLine", leftLine);
        leftGeo.setLocalTranslation(-size, 0, 0);

        Box rightLine = new Box(thickness/2, size, thickness/2);
        Geometry rightGeo = new Geometry("RightLine", rightLine);
        rightGeo.setLocalTranslation(size, 0, 0);

        // Общий материал для всех линий
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

        faceHighlight = highlightNode; // используем Node вместо Geometry
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
                // Нашли твёрдый блок - показываем грань куда будет размещён новый блок
                Vector3f prevPos = currentPos.subtract(step);
                int placeX = (int) Math.floor(prevPos.x);
                int placeY = (int) Math.floor(prevPos.y);
                int placeZ = (int) Math.floor(prevPos.z);

                if (blockWorld.getBlock(placeX, placeY, placeZ) == BlockType.AIR) {
                    // Определяем какая грань блока будет "прикреплена"
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
        // Позиция центра грани существующего блока
        Vector3f faceCenter = new Vector3f(
                blockX + 0.5f * attachDir.x,
                blockY + 0.5f * attachDir.y,
                blockZ + 0.5f * attachDir.z
        );

        faceHighlight.setLocalTranslation(faceCenter);

        // Поворачиваем wireframe чтобы он был параллелен нужной грани
        Quaternion rotation = new Quaternion();

        if (attachDir.y != 0) {
            // Горизонтальная грань (верх/низ) - квадрат остаётся в плоскости XZ
            rotation.fromAngles(FastMath.HALF_PI, 0, 0);
        } else if (attachDir.x != 0) {
            // Вертикальная грань по X (лево/право) - поворот в плоскость YZ  
            rotation.fromAngles(0, 0, FastMath.HALF_PI);
        } else {
            // Вертикальная грань по Z (перед/зад) - квадрат в плоскости XY (по умолчанию)
            rotation.fromAngles(0, 0, 0);
        }

        faceHighlight.setLocalRotation(rotation);
    }




}
