package org.game.core;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;
import org.game.Main;
import org.game.contexts.PlayerContext;
import org.game.contexts.WorldContext;
import org.game.facades.GameUIFacade;
import org.game.facades.GameWorldFacade;
import org.game.facades.PlayerFacade;
import org.game.input.*;
import org.game.player.CollisionChecker;
import org.game.systems.GameSaveManager;

public class GameSystemBuilder {
    private final SimpleApplication app;
    private PlayerFacade playerFacade;
    private GameWorldFacade worldFacade;
    private GameUIFacade uiFacade;
    private PlayerInputHandler inputHandler;
    private final Node camTarget;

    public GameSystemBuilder(SimpleApplication app) {
        this.app = app;
        this.camTarget = new Node("CamTarget");
    }

    public GameSystemBuilder buildPlayer() {
        this.playerFacade = new PlayerFacade(app.getAssetManager(), app.getRootNode(), app.getCamera());

        app.getRootNode().attachChild(camTarget);
        camTarget.setLocalTranslation(playerFacade.getPlayer().getLocalTranslation());
        camTarget.setLocalRotation(playerFacade.getPlayer().getLocalRotation());

        return this;
    }

    public GameSystemBuilder buildWorld() {
        this.worldFacade = new GameWorldFacade(app.getAssetManager(), app.getRootNode());

        AppStateManager stateManager = app.getStateManager();
        stateManager.attach(worldFacade.getMoistureState());
        stateManager.attach(worldFacade.getDayNightCycle());
        stateManager.attach(worldFacade.getPlantGrowthState());

        return this;
    }

    public GameSystemBuilder buildUI() {
        app.getFlyByCamera().setEnabled(false);

        this.uiFacade = new GameUIFacade(
                app.getAssetManager(),
                app.getGuiNode(),
                app.getRootNode(),
                app.getCamera(),
                app.getInputManager(),
                worldFacade.getBlockWorld(),
                camTarget
        );
        return this;
    }

    public GameSystemBuilder buildInput() {
        this.inputHandler = new PlayerInputHandler();

        CollisionChecker collisionChecker = new CollisionChecker(
                worldFacade.getShopModel(),
                worldFacade.getHouseModel(),
                worldFacade.getPlantGrowthState()
        );
        playerFacade.getMovementController().setCollisionChecker(collisionChecker);

        GameInputRouter router = new GameInputRouter(app.getInputManager());
        InputModules modules = createInputModules();
        registerInputModules(router, modules);

        return this;
    }

    private InputModules createInputModules() {
        CameraInput cameraInput = new CameraInput(uiFacade.getChaseCamera());
        HotbarInput hotbarInput = new HotbarInput(uiFacade.getHotbar(), cameraInput);
        MovementInput movementInput = new MovementInput(inputHandler);
        UIInput uiInput = new UIInput(uiFacade.getShopUI());
        GameSaveManager saveManager = new GameSaveManager();

        WorldContext worldContext = new WorldContext(
                worldFacade.getBlockWorld(),
                worldFacade.getPlantGrowthState(),
                worldFacade.getMoistureState(),
                worldFacade.getPlantFactory(),
                uiFacade.getInventoryManager(),
                worldFacade.getShopModel(),
                worldFacade.getHouseModel(),
                worldFacade.getDayNightCycle(),
                app.getAssetManager()
        );

        PlayerContext playerContext = new PlayerContext(
                playerFacade.getPlayer(),
                playerFacade.getMovementController()
        );

        InteractionCommandFactory commandFactory = new InteractionCommandFactory(
                worldContext, playerContext, uiFacade.getHotbar(), uiFacade.getShopUI()
        );

        WorldInteractionInput worldInput = new WorldInteractionInput(
                worldContext, commandFactory, app.getCamera(),
                app.getInputManager(), uiFacade.getShopUI()
        );

        return new InputModules(cameraInput, hotbarInput, movementInput,
                uiInput, worldInput, saveManager);
    }

    private void registerInputModules(GameInputRouter router, InputModules modules) {
        router.addActionModule(modules.cameraInput);
        router.addAnalogModule(modules.cameraInput);
        router.addActionModule(modules.hotbarInput);
        router.addAnalogModule(modules.hotbarInput);
        router.addActionModule(modules.movementInput);
        router.addActionModule(modules.worldInput);
        router.addActionModule(modules.uiInput);
        router.addActionModule(new SaveLoadInput((Main) app, modules.saveManager));
    }

    public GameSystemBuilder build() {
        return this;
    }

    public GameUpdateManager createUpdateManager() {
        return new GameUpdateManager(playerFacade, worldFacade, uiFacade, inputHandler, camTarget);
    }

    public PlayerFacade getPlayerFacade() { return playerFacade; }
    public GameWorldFacade getWorldFacade() { return worldFacade; }
    public GameUIFacade getUiFacade() { return uiFacade; }
    public PlayerInputHandler getInputHandler() { return inputHandler; }

    private record InputModules(CameraInput cameraInput,
                                HotbarInput hotbarInput,
                                MovementInput movementInput,
                                UIInput uiInput,
                                WorldInteractionInput worldInput,
                                GameSaveManager saveManager) {
    }
}
