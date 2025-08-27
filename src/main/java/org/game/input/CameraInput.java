package org.game.input;

import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.input.ChaseCamera;

public class CameraInput implements ActionListener, AnalogListener {
    private final ChaseCamera chaseCam;
    private boolean ctrlDown = false;

    public CameraInput(ChaseCamera chaseCam) {
        this.chaseCam = chaseCam;
        chaseCam.setZoomInTrigger();
        chaseCam.setZoomOutTrigger();
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (InputNames.CTRL.equals(name)) {
            ctrlDown = isPressed;
            if (ctrlDown) {
                chaseCam.setZoomInTrigger (new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
                chaseCam.setZoomOutTrigger(new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
            } else {
                chaseCam.setZoomInTrigger();
                chaseCam.setZoomOutTrigger();
            }
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        // ничего — колесо обрабатывает сама ChaseCamera, когда зажата Ctrl
    }

    public boolean isCtrlDown() { return ctrlDown; }
}

