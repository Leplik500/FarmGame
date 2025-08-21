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
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.RectangleMesh;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureCubeMap;
import com.jme3.util.SkyFactory;

import java.awt.*;

public class Main extends SimpleApplication implements AnalogListener, ActionListener {

    private Geometry teaGeom;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();
        int i=0;
        settings.setResolution(modes[i].getWidth(),modes[i].getHeight());
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
        teaGeom = new Geometry("PlayerBox", box);
        Material matTea = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teaGeom.setMaterial(matTea);
        rootNode.attachChild(teaGeom);

        // Пол
        Material matGround = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture groundTex = assetManager.loadTexture("Textures/grass7.jpg");
        groundTex.setWrap(Texture.WrapMode.Repeat);   // критично для тайлинга
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


        System.out.println("CWD = " + System.getProperty("user.dir"));
        Texture px = assetManager.loadTexture("SkyBox/px.png");  
        Texture nx = assetManager.loadTexture("SkyBox/nx.png");  
        Texture py = assetManager.loadTexture("SkyBox/py.png"); 
        Texture ny = assetManager.loadTexture("SkyBox/ny.png");  
        Texture pz = assetManager.loadTexture("SkyBox/pz.png");  
        Texture nz = assetManager.loadTexture("SkyBox/nz.png");  
        
// Создаём небосвод из 6 граней
        Spatial sky = SkyFactory.createSky(assetManager, px, nx, py, ny, pz, nz);
        rootNode.attachChild(sky);

        // Отключить FlyCam (обязательно)
        flyCam.setEnabled(false);

        ChaseCamera chaseCam = new ChaseCamera(cam, teaGeom, inputManager);

        // Дистанция и её границы (камера не подлетает слишком близко/далеко)
        chaseCam.setDefaultDistance(14f);
        chaseCam.setMinDistance(10f);
        chaseCam.setMaxDistance(18f);

        // Высота камеры (поднимаем камеру над персонажем)
        chaseCam.setDefaultVerticalRotation(FastMath.DEG_TO_RAD * 50f); // наклон вниз ~50°
        chaseCam.setMinVerticalRotation(FastMath.DEG_TO_RAD * 35f);     // не даём опускаться слишком низко
        chaseCam.setMaxVerticalRotation(FastMath.DEG_TO_RAD * 70f);     // и задирать слишком высоко

        // Горизонтальный угол (чуть сбоку от персонажа)
        chaseCam.setDefaultHorizontalRotation(FastMath.DEG_TO_RAD * 35f);

        // Точка прицеливания немного выше центра (голова персонажа)
        chaseCam.setLookAtOffset(new Vector3f(0, 1.2f, 0));

        // Управление: вращение только при средней кнопке (как у вас), зум — колёсиком
        chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        chaseCam.setDragToRotate(true);         // вращение только при удержании MMB
        chaseCam.setRotationSpeed(1.5f);        // помедленнее, «консольное» ощущение
        chaseCam.setZoomSensitivity(0.75f);     // зум не резкий
        chaseCam.setSmoothMotion(true);         // лёгкое сглаживание при движении

        // Ввод
        registerInput();
    }

    public void registerInput() {
        inputManager.addMapping("moveForward", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("moveBackward", new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("moveRight", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("moveLeft", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("displayPosition", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addListener(this, "moveForward", "moveBackward", "moveRight", "moveLeft");
        inputManager.addListener(this, "displayPosition");
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        float speed = 5f * tpf;

        // Горизонтальные (по земле) направления от камеры
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
        if ("moveLeft".equals(name))     move.addLocal(left.mult(speed));  // влево = +left

        if (move.lengthSquared() > 0f) {
            teaGeom.move(move);
        }
    }


    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        if ("displayPosition".equals(name) && keyPressed) {
            System.out.println("Pos: " + teaGeom.getWorldTranslation());
        }
    }
}
