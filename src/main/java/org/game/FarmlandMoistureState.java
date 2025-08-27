package org.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.*;

public class FarmlandMoistureState extends BaseAppState {
    private final SimpleBlockWorld world;
    private final Map<Vector3i, Float> timers = new HashMap<>();
    private float defaultWetSeconds = 30f; 

    public FarmlandMoistureState(SimpleBlockWorld world) {
        this.world = world;
    }

    public void markWet(int x, int y, int z, Float seconds) {
        if (world.getBlock(x, y, z) == BlockType.PLOWED_WET) {
            timers.put(new Vector3i(x, y, z), seconds != null ? seconds : defaultWetSeconds);
        }
    }

    public void setDefaultWetSeconds(float seconds) { this.defaultWetSeconds = seconds; }

    @Override protected void initialize(Application app) {}
    @Override protected void cleanup(Application app) { timers.clear(); }
    @Override protected void onEnable() {}
    @Override protected void onDisable() {}

    @Override
    public void update(float tpf) {
        if (timers.isEmpty()) return;
        List<Vector3i> toDry = new ArrayList<>();
        Iterator<Map.Entry<Vector3i, Float>> it = timers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vector3i, Float> e = it.next();
            Vector3i pos = e.getKey();
            // Если клетка успела измениться чем-то ещё — убрать таймер
            int id = world.getBlock(pos.x(), pos.y(), pos.z());
            if (id != BlockType.PLOWED_WET) {
                it.remove();
                continue;
            }
            float left = e.getValue() - tpf; // tpf — время с прошлого кадра, в секундах
            if (left <= 0f) {
                toDry.add(pos);
                it.remove();
            } else {
                e.setValue(left);
            }
        }
        for (Vector3i p : toDry) {
            world.setBlock(p.x(), p.y(), p.z(), BlockType.PLOWED_DRY);
        }
    }
}
