package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

import java.util.HashMap;
import java.util.Map;

public class BlockType {
    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int PLOWED_DRY = 2;
    public static final int PLOWED_WET = 3;


    private static final Map<Integer, Material> materialCache = new HashMap<>();

    public static Material getMaterial(int blockId, AssetManager assetManager) {
        if (materialCache.containsKey(blockId)) {
            return materialCache.get(blockId);
        }
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        switch (blockId) {
            case GRASS: {
                Texture tex = assetManager.loadTexture("Textures/grass7.jpg");
                tex.setWrap(Texture.WrapMode.Repeat);
                mat.setTexture("ColorMap", tex);
                break;
            }
            case PLOWED_DRY: {
                Texture tex = assetManager.loadTexture("Textures/plowed_dirt" +
                        ".png");
                tex.setWrap(Texture.WrapMode.Repeat);
                mat.setTexture("ColorMap", tex);
                break;
            }
            case PLOWED_WET: {
                Texture tex = assetManager.loadTexture("Textures" +
                        "/plowed_dirt_wet.png");
                tex.setWrap(Texture.WrapMode.Repeat);
                mat.setTexture("ColorMap", tex);
                break;
            }
            default:
                mat.setColor("Color", ColorRGBA.White);
        }
        materialCache.put(blockId, mat);
        return mat;
    }
}
