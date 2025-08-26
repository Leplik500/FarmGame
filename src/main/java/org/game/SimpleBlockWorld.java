package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import java.util.HashMap;
import java.util.Map;

public class SimpleBlockWorld {
    private final Map<Vector3i, Integer> blocks = new HashMap<>();
    private final Map<Vector3i, Geometry> blockGeometries = new HashMap<>();
    private final Node worldRoot;
    private final AssetManager assetManager;
    private final Box blockMesh = new Box(0.5f, 0.5f, 0.5f);

    public SimpleBlockWorld(Node worldRoot, AssetManager assetManager) {
        this.worldRoot = worldRoot;
        this.assetManager = assetManager;
    }

    public void setBlock(int x, int y, int z, int blockType) {
        Vector3i pos = new Vector3i(x, y, z);

        // Удаляем старый блок если есть
        removeBlockGeometry(pos);

        if (blockType != BlockType.AIR) {
            blocks.put(pos, blockType);
            createBlockGeometry(pos, blockType);
        } else {
            blocks.remove(pos);
        }
    }

    public int getBlock(int x, int y, int z) {
        return blocks.getOrDefault(new Vector3i(x, y, z), BlockType.AIR);
    }

    private void createBlockGeometry(Vector3i pos, int blockType) {
        Geometry blockGeo = new Geometry("Block_" + pos, blockMesh);
        blockGeo.setMaterial(BlockType.getMaterial(blockType, assetManager));
        blockGeo.setLocalTranslation(pos.x(), pos.y(), pos.z());

        worldRoot.attachChild(blockGeo);
        blockGeometries.put(pos, blockGeo);
    }

    private void removeBlockGeometry(Vector3i pos) {
        Geometry oldGeo = blockGeometries.remove(pos);
        if (oldGeo != null) {
            oldGeo.removeFromParent();
        }
    }

    public void generateFlatWorld(int sizeX, int sizeZ) {
        for (int x = -sizeX / 2; x < sizeX / 2; x++) {
            for (int z = -sizeZ / 2; z < sizeZ / 2; z++) {
                setBlock(x, -1, z, BlockType.GRASS);
            }
        }
    }
}

