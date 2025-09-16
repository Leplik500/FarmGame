package org.game.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import org.game.utils.GameConfig;

public class DayNightCycle extends BaseAppState {
    private float currentTime = 30f;
    private final float cycleDuration;
    private boolean isDay = true;

    public DayNightCycle() {
        this.cycleDuration = GameConfig.DAY_DURATION_SECONDS + GameConfig.NIGHT_DURATION_SECONDS;
    }

    @Override
    protected void initialize(Application app) {}

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void update(float tpf) {
        currentTime += tpf;
        if (currentTime >= cycleDuration) {
            currentTime -= cycleDuration;
        }

        boolean wasDay = isDay;
        float transitionDuration = 30f;
        isDay = currentTime >= transitionDuration && currentTime < GameConfig.DAY_DURATION_SECONDS;

        if (wasDay != isDay) {
            System.out.println(isDay ? "Dawn breaks" : "Night falls");
        }
    }

    public boolean isDay() {
        return isDay;
    }

    public boolean isNight() {
        return !isDay;
    }

    public float getLightIntensity() {
        float transitionDuration = 30f;
        float dayDuration = GameConfig.DAY_DURATION_SECONDS;
        float cycleDuration = dayDuration + GameConfig.NIGHT_DURATION_SECONDS;

        float t = currentTime % cycleDuration;

        if (t < transitionDuration) {
            float progress = t / transitionDuration;
            return lerp(GameConfig.NIGHT_LIGHT_INTENSITY, GameConfig.DAY_LIGHT_INTENSITY, progress);
        }
        else if (t < dayDuration) {
            return GameConfig.DAY_LIGHT_INTENSITY;
        }
        else if (t < dayDuration + transitionDuration) {
            float progress = (t - dayDuration) / transitionDuration;
            return lerp(GameConfig.DAY_LIGHT_INTENSITY, GameConfig.NIGHT_LIGHT_INTENSITY, progress);
        }
        else {
            return GameConfig.NIGHT_LIGHT_INTENSITY;
        }
    }
    
    public void skipToDay() {
        if (isNight()) {
            currentTime = 30f;
            isDay = true;
            System.out.println("Skipped to dawn - a new day begins!");
        }
    }

    public float getCurrentTime() { return currentTime; }
    public void setCurrentTime(float time) { this.currentTime = time; }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
