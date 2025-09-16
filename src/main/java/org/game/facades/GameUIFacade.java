package org.game.facades;

import com.jme3.asset.AssetManager;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import org.game.systems.InventoryManager;
import org.game.systems.RaycastHelper;
import org.game.ui.Hotbar;
import org.game.ui.HotbarItem;
import org.game.ui.MoneyDisplay;
import org.game.ui.ShopUI;
import org.game.utils.GameConfig;
import org.game.utils.ItemIds;
import org.game.world.BlockWorld;
import org.game.world.Vector3i;

public class GameUIFacade {
    private final Hotbar hotbar;
    private final MoneyDisplay moneyDisplay;
    private final ShopUI shopUI;
    private final InventoryManager inventoryManager;
    private final RaycastHelper raycastHelper;
    private final ChaseCamera chaseCamera;
    private final Spatial faceHighlight;

    public GameUIFacade(AssetManager assetManager, Node guiNode, Node rootNode,
                        Camera camera, InputManager inputManager, BlockWorld blockWorld,
                        Node camTarget) {

        configureCamera(camera);
        this.chaseCamera = new ChaseCamera(camera, camTarget, inputManager);
        setupChaseCamera();

        this.faceHighlight = createFaceHighlight(assetManager, rootNode);
        setupCustomCursor(assetManager, inputManager);

        this.raycastHelper = new RaycastHelper(camera, inputManager, blockWorld);
        this.hotbar = new Hotbar(guiNode, assetManager, camera.getWidth(), camera.getHeight());

        hotbar.addItemToSlot(2, new HotbarItem(ItemIds.WATERING_CAN, GameConfig.WATERING_CAN_ITEM));
        hotbar.addItemToSlot(3, new HotbarItem(ItemIds.HOE, GameConfig.HOE_ITEM));

        this.moneyDisplay = new MoneyDisplay(guiNode, assetManager, camera.getWidth());
        moneyDisplay.addMoney(10);

        this.inventoryManager = new InventoryManager(hotbar);
        this.shopUI = new ShopUI(guiNode, assetManager, moneyDisplay, inventoryManager);
    }

    private void configureCamera(Camera camera) {
        float fovDegrees = GameConfig.CAM_FOV_DEGREES;
        float aspect = (float) camera.getWidth() / camera.getHeight();
        camera.setFrustumPerspective(fovDegrees, aspect, 0.1f, 1500);
    }

    private void setupChaseCamera() {
        chaseCamera.setDefaultDistance(GameConfig.CAM_DEFAULT_DISTANCE);
        chaseCamera.setMinDistance(GameConfig.CAM_MIN_DISTANCE);
        chaseCamera.setMaxDistance(GameConfig.CAM_MAX_DISTANCE);
        chaseCamera.setDefaultVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCamera.setMinVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCamera.setMaxVerticalRotation(GameConfig.CAM_VERTICAL_ANGLE);
        chaseCamera.setDefaultHorizontalRotation(GameConfig.CAM_HORIZONTAL_ANGLE);
        chaseCamera.setLookAtOffset(GameConfig.CAM_LOOK_OFFSET);
        chaseCamera.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));
        chaseCamera.setZoomInTrigger();
        chaseCamera.setZoomOutTrigger();
        chaseCamera.setDragToRotate(true);
        chaseCamera.setRotationSpeed(GameConfig.CAM_ROTATION_SPEED);
        chaseCamera.setZoomSensitivity(GameConfig.CAM_ZOOM_SENSITIVITY);
        chaseCamera.setSmoothMotion(false);
    }

    private Spatial createFaceHighlight(AssetManager assetManager, Node rootNode) {
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
        rootNode.attachChild(highlightNode);
        highlightNode.setCullHint(Spatial.CullHint.Always);
        return highlightNode;
    }

    private void setupCustomCursor(AssetManager assetManager, InputManager inputManager) {
        JmeCursor cursor = (JmeCursor) assetManager.loadAsset(GameConfig.CURSOR_PATH);
        cursor.setHeight(GameConfig.CURSOR_SIZE);
        cursor.setWidth(GameConfig.CURSOR_SIZE);
        inputManager.setMouseCursor(cursor);
    }

    public void updateFaceHighlight() {
        RaycastHelper.RaycastResult result = raycastHelper.getRaycastForHighlight();

        if (result == null) {
            faceHighlight.setCullHint(Spatial.CullHint.Always);
            return;
        }

        Vector3i blockPos = result.blockPos();
        Vector3f attachDirection = result.attachDirection();
        positionFaceHighlight(blockPos.x(), blockPos.y(), blockPos.z(), attachDirection);
        faceHighlight.setCullHint(Spatial.CullHint.Never);
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

    public void updateViewport(int width, int height) {
        if (hotbar != null) {
            hotbar.updateViewportSizeIfChanged(width, height);
            moneyDisplay.updatePosition(width);
        }
    }

    public Hotbar getHotbar() { return hotbar; }
    public MoneyDisplay getMoneyDisplay() { return moneyDisplay; }
    public ShopUI getShopUI() { return shopUI; }
    public InventoryManager getInventoryManager() { return inventoryManager; }

    public ChaseCamera getChaseCamera() { return chaseCamera; }
}
