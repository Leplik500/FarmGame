package org.game;

import com.jme3.anim.AnimComposer;

public class PlayerAnimationController {
    private static final float MIN_ANIMATION_DURATION = GameConfig.MIN_ANIMATION_DURATION;
    private static final float IDLE_ANIMATION_DURATION = GameConfig.IDLE_ANIMATION_DURATION;
    private final AnimComposer animComposer;
    
    private String targetAnimation = GameConfig.ANIM_IDLE;
    private boolean animationLocked = false;
    private float animationLockTime = 0f;

    public PlayerAnimationController(AnimComposer animComposer) {
        this.animComposer = animComposer;
        if (animComposer != null) {
            setAndLockAnimation(targetAnimation);
        }
    }

    public void update(float tpf, boolean isWalking, boolean isRunning) {
        updateLockTimer(tpf);
        updateAnimation(isWalking, isRunning);
    }

    private void updateLockTimer(float tpf) {
        if (animationLocked) {
            animationLockTime -= tpf;
            if (animationLockTime <= 0) {
                animationLocked = false;
            }
        }
    }

    private void updateAnimation(boolean isWalking, boolean isRunning) {
        String newTarget = determineTargetAnimation(isWalking, isRunning);

        if (!newTarget.equals(targetAnimation)) {
            targetAnimation = newTarget;
            if (!animationLocked) {
                setAndLockAnimation(targetAnimation);
            }
        }
    }

    private String determineTargetAnimation(boolean isWalking, boolean isRunning) {
        if (isWalking) {
            return isRunning ? GameConfig.ANIM_RUN : GameConfig.ANIM_WALK;
        } else {
            return GameConfig.ANIM_IDLE;
        }
    }

    public void forceUnlockIfMoving(boolean isWalking) {
        if (isWalking && animationLocked && targetAnimation.equals(GameConfig.ANIM_IDLE) && animationLockTime < GameConfig.ANIMATION_UNLOCK_THRESHOLD) {
            animationLocked = false;
        }
    }

    private void setAndLockAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            try {
                animComposer.setCurrentAction(animationName);
                animationLocked = true;
                animationLockTime = animationName.equals(GameConfig.ANIM_IDLE) ?
                        IDLE_ANIMATION_DURATION : MIN_ANIMATION_DURATION;
            } catch (Exception e) {
                System.err.println("Error setting animation: " + e.getMessage());
            }
        }
    }
}
