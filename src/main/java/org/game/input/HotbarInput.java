package org.game.input;

import com.jme3.input.controls.*;
import org.game.ui.Hotbar;

public class HotbarInput implements ActionListener, AnalogListener {
    private final Hotbar hotbar;
    private final CameraInput cameraInput; // чтобы знать, зажата ли Ctrl

    public HotbarInput(Hotbar hotbar, CameraInput cameraInput) {
        this.hotbar = hotbar;
        this.cameraInput = cameraInput;
    }
    
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;
        if (name.startsWith("hotbar_")) {
            if (InputNames.HOTBAR_NEXT.equals(name) || InputNames.HOTBAR_PREV.equals(name)) return;
            String tail = name.substring("hotbar_".length());
            try {
                int idx = Integer.parseInt(tail) - 1;
                hotbar.select(idx);
            } catch (NumberFormatException ignore) {}
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (cameraInput.isCtrlDown()) return;
        if (InputNames.HOTBAR_NEXT.equals(name)) hotbar.step(+1);
        else if (InputNames.HOTBAR_PREV.equals(name)) hotbar.step(-1);
    }
}

