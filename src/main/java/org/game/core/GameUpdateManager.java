package org.game.core;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import org.game.facades.GameUIFacade;
import org.game.facades.GameWorldFacade;
import org.game.facades.PlayerFacade;
import org.game.input.PlayerInputHandler;
import org.game.utils.GameConfig;

public class GameUpdateManager {
    private final PlayerFacade playerFacade;
    private final GameWorldFacade worldFacade;
    private final GameUIFacade uiFacade;
    private final PlayerInputHandler inputHandler;
    private final Node camTarget;

    public GameUpdateManager(PlayerFacade playerFacade, GameWorldFacade worldFacade,
                             GameUIFacade uiFacade, PlayerInputHandler inputHandler, Node camTarget) {
        this.playerFacade = playerFacade;
        this.worldFacade = worldFacade;
        this.uiFacade = uiFacade;
        this.inputHandler = inputHandler;
        this.camTarget = camTarget;
    }

    public void update(float tpf) {
        updatePlayerMovement(tpf);
        updateCamera(tpf);
        updateLighting();
        updateUI();
    }

    private void updatePlayerMovement(float tpf) {
        boolean hasManualInput = inputHandler.isWalking();
        boolean hasAutoMovement = playerFacade.getMovementController()
                .getAutoMovementController().isAutoMoving();
        boolean isUIOpen = uiFacade.getShopUI().isVisible();

        if (hasManualInput || hasAutoMovement) {
            playerFacade.getMovementController().handleMovement(
                    inputHandler.getPressedKeys(),
                    inputHandler.isRunning(),
                    tpf,
                    isUIOpen
            );
        }

        boolean isMoving = hasManualInput || hasAutoMovement;
        boolean isRunning = hasManualInput ? inputHandler.isRunning() : hasAutoMovement;
        playerFacade.getAnimationController().update(isMoving && !isUIOpen, isRunning);
    }

    private void updateCamera(float tpf) {
        Vector3f to = playerFacade.getPlayer().getWorldTranslation();
        Vector3f from = camTarget.getLocalTranslation();
        float camFollowPosSpeed = GameConfig.CAM_FOLLOW_SPEED;
        Vector3f posDelta = to.subtract(from).multLocal(Math.min(1f, camFollowPosSpeed * tpf));
        camTarget.move(posDelta);
    }

    private void updateLighting() {
        worldFacade.updateLighting();
    }

    private void updateUI() {
        uiFacade.updateFaceHighlight();
    }
}
