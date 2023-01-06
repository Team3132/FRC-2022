package org.strongback.components.ui;



import frc.robot.lib.MathUtil;
import java.util.function.BooleanSupplier;

/**
 * Allows actions to be triggered based on the position of the joystick.
 * 
 * There needs to be a deadband between buttons to prevent mistriggering
 * north/south or west/east when trying to get to one of the corners.
 */
public class JoystickAxis extends Axis {

    private ContinuousRange x;
    private ContinuousRange y;
    private BooleanSupplier button; // Depending on device, may not be supported.
    private double threshold; // How far from the centre before triggering, eg 0.3

    public JoystickAxis(String device, String name, ContinuousRange x, ContinuousRange y,
            BooleanSupplier button,
            double threshold) {
        super(device, name);
        this.x = x;
        this.y = y;
        this.button = button;
        this.threshold = threshold;
    }

    /**
     * Returns the current angle of the joystick. If centered is true, then it won't be used.
     * 
     * @return an angle in degrees where 0 is up/north, 90 is right/east and 270 is left/west
     */
    protected double getBearing() {
        // +y is up on the joystick.
        // -x is left on the joystick.
        // Need to return degrees from north going clockwise.
        double angleDegrees = MathUtil.atan2(y.read(), x.read());
        return MathUtil.angleToBearing(angleDegrees);
    }

    protected boolean isCentred() {
        return Math.abs(x.read()) < threshold && Math.abs(y.read()) < threshold;
    }

    @Override
    public Trigger button() {
        return new Trigger(device, "press on " + name, button);
    }

    // The Axis superclass implements all of the other angles, eg north().
}
