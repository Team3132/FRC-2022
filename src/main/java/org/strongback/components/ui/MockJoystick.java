package org.strongback.components.ui;



import edu.wpi.first.wpilibj.Joystick;

/**
 * Fakes a plugged in joystick. Returns default values as if joystick is plugged in and no buttons
 * are being pressed.
 */
public class MockJoystick extends Joystick {

    public MockJoystick(int port) {
        super(port);
    }

    /**
     * Get the button value (starting at button 1).
     *
     * <p>
     * The buttons are returned in a single 16 bit value with one bit representing the state of
     * each button. The appropriate button is returned as a boolean value.
     *
     * <p>
     * This method returns true if the button is being held down at the time that this method is
     * being called.
     *
     * @param button The button number to be read (starting at 1)
     * @return The state of the button.
     */
    @Override
    public boolean getRawButton(int button) {
        return false;
    }

    /**
     * Get the value of the axis.
     *
     * @param axis The axis to read, starting at 0.
     * @return The value of the axis.
     */
    @Override
    public double getRawAxis(int axis) {
        return 0;
    }

    /**
     * Get the angle in degrees of a POV on the HID.
     *
     * <p>
     * The POV angles start at 0 in the up direction, and increase clockwise (eg right is 90,
     * upper-left is 315).
     *
     * @param pov The index of the POV to read (starting at 0)
     * @return the angle of the POV in degrees, or -1 if the POV is not pressed.
     */
    @Override
    public int getPOV(int pov) {
        return 0;
    }

    @Override
    public int getPOV() {
        return getPOV(0);
    }

    /**
     * Get the number of axes for the HID.
     *
     * @return the number of axis for the current HID
     */
    @Override
    public int getAxisCount() {
        return 32;
    }

    /** For the current HID, return the number of POVs. */
    @Override
    public int getPOVCount() {
        return 32;
    }

    /** For the current HID, return the number of buttons. */
    @Override
    public int getButtonCount() {
        return 32;
    }

    /**
     * Get the z position of the HID.
     *
     * @return the z position
     */
    @Override
    public double getZ() {
        return 0;
    }

    /**
     * Get the twist value of the current joystick. This depends on the mapping of the joystick
     * connected to the current port.
     *
     * @return The Twist value of the joystick.
     */
    @Override
    public double getTwist() {
        return 0;
    }

    /**
     * Get the throttle value of the current joystick. This depends on the mapping of the joystick
     * connected to the current port.
     *
     * @return The Throttle value of the joystick.
     */
    @Override
    public double getThrottle() {
        return 0;
    }
}
