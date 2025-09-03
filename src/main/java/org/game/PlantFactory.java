package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class PlantFactory {
    private final AssetManager assetManager;
    private final Node plantsRoot;

    public PlantFactory(AssetManager assetManager, Node plantsRoot) {
        this.assetManager = assetManager;
        this.plantsRoot = plantsRoot;
    }

    public Spatial createPlant(PlantKind kind, Vector3i soilPos) {
        String modelPath = getFirstStageModel(kind);
        Spatial spatial = assetManager.loadModel(modelPath);

        float yTop = soilPos.y() + 0.5f;
        spatial.setLocalTranslation(new Vector3f(soilPos.x(), yTop, soilPos.z()));
        spatial.scale(3f);
        plantsRoot.attachChild(spatial);

        return spatial;
    }

    public Spatial createPlantAtStage(PlantKind kind, Vector3i soilPos, int stageIndex) {
        String modelPath = getModelPathForStage(kind, stageIndex);
        Spatial spatial = assetManager.loadModel(modelPath);

        float yTop = soilPos.y() + 0.5f;
        spatial.setLocalTranslation(new Vector3f(soilPos.x(), yTop, soilPos.z()));
        spatial.scale(3f);
        plantsRoot.attachChild(spatial);

        return spatial;
    }

    private String getFirstStageModel(PlantKind kind) {
        return switch (kind) {
            case PUMPKIN -> GameConfig.PUMPKIN_GROWTH_MODELS[0];
            case TOMATO -> GameConfig.TOMATO_GROWTH_MODELS[0];
        };
    }

    private String getModelPathForStage(PlantKind kind, int stageIndex) {
        if (stageIndex == 4) { 
            return switch (kind) {
                case PUMPKIN -> GameConfig.PUMPKIN_HARVESTED_MODEL;
                case TOMATO -> GameConfig.TOMATO_HARVESTED_MODEL;
            };
        } else {
            return switch (kind) {
                case PUMPKIN -> GameConfig.PUMPKIN_GROWTH_MODELS[stageIndex];
                case TOMATO -> GameConfig.TOMATO_GROWTH_MODELS[stageIndex];
            };
        }
    }
}
