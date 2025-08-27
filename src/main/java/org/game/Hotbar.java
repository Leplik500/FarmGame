package org.game;


import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.ui.Picture;

public class Hotbar {
    public static final int SLOT_COUNT = 9;

    private final Node root = new Node("HotbarRoot");
    private final Node slotsNode = new Node("Slots");
    private final Node iconsNode = new Node("Icons");
    private final Node highlightNode = new Node("Highlight");
    private final Geometry[] slotBG = new Geometry[SLOT_COUNT];
    private final Picture[] slotIcon = new Picture[SLOT_COUNT];
    private final HotbarItem[] items = new HotbarItem[SLOT_COUNT];

    private final AssetManager assetManager;
    // UI sizes (px)
    private final int slotSize = 64;
    private final int slotGap = 6;
    private int screenW;
    private int screenH;
    private int selected = 0;

    public Hotbar(Node guiNode, AssetManager assetManager, int screenW, int screenH) {
        this.assetManager = assetManager;
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
        updateHighlight();
    }

    private void buildSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            Quad q = new Quad(slotSize, slotSize);
            Geometry g = new Geometry("slotBG_" + i, q);
            Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            m.setColor("Color", new ColorRGBA(255, 255, 255, 0f));
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
        int marginBottom = 12;
        root.setLocalTranslation(x, marginBottom, 0);
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
            p.setImage(assetManager, item.iconPath, true); 
            int pad = 6;
            p.setWidth(slotSize - pad * 2);
            p.setHeight(slotSize - pad * 2);
            float x = slot * (slotSize + slotGap) + pad;
            p.setPosition(x, (float) pad);

            p.setQueueBucket(RenderQueue.Bucket.Gui); 

            iconsNode.attachChild(p);
            slotIcon[slot] = p;
        }
    }

    private void updateHighlight() {
        Geometry sel = (Geometry) highlightNode.getChild("selector");
        int border = 4;
        float x = selected * (slotSize + slotGap) - border;
        float y = -border;
        sel.setLocalTranslation(x, y, -1);
    }

    private void setSelected(int idx) {
        if (idx < 0) idx = 0;
        if (idx >= SLOT_COUNT) idx = SLOT_COUNT - 1;
        selected = idx;
        updateHighlight();
    }

    public void select(int idx) { setSelected(idx); }

    public void step(int delta) { setSelected((selected + delta + SLOT_COUNT) % SLOT_COUNT); }

    public void updateViewportSizeIfChanged(int w, int h) {
        if (w != screenW || h != screenH) {
            screenW = w;
            screenH = h;
            centerAlongBottom();
        }
    }
    
}

