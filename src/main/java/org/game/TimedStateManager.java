package org.game;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

public abstract class TimedStateManager extends BaseAppState {

    @Override
    protected void initialize(Application app) {
        onInitialize();
    }

    @Override
    protected void cleanup(Application app) {
        onCleanup(app);
    }

    @Override
    protected void onEnable() {
        onStateEnable();
    }

    @Override
    protected void onDisable() {
        onStateDisable();
    }

    @Override
    public final void update(float tpf) {
        if (!shouldUpdate()) return;

        performCleanup();
        updateTimedStates(tpf);
    }

    protected abstract boolean shouldUpdate();
    protected abstract void updateTimedStates(float tpf);
    protected abstract void performCleanup();

    protected void onInitialize() {}
    protected void onCleanup(Application app) {}
    protected void onStateEnable() {}
    protected void onStateDisable() {}
}
