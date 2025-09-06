package org.game;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.math.Vector3f;
import java.util.*;

public class PlantGrowthState extends TimedStateManager {
    private final SimpleBlockWorld world;
    private final AssetManager assetManager;
    private final Node plantsRoot;
    private final DayNightCycle dayNightCycle;

    public static final class Plant {
        public final PlantKind kind;
        final Vector3i soil;   
        final Vector3i above;  
        public int stageIndex;        
        public float timeLeft;        
        public Spatial spatial;

        public Plant(PlantKind kind, Vector3i soil, Vector3i above, int stageIndex,
                     float timeLeft, Spatial spatial) {
            this.kind = kind; this.soil = soil; this.above = above;
            this.stageIndex = stageIndex; this.timeLeft = timeLeft; this.spatial = spatial;
        }
    }

    private final Map<Vector3i, Plant> plants = new HashMap<>();

    public PlantGrowthState(SimpleBlockWorld world, AssetManager assetManager, Node plantsRoot, DayNightCycle dayNightCycle) {
        this.world = world;
        this.assetManager = assetManager;
        this.plantsRoot = plantsRoot;
        this.dayNightCycle = dayNightCycle;
    }

    public boolean HasNotPlantAt(Vector3i above) { return !plants.containsKey(above); } 

    public void registerPlanted(PlantKind kind, Vector3i soil, Vector3i above, Spatial stage1Spatial) {
        float duration = GameConfig.GROWTH_STAGE_SECONDS[0];
        plants.put(above, new Plant(kind, soil, above, 0, duration, stage1Spatial));
    }

    @Override protected void initialize(Application app) {}
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
    

    private void replaceModel(Plant p) {
        String path = getModelPath(p);

        Spatial next = assetManager.loadModel(path);
        Vector3f pos = p.spatial.getLocalTranslation().clone();
        next.setLocalTranslation(pos);
        next.setLocalRotation(p.spatial.getLocalRotation());
        next.setLocalScale(p.spatial.getLocalScale());

        if (p.spatial.getParent() != null) p.spatial.removeFromParent();
        plantsRoot.attachChild(next);
        p.spatial = next;
    }

    private static String getModelPath(Plant p) {
        String path;
        if (p.stageIndex == 4) {
            path = switch (p.kind) {
                case PUMPKIN -> GameConfig.PUMPKIN_HARVESTED_MODEL;
                case TOMATO -> GameConfig.TOMATO_HARVESTED_MODEL;
            };
        } else {
            path = switch (p.kind) {
                case PUMPKIN -> GameConfig.PUMPKIN_GROWTH_MODELS[p.stageIndex];
                case TOMATO -> GameConfig.TOMATO_GROWTH_MODELS[p.stageIndex];
            };
        }
        return path;
    }


    public Plant getPlantAt(Vector3i above) {
        return plants.get(above);
    }

    public Node getPlantsRoot() {
        return plantsRoot;
    }

    public Map<Vector3i, Plant> getAllPlants() { return new HashMap<>(plants); }

    public void clearAll() {
        for (Plant plant : plants.values()) {
            if (plant.spatial != null && plant.spatial.getParent() != null) {
                plant.spatial.removeFromParent();
            }
        }
        plants.clear();
    }

    public void registerPlantWithState(PlantKind kind, Vector3i soil, Vector3i above,
                                       Spatial spatial, int stageIndex, float timeLeft) {
        plants.put(above, new Plant(kind, soil, above, stageIndex, timeLeft, spatial));
    }


    @Override
    protected boolean shouldUpdate() {
        return !plants.isEmpty() && !dayNightCycle.isNight();
    }

    @Override
    protected void updateTimedStates(float tpf) {
        for (Plant plant : plants.values()) {
            updatePlantGrowth(plant, tpf);
        }
    }

    @Override
    protected void performCleanup() {
        List<Vector3i> toRemove = new ArrayList<>();

        for (Plant plant : plants.values()) {
            int soilType = world.getBlock(plant.soil.x(), plant.soil.y(), plant.soil.z());
            if (soilType == BlockType.AIR || soilType == BlockType.GRASS) {
                if (plant.spatial != null && plant.spatial.getParent() != null) {
                    plant.spatial.removeFromParent();
                }
                toRemove.add(plant.above);
            }
        }

        for (Vector3i pos : toRemove) {
            plants.remove(pos);
        }
    }

    private void updatePlantGrowth(Plant plant, float tpf) {
        int soilType = world.getBlock(plant.soil.x(), plant.soil.y(), plant.soil.z());
        if (soilType != BlockType.PLOWED_WET) return;

        if (plant.stageIndex == 3) return;
        if (plant.stageIndex >= 5) return;

        plant.timeLeft -= tpf;
        if (plant.timeLeft <= 0f) {
            if (plant.stageIndex < 3) {
                plant.stageIndex++;
                replaceModel(plant);
                if (plant.stageIndex < GameConfig.GROWTH_STAGE_SECONDS.length) {
                    plant.timeLeft = GameConfig.GROWTH_STAGE_SECONDS[plant.stageIndex];
                }
            } else if (plant.stageIndex == 4) {
                plant.stageIndex = 3;
                replaceModel(plant);
            }
        }
    }

    @Override
    protected void onCleanup(Application app) {
        plants.clear();
    }
}
