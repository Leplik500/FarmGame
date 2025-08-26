package org.game;

import com.jme3.anim.AnimComposer;

public class PlayerAnimationController {
    // Constants moved here
    private static final float MIN_ANIMATION_DURATION = 0.5f;
    private static final float IDLE_ANIMATION_DURATION = 0.15f;
    private final AnimComposer animComposer;
    // Animation state
    private String targetAnimation = "animation.lael.idlemain";
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
            return isRunning ? "animation.lael.run" : "animation.lael.walk";
        } else {
            return "animation.lael.idlemain";
        }
    }

    public void forceUnlockIfMoving(boolean isWalking) {
        if (isWalking && animationLocked && targetAnimation.equals("animation.lael.idlemain") && animationLockTime < 0.1f) {
            animationLocked = false;
        }
    }

    private void setAndLockAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            try {
                animComposer.setCurrentAction(animationName);
                animationLocked = true;
                animationLockTime = animationName.equals("animation.lael.idlemain") ? IDLE_ANIMATION_DURATION : MIN_ANIMATION_DURATION;
            } catch (Exception e) {
                System.err.println("Error setting animation: " + e.getMessage());
            }
        }
    }
}
