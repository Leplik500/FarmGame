package org.game.facades;

import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.game.player.PlayerAnimationController;
import org.game.player.PlayerMovementController;
import org.game.utils.GameConfig;
import com.jme3.renderer.Camera;

public class PlayerFacade {
    private final Spatial player;
    private final PlayerMovementController movementController;
    private final PlayerAnimationController animationController;
    private AnimComposer animComposer;

    public PlayerFacade(AssetManager assetManager, Node rootNode, Camera camera) {
        this.player = initializePlayer(assetManager, rootNode);
        this.movementController = new PlayerMovementController(player, camera);
        this.animationController = new PlayerAnimationController(animComposer);
    }

    private Spatial initializePlayer(AssetManager assetManager, Node rootNode) {
        Spatial playerSpatial = assetManager.loadModel(GameConfig.MODEL_PLAYER);
        playerSpatial.depthFirstTraversal(spatial -> {
            if (spatial.getControl(AnimComposer.class) != null) {
                animComposer = spatial.getControl(AnimComposer.class);
            }
        });
        playerSpatial.setLocalTranslation(0f, -0.5f, 0f);

        if (animComposer != null) {
            System.out.println("Available animations: " + animComposer.getAnimClipsNames());
        }

        rootNode.attachChild(playerSpatial);
        return playerSpatial;
    }

    public Spatial getPlayer() { return player; }
    public PlayerMovementController getMovementController() { return movementController; }
    public PlayerAnimationController getAnimationController() { return animationController; }
}
