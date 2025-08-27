package org.game;


import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;

public class Hotbar implements ActionListener, AnalogListener {
    public static final int SLOT_COUNT = 9;

    private final Node root = new Node("HotbarRoot");
    private final Node slotsNode = new Node("Slots");
    private final Node iconsNode = new Node("Icons");
    private final Node highlightNode = new Node("Highlight");
    private final Geometry[] slotBG = new Geometry[SLOT_COUNT];
    private final Picture[] slotIcon = new Picture[SLOT_COUNT];
    private final HotbarItem[] items = new HotbarItem[SLOT_COUNT];

    private final AssetManager assetManager;
    private final InputManager input;
    private final Node guiNode;
    // UI sizes (px)
    private final int slotSize = 64;
    private final int slotGap = 6;
    private final int marginBottom = 12;
    private int screenW;
    private int screenH;
    private int selected = 0;

    public Hotbar(Node guiNode, AssetManager assetManager, InputManager input, int screenW, int screenH) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;

        // GUI bucket & hierarchy
        root.setQueueBucket(RenderQueue.Bucket.Gui);
        root.attachChild(slotsNode);
        root.attachChild(iconsNode);
        root.attachChild(highlightNode);
        guiNode.attachChild(root);

        buildSlots();
        buildHighlight();
        centerAlongBottom();
        setupInput();
        updateHighlight();
    }

    private void buildSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            Quad q = new Quad(slotSize, slotSize);
            Geometry g = new Geometry("slotBG_" + i, q);
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setColor("Color", new ColorRGBA(0, 0, 0, 0.45f));
            g.setMaterial(m);

            g.setQueueBucket(RenderQueue.Bucket.Gui);

            float x = i * (slotSize + slotGap);
            g.setLocalTranslation(x, 0, 0);
            slotsNode.attachChild(g);
            slotBG[i] = g;
        }
    }

    private void buildHighlight() {
        int border = 4;
        Quad q = new Quad(slotSize + border * 2, slotSize + border * 2);
        Geometry sel = new Geometry("selector", q);
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", new ColorRGBA(1f, 1f, 0.2f, 0.6f));
        sel.setMaterial(m);

        sel.setQueueBucket(RenderQueue.Bucket.Gui); // ВАЖНО [5]

        highlightNode.attachChild(sel);
    }

    private void centerAlongBottom() {
        int totalW = SLOT_COUNT * slotSize + (SLOT_COUNT - 1) * slotGap;
        int x = (screenW - totalW) / 2;
        int y = marginBottom;
        root.setLocalTranslation(x, y, 0);
    }

    private void setupInput() {
        // number keys 1-9
        for (int i = 0; i < SLOT_COUNT; i++) {
            String map = "hotbar_" + (i + 1);
            input.addMapping(map, new KeyTrigger(KeyInput.KEY_1 + i));
            input.addListener(this, map);
        }
        // mouse wheel cycle
        input.addMapping("hotbar_prev", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        input.addMapping("hotbar_next", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        input.addListener(this, "hotbar_prev", "hotbar_next");
    }

    public void setItem(int slot, HotbarItem item) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        items[slot] = item;
        updateSlotIcon(slot);
    }

    public HotbarItem getSelectedItem() {
        return items[selected];
    }

    private void updateSlotIcon(int slot) {
        if (slotIcon[slot] != null) {
            slotIcon[slot].removeFromParent();
            slotIcon[slot] = null;
        }
        HotbarItem item = items[slot];
        if (item != null && item.iconPath != null && !item.iconPath.isEmpty()) {
            Picture p = new Picture("icon_" + slot);
            p.setImage(assetManager, item.iconPath, true); // true = альфа [5]
            int pad = 6;
            p.setWidth(slotSize - pad * 2);
            p.setHeight(slotSize - pad * 2);
            float x = slot * (slotSize + slotGap) + pad;
            float y = pad;
            p.setPosition(x, y);

            p.setQueueBucket(RenderQueue.Bucket.Gui); // ВАЖНО [5]

            iconsNode.attachChild(p);
            slotIcon[slot] = p;
        }
    }

    private void updateHighlight() {
        Geometry sel = (Geometry) highlightNode.getChild("selector");
        int border = 4;
        float x = selected * (slotSize + slotGap) - border;
        float y = -border;
        sel.setLocalTranslation(x, y, -1); // Z slightly back so icons render above
    }

    private void setSelected(int idx) {
        if (idx < 0) idx = 0;
        if (idx >= SLOT_COUNT) idx = SLOT_COUNT - 1;
        selected = idx;
        updateHighlight();
    }

    private void step(int delta) {
        setSelected((selected + delta + SLOT_COUNT) % SLOT_COUNT);
    }

    public void updateViewportSizeIfChanged(int w, int h) {
        if (w != screenW || h != screenH) {
            screenW = w;
            screenH = h;
            centerAlongBottom(); // перецентровать
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (name.startsWith("hotbar_")) {
            if ("hotbar_prev".equals(name) || "hotbar_next".equals(name)) {
                return;
            }
            String tail = name.substring("hotbar_".length());
            try {
                int idx = Integer.parseInt(tail) - 1;
                setSelected(idx);
            } catch (NumberFormatException ignored) { /* ничего */ }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if ("hotbar_next".equals(name)) step(+1);
        else if ("hotbar_prev".equals(name)) step(-1);
    }
}

