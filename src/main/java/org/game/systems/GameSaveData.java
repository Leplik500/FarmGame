package org.game.systems;

import org.game.world.Vector3i;

import java.util.List;
import java.util.Map;

public class GameSaveData {
    public float playerX, playerY, playerZ;
    public int money;
    public List<HotbarItemData> inventory;

    public Map<String, Integer> blocks; // "x,y,z" -> blockType
    public List<PlantData> plants;

    public float currentTime;
    public boolean isDay;

    public static class HotbarItemData {
        public String id;
        public String iconPath;
        public int count;
        public int slot;

        public HotbarItemData() {} // для JSON

        public HotbarItemData(String id, String iconPath, int count, int slot) {
            this.id = id;
            this.iconPath = iconPath;
            this.count = count;
            this.slot = slot;
        }
    }

    public static class PlantData {
        public String kind;
        public int soilX, soilY, soilZ;
        public int aboveX, aboveY, aboveZ;
        public int stageIndex;
        public float timeLeft;

        public PlantData() {} // для JSON

        public PlantData(String kind, Vector3i soil, Vector3i above, int stageIndex, float timeLeft) {
            this.kind = kind;
            this.soilX = soil.x(); this.soilY = soil.y(); this.soilZ = soil.z();
            this.aboveX = above.x(); this.aboveY = above.y(); this.aboveZ = above.z();
            this.stageIndex = stageIndex;
            this.timeLeft = timeLeft;
        }
    }
}
