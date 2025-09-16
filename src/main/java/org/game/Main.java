package org.game;

import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.ActionListener;
import com.jme3.system.AppSettings;
import org.game.core.GameSystemBuilder;
import org.game.core.GameUpdateManager;
import org.game.facades.GameUIFacade;
import org.game.facades.GameWorldFacade;
import org.game.facades.PlayerFacade;
import org.game.input.PlayerInputHandler;
import org.game.states.DayNightCycle;
import org.game.states.PlantGrowthState;
import org.game.ui.Hotbar;
import org.game.ui.MoneyDisplay;
import org.game.utils.GameConfig;
import org.game.world.BlockWorld;
import org.game.world.PlantFactory;
import com.jme3.scene.Spatial;

import java.awt.*;

public class Main extends SimpleApplication implements ActionListener {

    private PlayerFacade playerFacade;
    private GameWorldFacade worldFacade;
    private GameUIFacade uiFacade;
    private PlayerInputHandler inputHandler;
    private GameUpdateManager updateManager;

    public static void main(String[] args) {
        try {
            AppSettings settings = new AppSettings(true);

            if (GameConfig.FULLSCREEN) {
                settings = createFullscreenSettings();
            } else {
                settings.setResolution(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
                settings.setFullscreen(false);
            }

            Main app = new Main();
            app.setSettings(settings);
            app.start();
        }
        catch (Exception e) {
            System.err.println("Application crashed: " + e.getMessage());
            e.printStackTrace();

            System.out.println("Press Enter to exit...");
            try {
                System.in.read();
            } catch (Exception ignored) {}
        }
    }

    private static AppSettings createFullscreenSettings() {
        AppSettings settings = new AppSettings(true);
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode[] modes = device.getDisplayModes();

        DisplayMode mode = modes[0];
        settings.setResolution(mode.getWidth(), mode.getHeight());
        settings.setFrequency(mode.getRefreshRate());
        settings.setBitsPerPixel(mode.getBitDepth());
        settings.setFullscreen(device.isFullScreenSupported());

        return settings;
    }

    @Override
    public void simpleInitApp() {
        GameSystemBuilder builder = new GameSystemBuilder(this)
                .buildPlayer()
                .buildWorld()
                .buildUI()
                .buildInput()
                .build();

        this.playerFacade = builder.getPlayerFacade();
        this.worldFacade = builder.getWorldFacade();
        this.uiFacade = builder.getUiFacade();
        this.inputHandler = builder.getInputHandler();
        this.updateManager = builder.createUpdateManager();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (isPressed) {
            inputHandler.handleKeyPress(name);
        } else {
            inputHandler.handleKeyRelease(name);
        }
        inputHandler.updateWalkingState();
    }

    @Override
    public void simpleUpdate(float tpf) {
        updateManager.update(tpf);
        uiFacade.updateViewport(cam.getWidth(), cam.getHeight());
    }

    public Spatial getPlayer() { return playerFacade.getPlayer(); }
    public MoneyDisplay getMoneyDisplay() { return uiFacade.getMoneyDisplay(); }
    public Hotbar getHotbar() { return uiFacade.getHotbar(); }
    public BlockWorld getBlockWorld() { return worldFacade.getBlockWorld(); }
    public PlantGrowthState getPlantGrowthState() { return worldFacade.getPlantGrowthState(); }
    public PlantFactory getPlantFactory() { return worldFacade.getPlantFactory(); }
    public DayNightCycle getDayNightCycle() { return worldFacade.getDayNightCycle(); }
}
