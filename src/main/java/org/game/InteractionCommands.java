package org.game;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import org.game.events.SimpleEventBus;

public class InteractionCommands {

    public static class HoeCommand implements InteractionCommand {
        private final Vector3i targetBlock;
        private final SimpleBlockWorld world;

        public HoeCommand(Vector3i targetBlock, SimpleBlockWorld world) {
            this.targetBlock = targetBlock;
            this.world = world;
        }

        @Override
        public boolean execute() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            if (type == BlockType.GRASS) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_DRY);
                System.out.println("Tilled soil at: " + targetBlock);
                return true;
            }
            return false;
        }

        @Override
        public Vector3f getTargetPosition() {
            return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, targetBlock);
        }

        @Override
        public String getDescription() {
            return "Hoe at " + targetBlock;
        }

        @Override
        public boolean isValidTarget() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            return type == BlockType.GRASS;
        }
    }

    public static class WaterCommand implements InteractionCommand {
        private final Vector3i targetBlock;
        private final SimpleBlockWorld world;
        private final FarmlandMoistureState moisture;

        public WaterCommand(Vector3i targetBlock, SimpleBlockWorld world, FarmlandMoistureState moisture) {
            this.targetBlock = targetBlock;
            this.world = world;
            this.moisture = moisture;
        }

        @Override
        public boolean execute() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            if (type == BlockType.PLOWED_DRY) {
                world.setBlock(targetBlock.x(), targetBlock.y(), targetBlock.z(), BlockType.PLOWED_WET);
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Watered soil at: " + targetBlock);
                return true;
            } else if (type == BlockType.PLOWED_WET) {
                moisture.markWet(targetBlock.x(), targetBlock.y(), targetBlock.z(), null);
                System.out.println("Refreshed moisture at: " + targetBlock);
                return true;
            }
            return false;
        }

        @Override
        public Vector3f getTargetPosition() {
            return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, targetBlock);
        }

        @Override
        public String getDescription() {
            return "Water at " + targetBlock;
        }

        @Override
        public boolean isValidTarget() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            return type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET;
        }
    }

    public static class PlantSeedCommand implements InteractionCommand {
        private final PlantKind plantKind;
        private final Vector3i targetBlock;
        private final SimpleBlockWorld world;
        private final PlantGrowthState growth;
        private final PlantFactory plantFactory;
        private final Hotbar hotbar;

        public PlantSeedCommand(PlantKind plantKind, Vector3i targetBlock, SimpleBlockWorld world,
                                PlantGrowthState growth, PlantFactory plantFactory, Hotbar hotbar) {
            this.plantKind = plantKind;
            this.targetBlock = targetBlock;
            this.world = world;
            this.growth = growth;
            this.plantFactory = plantFactory;
            this.hotbar = hotbar;
        }

        @Override
        public boolean execute() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            if (type == BlockType.PLOWED_DRY || type == BlockType.PLOWED_WET) {
                Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
                if (world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                        && !growth.hasPlantAt(aboveBlock)) {
                    Spatial s = plantFactory.createPlant(plantKind, targetBlock);
                    growth.registerPlanted(plantKind, targetBlock, aboveBlock, s);

                    int selectedSlot = hotbar.getSelectedSlot();
                    HotbarItem currentItem = hotbar.getSlotItem(selectedSlot);
                    if (currentItem != null && currentItem.count() >= 1) {
                        int newCount = currentItem.count() - 1;
                        if (newCount <= 0) {
                            SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, null);
                        } else {
                            HotbarItem updatedItem = currentItem.withCount(newCount);
                            SimpleEventBus.INSTANCE.publishInventoryChanged(selectedSlot, updatedItem);
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public Vector3f getTargetPosition() {
            return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, targetBlock);
        }

        @Override
        public String getDescription() {
            return "Plant " + plantKind + " at " + targetBlock;
        }
        
        @Override
        public boolean isValidTarget() {
            int type = world.getBlock(targetBlock.x(), targetBlock.y(), targetBlock.z());
            if (type != BlockType.PLOWED_DRY && type != BlockType.PLOWED_WET) {
                return false;
            }
            Vector3i aboveBlock = new Vector3i(targetBlock.x(), targetBlock.y() + 1, targetBlock.z());
            return world.getBlock(aboveBlock.x(), aboveBlock.y(), aboveBlock.z()) == BlockType.AIR
                    && !growth.hasPlantAt(aboveBlock);
        }
    }

    public static class HarvestCommand implements InteractionCommand {
        private final Vector3i targetBlock;
        private final PlantGrowthState growth;
        private final AssetManager assetManager;
        private final Hotbar hotbar;

        public HarvestCommand(Vector3i targetBlock, PlantGrowthState growth, AssetManager assetManager, Hotbar hotbar) {
            this.targetBlock = targetBlock;
            this.growth = growth;
            this.assetManager = assetManager;
            this.hotbar = hotbar;
        }

        @Override
        public boolean execute() {
            PlantGrowthState.Plant plant = growth.getPlantAt(targetBlock);
            if (plant == null || plant.stageIndex != 3) return false;

            if (plant.kind == PlantKind.PUMPKIN) {
                addToInventory("pumpkin", 1);
                System.out.println("Harvested 1 pumpkin!");
            } else if (plant.kind == PlantKind.TOMATO) {
                int count = 1 + (int)(Math.random() * 4);
                addToInventory("tomato", count);
                System.out.println("Harvested " + count + " tomatoes!");
            }

            plant.stageIndex = 4;
            plant.timeLeft = GameConfig.FRUIT_REGROW_SECONDS;

            String harvestedModelPath = switch (plant.kind) {
                case PUMPKIN -> GameConfig.PUMPKIN_HARVESTED_MODEL;
                case TOMATO -> GameConfig.TOMATO_HARVESTED_MODEL;
            };

            Spatial harvestedModel = assetManager.loadModel(harvestedModelPath);
            harvestedModel.setLocalTranslation(plant.spatial.getLocalTranslation());
            harvestedModel.setLocalRotation(plant.spatial.getLocalRotation());
            harvestedModel.setLocalScale(plant.spatial.getLocalScale());

            if (plant.spatial.getParent() != null) {
                plant.spatial.removeFromParent();
            }
            growth.getPlantsRoot().attachChild(harvestedModel);
            plant.spatial = harvestedModel;

            return true;
        }

        private void addToInventory(String itemId, int count) {
            for (int i = 0; i < 9; i++) {
                HotbarItem existing = hotbar.getSlotItem(i);
                if (existing != null && existing.id().equals(itemId)) {
                    HotbarItem updated = existing.withCount(existing.count() + count);
                    SimpleEventBus.INSTANCE.publishInventoryChanged(i, updated);
                    return;
                }
            }

            for (int i = 0; i < 9; i++) {
                if (hotbar.getSlotItem(i) == null) {
                    String iconPath = switch (itemId) {
                        case "pumpkin" -> GameConfig.PUMPKIN_ITEM;
                        case "tomato" -> GameConfig.TOMATO_ITEM;
                        default -> GameConfig.DEFAULT_ITEM;
                    };
                    HotbarItem newItem = new HotbarItem(itemId, iconPath, count);
                    SimpleEventBus.INSTANCE.publishInventoryChanged(i, newItem);
                    return;
                }
            }

            System.out.println("Inventory full! Couldn't pick up " + count + " " + itemId);
        }

        @Override
        public Vector3f getTargetPosition() {
            return new Vector3f(targetBlock.x(), targetBlock.y(), targetBlock.z());
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, targetBlock);
        }

        @Override
        public String getDescription() {
            return "Harvest at " + targetBlock;
        }

        @Override
        public boolean isValidTarget() {
            PlantGrowthState.Plant plant = growth.getPlantAt(targetBlock);
            return plant != null && plant.stageIndex == 3;
        }
    }

    public static class ShopCommand implements InteractionCommand {
        private final Spatial shopModel;
        private final ShopUI shopUI;

        public ShopCommand(Spatial shopModel, ShopUI shopUI) {
            this.shopModel = shopModel;
            this.shopUI = shopUI;
        }

        @Override
        public boolean execute() {
            shopUI.setVisible(true);
            return true;
        }

        @Override
        public Vector3f getTargetPosition() {
            return shopModel.getWorldTranslation();
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, shopModel.getWorldTranslation());
        }

        @Override
        public String getDescription() {
            return "Open shop";
        }

        @Override
        public boolean isValidTarget() {
            return true;
        }
    }

    public static class HouseCommand implements InteractionCommand {
        private final Spatial houseModel;
        private final DayNightCycle dayNightCycle;

        public HouseCommand(Spatial houseModel, DayNightCycle dayNightCycle) {
            this.houseModel = houseModel;
            this.dayNightCycle = dayNightCycle;
        }

        @Override
        public boolean execute() {
            if (dayNightCycle.isNight()) {
                dayNightCycle.skipToDay();
                System.out.println("You slept through the night. Good morning!");
                return true;
            } else {
                System.out.println("You can only sleep at night.");
                return false;
            }
        }

        @Override
        public Vector3f getTargetPosition() {
            return houseModel.getWorldTranslation();
        }

        @Override
        public boolean canExecuteAtCurrentPosition(Vector3f playerPos) {
            return ActionRange.isWithinRange(playerPos, houseModel.getWorldTranslation());
        }

        @Override
        public boolean isValidTarget() {
            return dayNightCycle.isNight();
        }

        @Override
        public String getDescription() {
            return "Sleep in house";
        }
    }

}
