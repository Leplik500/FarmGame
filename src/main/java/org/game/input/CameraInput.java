package org.game.input;

import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.input.ChaseCamera;
import org.game.GameConfig;

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
            return;
        }

        if (isPressed && InputNames.CAMERA_RESET_ZOOM.equals(name)) {
            resetZoom();
        }
    }

    private void resetZoom() {
        chaseCam.setDefaultDistance(GameConfig.CAM_DEFAULT_DISTANCE);
    }


    @Override
    public void onAnalog(String name, float value, float tpf) {
        // ничего — колесо обрабатывает сама ChaseCamera, когда зажата Ctrl
    }

    public boolean isCtrlDown() { return ctrlDown; }
}

