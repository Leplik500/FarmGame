package org.game.facades;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import org.game.states.DayNightCycle;
import org.game.states.FarmlandMoistureState;
import org.game.states.PlantGrowthState;
import org.game.utils.GameConfig;
import org.game.world.BlockWorld;
import org.game.world.PlantFactory;

public class GameWorldFacade {
    private final BlockWorld blockWorld;
    private final PlantGrowthState plantGrowthState;
    private final FarmlandMoistureState moistureState;
    private final PlantFactory plantFactory;
    private final DayNightCycle dayNightCycle;
    private final DirectionalLight sunLight;
    private final AmbientLight ambientLight;
    private final Spatial shopModel;
    private final Spatial houseModel;

    public GameWorldFacade(AssetManager assetManager, Node rootNode) {
        sunLight = new DirectionalLight();
        sunLight.setDirection(GameConfig.LIGHT_DIRECTION.normalizeLocal());
        sunLight.setColor(ColorRGBA.White.mult(GameConfig.DAY_LIGHT_INTENSITY));
        rootNode.addLight(sunLight);

        ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.15f));
        rootNode.addLight(ambientLight);

        blockWorld = new BlockWorld(rootNode, assetManager);
        blockWorld.generateFlatWorld(GameConfig.WORLD_SIZE_X, GameConfig.WORLD_SIZE_Z);

        moistureState = new FarmlandMoistureState(blockWorld);
        dayNightCycle = new DayNightCycle();

        Node plantsRoot = new Node("PlantsRoot");
        rootNode.attachChild(plantsRoot);
        plantFactory = new PlantFactory(assetManager, plantsRoot);
        plantGrowthState = new PlantGrowthState(blockWorld, assetManager, plantsRoot, dayNightCycle);

        shopModel = createShop(assetManager, rootNode);
        houseModel = createHouse(assetManager, rootNode);

        createWorldBoundaries(assetManager, rootNode);
        createSky(assetManager, rootNode);
    }

    private Spatial createShop(AssetManager assetManager, Node rootNode) {
        Spatial shop = assetManager.loadModel(GameConfig.SHOP_MODEL);
        shop.setLocalTranslation(15, -1, 15);
        shop.setLocalScale(1f);
        rootNode.attachChild(shop);
        return shop;
    }

    private Spatial createHouse(AssetManager assetManager, Node rootNode) {
        Spatial house = assetManager.loadModel(GameConfig.HOUSE_MODEL);
        house.setLocalTranslation(10, -1, 10);
        house.setLocalScale(0.3f);
        rootNode.attachChild(house);
        return house;
    }

    private void createSky(AssetManager assetManager, Node rootNode) {
        Texture px = assetManager.loadTexture(GameConfig.SKYBOX_PX);
        Texture nx = assetManager.loadTexture(GameConfig.SKYBOX_NX);
        Texture py = assetManager.loadTexture(GameConfig.SKYBOX_PY);
        Texture ny = assetManager.loadTexture(GameConfig.SKYBOX_NY);
        Texture pz = assetManager.loadTexture(GameConfig.SKYBOX_PZ);
        Texture nz = assetManager.loadTexture(GameConfig.SKYBOX_NZ);
        Spatial sky = SkyFactory.createSky(assetManager, px, nx, py, ny, pz, nz);
        rootNode.attachChild(sky);
    }

    private void createWorldBoundaries(AssetManager assetManager, Node rootNode) {
        Material boundaryMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boundaryMat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 0.3f));
        boundaryMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        float wallHeight = 25f;
        float wallThickness = 0.5f;
        float minX = GameConfig.WORLD_BOUNDARY_MIN_X;
        float maxX = GameConfig.WORLD_BOUNDARY_MAX_X;
        float minZ = GameConfig.WORLD_BOUNDARY_MIN_Z;
        float maxZ = GameConfig.WORLD_BOUNDARY_MAX_Z;
        float worldWidth = maxX - minX + 1 + wallThickness * 2;
        float worldDepth = maxZ - minZ + 1 + wallThickness * 2;

        // Create walls
        createWall(rootNode, boundaryMat, worldWidth/2f, wallHeight/2f, wallThickness/2f,
                (minX + maxX)/2f, wallHeight/2f, maxZ + 0.5f + wallThickness/2f, "NorthBoundary");
        createWall(rootNode, boundaryMat, worldWidth/2f, wallHeight/2f, wallThickness/2f,
                (minX + maxX)/2f, wallHeight/2f, minZ - 0.5f - wallThickness/2f, "SouthBoundary");
        createWall(rootNode, boundaryMat, wallThickness/2f, wallHeight/2f, (worldDepth - wallThickness * 2)/2f,
                maxX + 0.5f + wallThickness/2f, wallHeight/2f, (minZ + maxZ)/2f, "EastBoundary");
        createWall(rootNode, boundaryMat, wallThickness/2f, wallHeight/2f, (worldDepth - wallThickness * 2)/2f,
                minX - 0.5f - wallThickness/2f, wallHeight/2f, (minZ + maxZ)/2f, "WestBoundary");
    }

    private void createWall(Node rootNode, Material material, float sizeX, float sizeY, float sizeZ,
                            float posX, float posY, float posZ, String name) {
        Box wall = new Box(sizeX, sizeY, sizeZ);
        Geometry wallGeo = new Geometry(name, wall);
        wallGeo.setLocalTranslation(posX, posY, posZ);
        wallGeo.setMaterial(material);
        wallGeo.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(wallGeo);
    }

    public void updateLighting() {
        float intensity = dayNightCycle.getLightIntensity();
        sunLight.setColor(ColorRGBA.White.mult(intensity));
        float ambientIntensity = intensity * 0.1f + 0.05f;
        ambientLight.setColor(ColorRGBA.White.mult(ambientIntensity));
    }

    public BlockWorld getBlockWorld() { return blockWorld; }
    public PlantGrowthState getPlantGrowthState() { return plantGrowthState; }
    public FarmlandMoistureState getMoistureState() { return moistureState; }
    public PlantFactory getPlantFactory() { return plantFactory; }
    public DayNightCycle getDayNightCycle() { return dayNightCycle; }
    public Spatial getShopModel() { return shopModel; }
    public Spatial getHouseModel() { return houseModel; }
}
