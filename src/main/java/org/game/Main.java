package org.game;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.RectangleMesh;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Main extends SimpleApplication implements AnalogListener, ActionListener {

    private Geometry player;          // «персонаж» — синий куб
    private final Node camTarget = new Node("CamTarget"); // сглаженная цель для камеры

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        int i = 0;
        settings.setResolution(modes[i].getWidth(), modes[i].getHeight());
        settings.setFrequency(modes[i].getRefreshRate());
        settings.setBitsPerPixel(modes[i].getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());

        Main app = new Main();
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Куб (игрок)
        Box box = new Box(1f, 1f, 1f);
        player = new Geometry("PlayerBox", box);
        Material matTea = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        player.setMaterial(matTea);
        rootNode.attachChild(player);

        // Пол с тайлингом
        Material matGround = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture groundTex = assetManager.loadTexture("Textures/grass7.jpg");
        groundTex.setWrap(Texture.WrapMode.Repeat);
        groundTex.setMagFilter(Texture.MagFilter.Bilinear);
        groundTex.setMinFilter(Texture.MinFilter.Trilinear);
        matGround.setTexture("ColorMap", groundTex);

        Geometry ground = new Geometry("ground", new RectangleMesh(
                new Vector3f(-25, -1,  25),
                new Vector3f( 25, -1,  25),
                new Vector3f(-25, -1, -25)));
        ground.getMesh().scaleTextureCoordinates(new Vector2f(16f, 16f));
        ground.setMaterial(matGround);
        rootNode.attachChild(ground);

        Texture px = assetManager.loadTexture("SkyBox/px.png");
        Texture nx = assetManager.loadTexture("SkyBox/nx.png");
        Texture py = assetManager.loadTexture("SkyBox/py.png");
        Texture ny = assetManager.loadTexture("SkyBox/ny.png");
        Texture pz = assetManager.loadTexture("SkyBox/pz.png");
        Texture nz = assetManager.loadTexture("SkyBox/nz.png");
        Spatial sky = SkyFactory.createSky(assetManager, px, nx, py, ny, pz, nz);
        rootNode.attachChild(sky);

        // camTarget: стартовая позиция и привязка в сцену
        rootNode.attachChild(camTarget);
        camTarget.setLocalTranslation(player.getLocalTranslation());
        camTarget.setLocalRotation(player.getLocalRotation());

        configureCamera();
        registerInput();
    }

    private void configureCamera() {
        flyCam.setEnabled(false);

        // Привязываем камеру к camTarget, а не к игроку
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

        chaseCam.setRotationSpeed(1f);     // влияет на орбиту мышью
        chaseCam.setZoomSensitivity(0.6f); // менее резкий зум
        chaseCam.setSmoothMotion(false);

        float fovDegrees = 40f; // подбери в диапазоне 28–40
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
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        float speed = 5f * tpf;

        // Движение относительно направления камеры по плоскости XZ
        Vector3f forward = cam.getDirection().clone();
        forward.y = 0f;
        if (forward.lengthSquared() > 0f) forward.normalizeLocal();

        Vector3f left = cam.getLeft().clone();
        left.y = 0f;
        if (left.lengthSquared() > 0f) left.normalizeLocal();

        Vector3f move = new Vector3f();
        if ("moveForward".equals(name))  move.addLocal(forward.mult(speed));
        if ("moveBackward".equals(name)) move.addLocal(forward.mult(-speed));
        if ("moveRight".equals(name))    move.addLocal(left.mult(-speed)); // вправо = -left
        if ("moveLeft".equals(name))     move.addLocal(left.mult(speed));  // влево  = +left

        if (move.lengthSquared() > 0f) {
            player.move(move);

            // Поворачиваем игрока лицом к движению (опционально)
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
        if ("displayPosition".equals(name) && isPressed) {
            System.out.println("Pos: " + player.getWorldTranslation());
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
    }

}
