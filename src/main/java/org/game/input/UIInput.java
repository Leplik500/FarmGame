package org.game.input;

import com.jme3.input.controls.ActionListener;
import org.game.ui.ShopUI;

public class UIInput implements ActionListener {
    private final ShopUI shopUI;

    public UIInput(ShopUI shopUI) {
        this.shopUI = shopUI;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) return;

        if (InputNames.CLOSE_UI.equals(name)) {
            if (shopUI.isVisible()) {
                shopUI.setVisible(false);
                System.out.println("Shop closed");
            }
        }
    }
}
