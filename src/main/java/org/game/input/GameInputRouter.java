package org.game.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;

import java.util.ArrayList;
import java.util.List;

public class GameInputRouter implements ActionListener, AnalogListener {
    private final InputManager input;
    private final List<AnalogListener> analogModules = new ArrayList<>();
    private final List<ActionListener> actionModules = new ArrayList<>();

    public GameInputRouter(InputManager input) {
        this.input = input;
        registerMappings();
    }

    public void addAnalogModule(AnalogListener m){ analogModules.add(m); }
    public void addActionModule(ActionListener m){ actionModules.add(m); }

    private void registerMappings() {
        input.addMapping(InputNames.MOVE_FWD,  new KeyTrigger(KeyInput.KEY_W));
        input.addMapping(InputNames.MOVE_BACK, new KeyTrigger(KeyInput.KEY_S));
        input.addMapping(InputNames.MOVE_LEFT, new KeyTrigger(KeyInput.KEY_A));
        input.addMapping(InputNames.MOVE_RIGHT,new KeyTrigger(KeyInput.KEY_D));

        input.addMapping(InputNames.CTRL,
                new KeyTrigger(KeyInput.KEY_LCONTROL),
                new KeyTrigger(KeyInput.KEY_RCONTROL));

        input.addMapping(InputNames.WHEEL_UP,   new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        input.addMapping(InputNames.WHEEL_DOWN, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        for (int i=1;i<=9;i++) {
            input.addMapping(InputNames.hotbarSlot(i), new KeyTrigger(KeyInput.KEY_1 + (i-1)));
        }

        input.addMapping(InputNames.HOTBAR_NEXT, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        input.addMapping(InputNames.HOTBAR_PREV, new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        input.addListener(this,
                InputNames.MOVE_FWD, InputNames.MOVE_BACK, InputNames.MOVE_LEFT, InputNames.MOVE_RIGHT,
                InputNames.CTRL
        );
        for (int i=1;i<=9;i++) input.addListener(this, InputNames.hotbarSlot(i));

        input.addListener(this, InputNames.HOTBAR_NEXT, InputNames.HOTBAR_PREV);
        input.addListener(this, InputNames.WHEEL_UP, InputNames.WHEEL_DOWN,
                InputNames.HOTBAR_NEXT, InputNames.HOTBAR_PREV);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        for (ActionListener listener : actionModules) listener.onAction(name, isPressed, tpf);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        for (AnalogListener listener : analogModules) listener.onAnalog(name, value, tpf);
    }
}
