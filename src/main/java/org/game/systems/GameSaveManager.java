package org.game.systems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.Main;
import org.game.states.DayNightCycle;
import org.game.states.PlantGrowthState;
import org.game.ui.Hotbar;
import org.game.ui.HotbarItem;
import org.game.world.BlockWorld;
import org.game.world.PlantFactory;
import org.game.world.PlantKind;
import org.game.world.Vector3i;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GameSaveManager {
    private static final String SAVE_FILE = "gamesave.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveGame(Main gameInstance) {
        try {
            GameSaveData saveData = collectGameData(gameInstance);
            String json = gson.toJson(saveData);
            Files.write(Paths.get(SAVE_FILE), json.getBytes());
            System.out.println("Game saved successfully!");
        } catch (Exception e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    public void loadGame(Main gameInstance) {
        try {
            Path saveFile = Paths.get(SAVE_FILE);
            if (!Files.exists(saveFile)) {
                System.out.println("No save file found.");
                return;
            }

            String json = Files.readString(saveFile);
            GameSaveData saveData = gson.fromJson(json, GameSaveData.class);

            System.out.println("Save data loaded - blocks: " +
                    (saveData.blocks != null ? saveData.blocks.size() : "null") +
                    ", plants: " +
                    (saveData.plants != null ? saveData.plants.size() : "null"));

            applyGameData(gameInstance, saveData);
            System.out.println("Game loaded successfully!");
        } catch (Exception e) {
            System.err.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private GameSaveData collectGameData(Main gameInstance) {
        GameSaveData data = new GameSaveData();

        Vector3f playerPos = gameInstance.getPlayer().getWorldTranslation();
        data.playerX = playerPos.x;
        data.playerY = playerPos.y;
        data.playerZ = playerPos.z;
        data.money = gameInstance.getMoneyDisplay().getMoney();

        data.inventory = new ArrayList<>();
        Hotbar hotbar = gameInstance.getHotbar();
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            HotbarItem item = hotbar.getSlotItem(i);
            if (item != null) {
                data.inventory.add(new GameSaveData.HotbarItemData(
                        item.id(), item.iconPath(), item.count(), i));
            }
        }

        data.blocks = gameInstance.getBlockWorld().getAllBlocks();
        System.out.println("Saving " + data.blocks.size() + " blocks");

        data.plants = new ArrayList<>();
        for (Map.Entry<Vector3i, PlantGrowthState.Plant> entry :
                gameInstance.getPlantGrowthState().getAllPlants().entrySet()) {
            PlantGrowthState.Plant plant = entry.getValue();
            data.plants.add(new GameSaveData.PlantData(
                    plant.kind.name(), plant.soil, plant.above, plant.stageIndex, plant.timeLeft));
        }

        DayNightCycle dayNight = gameInstance.getDayNightCycle();
        data.currentTime = dayNight.getCurrentTime();
        data.isDay = dayNight.isDay();

        return data;
    }

    private void applyGameData(Main gameInstance, GameSaveData data) {
        gameInstance.getPlayer().setLocalTranslation(data.playerX, data.playerY, data.playerZ);

        gameInstance.getMoneyDisplay().setMoney(data.money);

        Hotbar hotbar = gameInstance.getHotbar();
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            hotbar.clearSlot(i);
        }

        for (GameSaveData.HotbarItemData itemData : data.inventory) {
            HotbarItem item = new HotbarItem(itemData.id, itemData.iconPath, itemData.count);
            hotbar.addItemToSlot(itemData.slot, item);
        }

        BlockWorld world = gameInstance.getBlockWorld();
        world.clearAll();

        System.out.println("Loading " + data.blocks.size() + " blocks");
        for (Map.Entry<String, Integer> entry : data.blocks.entrySet()) {
            String[] coords = entry.getKey().split(",");
            if (coords.length == 3) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    world.setBlock(x, y, z, entry.getValue());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid coordinate format: " + entry.getKey());
                }
            }
        }

        PlantGrowthState plantState = gameInstance.getPlantGrowthState();
        PlantFactory plantFactory = gameInstance.getPlantFactory();
        plantState.clearAll();

        for (GameSaveData.PlantData plantData : data.plants) {
            Vector3i soil = new Vector3i(plantData.soilX, plantData.soilY, plantData.soilZ);
            Vector3i above = new Vector3i(plantData.aboveX, plantData.aboveY, plantData.aboveZ);
            PlantKind kind = PlantKind.valueOf(plantData.kind);

            Spatial spatial = plantFactory.createPlantAtStage(kind, soil, plantData.stageIndex);

            plantState.registerPlantWithState(kind, soil, above, spatial,
                    plantData.stageIndex, plantData.timeLeft);
        }

        DayNightCycle dayNight = gameInstance.getDayNightCycle();
        dayNight.setCurrentTime(data.currentTime);

        System.out.println("Game loaded: " + data.blocks.size() + " blocks, " +
                data.plants.size() + " plants");
    }}
