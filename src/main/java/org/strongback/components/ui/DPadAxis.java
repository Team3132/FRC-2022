package org.strongback.components.ui;

/**
 * Allows actions to be triggered based on which DPad angle is pressed.
 * 
 * For details on the DPad see:
 * http://controls.coderedrobotics.com/programminglessons/4.html
 */
public class DPadAxis extends Axis {
    private DirectionalAxis direction;

    public DPadAxis(String device, String name, DirectionalAxis direction) {
        super(device, name);
        this.direction = direction;
    }

    @Override
    protected double getBearing() {
        if (direction.getDirection() == -1) {
            // This is how it indicates that there is no angle.
            return 0; // Really north, but isCentred() will also return true.
        }
        return direction.getDirection();
    }

    @Override
    protected boolean isCentred() {
        // direction == -1 when centred.
        return direction.getDirection() == -1;
    }

    @Override
    public Trigger button() {
        // Any direction is counted as a button press.
        return new Trigger(device, "press on " + name, () -> direction.getDirection() != -1);
    }

    // The Axis superclass implements all of the other angles, eg north().
}
