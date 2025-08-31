package org.game;

import com.jme3.anim.AnimComposer;

public class PlayerAnimationController {
    private final AnimComposer animComposer;
    private String currentAnimation = GameConfig.ANIM_IDLE;

    public PlayerAnimationController(AnimComposer animComposer) {
        this.animComposer = animComposer;
        if (animComposer != null) {
            setAnimation(GameConfig.ANIM_IDLE);
        }
    }

    public void update(float tpf, boolean isWalking, boolean isRunning) {
        String requestedAnimation;

        if (!isWalking) {
            requestedAnimation = GameConfig.ANIM_IDLE;
        } else if (isRunning) {
            requestedAnimation = GameConfig.ANIM_RUN;
        } else {
            requestedAnimation = GameConfig.ANIM_WALK;
        }

        if (!requestedAnimation.equals(currentAnimation)) {
            setAnimation(requestedAnimation);
        }
    }

    private void setAnimation(String animationName) {
        if (animComposer != null && animComposer.getAnimClipsNames().contains(animationName)) {
            animComposer.setCurrentAction(animationName);
            currentAnimation = animationName;
            System.out.println("Animation changed to: " + animationName);
        }
    }
}
