package org.game.states;

import com.jme3.app.Application;
import org.game.world.BlockType;
import org.game.world.BlockWorld;
import org.game.world.Vector3i;

import java.util.*;

public class FarmlandMoistureState extends TimedStateManager {
    private final BlockWorld world;
    private final Map<Vector3i, Float> timers = new HashMap<>();

    public FarmlandMoistureState(BlockWorld world) {
        this.world = world;
    }

    public void markWet(Vector3i position, Float seconds) {
        if (world.getBlock(position.x(), position.y(), position.z()) == BlockType.PLOWED_WET) {
            float defaultWetSeconds = 30f;
            timers.put(position, seconds != null ? seconds : defaultWetSeconds);
        }
    }

    @Override protected void initialize(Application app) {}
    @Override protected void cleanup(Application app) { timers.clear(); }
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}

    @Override
    protected boolean shouldUpdate() {
        return !timers.isEmpty();
    }

    @Override
    protected void updateTimedStates(float tpf) {
        Iterator<Map.Entry<Vector3i, Float>> it = timers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vector3i, Float> entry = it.next();
            Vector3i pos = entry.getKey();
            float timeLeft = entry.getValue() - tpf;

            if (timeLeft <= 0f) {
                world.setBlock(pos.x(), pos.y(), pos.z(), BlockType.PLOWED_DRY);
                it.remove();
            } else {
                entry.setValue(timeLeft);
            }
        }
    }

    @Override
    protected void performCleanup() {
        Iterator<Map.Entry<Vector3i, Float>> it = timers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vector3i, Float> entry = it.next();
            Vector3i pos = entry.getKey();
            int blockType = world.getBlock(pos.x(), pos.y(), pos.z());

            if (blockType != BlockType.PLOWED_WET) {
                it.remove();
            }
        }
    }

    @Override
    protected void onCleanup(Application app) {
        timers.clear();
    }
    
    

}
