package org.game;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class CollisionChecker {
    private final Spatial shopModel;
    private final Spatial houseModel;
    private final PlantGrowthState plantGrowth;

    // Конфигурируемая стадия растений для коллизии
    private static final int PLANT_COLLISION_MIN_STAGE = 2;

    public CollisionChecker(Spatial shopModel, Spatial houseModel, PlantGrowthState plantGrowth) {
        this.shopModel = shopModel;
        this.houseModel = houseModel;
        this.plantGrowth = plantGrowth;
    }

    public boolean isPositionBlocked(Vector3f position) {
        return isCollidingWithShop(position) ||
                isCollidingWithHouse(position) ||
                isCollidingWithPlants(position);
    }

    private boolean isCollidingWithShop(Vector3f position) {
        return isCollidingWithObject(position, shopModel, getObjectRadius(shopModel));
    }

    private boolean isCollidingWithHouse(Vector3f position) {
        return isCollidingWithObject(position, houseModel, getObjectRadius(houseModel));
    }

    private boolean isCollidingWithPlants(Vector3f position) {
        for (PlantGrowthState.Plant plant : plantGrowth.getAllPlants().values()) {
            if (plant.stageIndex >= PLANT_COLLISION_MIN_STAGE) {
                Vector3f plantPos = new Vector3f(plant.above.x(), position.y, plant.above.z());
                if (isCollidingWithObject(position, plantPos, GameConfig.PLANT_COLLISION_RADIUS)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCollidingWithObject(Vector3f playerPos, Spatial object, float radius) {
        Vector3f objectPos = object.getWorldTranslation();
        return isCollidingWithObject(playerPos, objectPos, radius);
    }

    private boolean isCollidingWithObject(Vector3f playerPos, Vector3f objectPos, float radius) {
        float distance = playerPos.distance(objectPos);
        return distance < radius;
    }

    private float getObjectRadius(Spatial object) {
        if (object == shopModel) {
            return GameConfig.SHOP_COLLISION_RADIUS;
        } else if (object == houseModel) {
            return GameConfig.HOUSE_COLLISION_RADIUS;
        }
        return 1.0f; // default
    }
}
