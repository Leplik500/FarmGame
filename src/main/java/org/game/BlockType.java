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
    public static final int DIRT = 2;
    public static final int STONE = 3;

    // Кэш материалов чтобы не создавать их каждый раз
    private static final Map<Integer, Material> materialCache = new HashMap<>();

    public static Material getMaterial(int blockId, AssetManager assetManager) {
        // Проверяем кэш
        if (materialCache.containsKey(blockId)) {
            return materialCache.get(blockId);
        }

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        switch (blockId) {
            case GRASS:
                Texture grassTex = assetManager.loadTexture("Textures/grass7" + ".jpg");
                grassTex.setWrap(Texture.WrapMode.Repeat); // для тайлинга
//                mat.setColor("Color", new ColorRGBA(0.8f, 1.0f, 0.8f, 1.0f));
                mat.setTexture("ColorMap", grassTex);
                break;
//            case DIRT:
//                Texture dirtTex = assetManager.loadTexture("Textures/dirt.jpg");
//                dirtTex.setWrap(Texture.WrapMode.Repeat);
//                mat.setTexture("ColorMap", dirtTex);
//                break;
//            case STONE:
//                Texture stoneTex = assetManager.loadTexture("Textures/stone.jpg");
//                stoneTex.setWrap(Texture.WrapMode.Repeat);
//                mat.setTexture("ColorMap", stoneTex);
//                break;
            default:
                mat.setColor("Color", ColorRGBA.White);
                break;
        }

        // Сохраняем в кэш
        materialCache.put(blockId, mat);
        return mat;
    }
}
