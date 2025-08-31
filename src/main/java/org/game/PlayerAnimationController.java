package org.game;

import com.jme3.anim.AnimComposer;

public class PlayerAnimationController {
    private static final float MIN_ANIMATION_DURATION = GameConfig.MIN_ANIMATION_DURATION;
    private static final float IDLE_ANIMATION_DURATION = GameConfig.IDLE_ANIMATION_DURATION;

    private final AnimComposer animComposer;

    private String current = GameConfig.ANIM_IDLE;

    private float lockLeft = 0f;

    public PlayerAnimationController(AnimComposer animComposer) {
        this.animComposer = animComposer;
        if (animComposer != null) {
            play(GameConfig.ANIM_IDLE);
        }
    }

    public void update(float tpf, boolean isWalking, boolean isRunning) {
        if (lockLeft > 0f) lockLeft -= tpf;

        String requested = isWalking ? (isRunning ? GameConfig.ANIM_RUN : GameConfig.ANIM_WALK)
                : GameConfig.ANIM_IDLE;

        if (lockLeft <= 0f && !requested.equals(current)) {
            play(requested);
            lockLeft = requested.equals(GameConfig.ANIM_IDLE) ? IDLE_ANIMATION_DURATION : MIN_ANIMATION_DURATION;
        }
    }

    public void forceUnlockIfMoving(boolean isWalking) {
        if (isWalking && lockLeft < GameConfig.ANIMATION_UNLOCK_THRESHOLD && GameConfig.ANIM_IDLE.equals(current)) {
            lockLeft = 0f;
        }
    }

    private void play(String name) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(name)) {
            animComposer.setCurrentAction(name); 
            current = name;                      
        }
    }
}
