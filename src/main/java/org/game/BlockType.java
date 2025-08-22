package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;

public class BlockType {
    public static final int AIR = 0;
    public static final int GRASS = 1;
    public static final int DIRT = 2;
    public static final int STONE = 3;

    public static Material getMaterial(int blockId, AssetManager assetManager) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        switch(blockId) {
            case GRASS: mat.setColor("Color", ColorRGBA.Green); break;
            case DIRT:  mat.setColor("Color", ColorRGBA.Brown); break;
            case STONE: mat.setColor("Color", ColorRGBA.Gray); break;
            default:    mat.setColor("Color", ColorRGBA.White); break;
        }
        return mat;
    }
}

