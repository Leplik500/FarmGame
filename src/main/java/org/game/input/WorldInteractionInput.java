package org.game.input;

import com.jme3.input.controls.ActionListener;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.input.InputManager;
import com.jme3.scene.Spatial;
import org.game.*;
import org.game.contexts.WorldContext;

public class WorldInteractionInput implements ActionListener {
    private final WorldContext worldContext;
    private final InteractionCommandFactory commandFactory;
    private final RaycastHelper raycastHelper;
    private final ShopUI shopUI;

    public WorldInteractionInput(WorldContext worldContext,
                                 InteractionCommandFactory commandFactory, Camera camera,
                                 InputManager inputManager, ShopUI shopUI) {
        this.worldContext = worldContext;
        this.commandFactory = commandFactory;
        this.shopUI = shopUI;
        this.raycastHelper = new RaycastHelper(camera, inputManager, worldContext.world());
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (InputNames.WORLD_INTERACT.equals(name)) {
            handleWorldClick();
        }
    }

    private void handleWorldClick() {
        if (handleUIClick()) return;

        Vector3i targetBlock = raycastHelper.getBlockUnderCursor();
        Spatial clickedObject = getClickedObject();

        InteractionCommand command = commandFactory.createCommand(targetBlock, clickedObject);
        if (command != null && command.isValidTarget()) {
            boolean executed = command.execute();
            if (executed) {
                System.out.println("Command executed: " + command.getDescription());
            } else {
                System.out.println("Command failed to execute: " + command.getDescription());
            }
        }
    }

    private boolean handleUIClick() {
        if (shopUI.isVisible()) {
            Vector2f cursorPos = raycastHelper.getCursorPosition();
            shopUI.handleClick(cursorPos.x, cursorPos.y);
            return true;
        }
        return false;
    }

    private Spatial getClickedObject() {
        return raycastHelper.getClickedObject(worldContext.shopModel(),
                worldContext.houseModel());
    }
}
