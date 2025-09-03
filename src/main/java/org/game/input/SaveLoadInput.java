package org.game.input;

import com.jme3.input.controls.ActionListener;
import org.game.GameSaveManager;
import org.game.Main;

public class SaveLoadInput implements ActionListener {
    private final Main gameInstance;
    private final GameSaveManager saveManager;

    public SaveLoadInput(Main gameInstance, GameSaveManager saveManager) {
        this.gameInstance = gameInstance;
        this.saveManager = saveManager;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;

        switch (name) {
            case InputNames.SAVE_GAME:
                saveManager.saveGame(gameInstance);
                break;
            case InputNames.LOAD_GAME:
                saveManager.loadGame(gameInstance);
                break;
        }
    }
}
