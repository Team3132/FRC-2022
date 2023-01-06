package org.strongback.components.ui;



import frc.robot.lib.MathUtil;

/**
 * Allows actions to be triggered based on the position of the joystick or DPad.
 */
public abstract class Axis {
    protected String device; // eg "gamepad"
    protected String name; // eg "DPad" or "left thumbpad"


    public Axis(String device, String name) {
        this.device = device;
        this.name = name;
    }

    /**
     * Returns true if there is no direction.
     * 
     * @return true if centred
     */
    protected abstract boolean isCentred();

    /**
     * Returns the current angle of the joystick. If centered is true, then it won't be used.
     * 
     * @return an angle in degrees where 0 is up/north, 90 is right/east and 270 is left/west
     */
    protected abstract double getBearing();

    /**
     * Trigger an action on no direction.
     * 
     * @return triggers true on center.
     */
    public Trigger centre() {
        return new Trigger(device, "center on " + name, () -> isCentred());
    }

    /**
     * Trigger an action on north.
     * 
     * @return triggers true on north.
     */
    public Trigger north() {
        return triggerOnBearing("north", CompassPoint.NORTH);
    }

    /**
     * Trigger an action on north and west.
     * 
     * @return triggers true on north and west.
     */
    public Trigger northWest() {
        return triggerOnBearing("north west", CompassPoint.NORTH_WEST);
    }

    /**
     * Trigger an action on west.
     * 
     * @return triggers true on west.
     */
    public Trigger west() {
        return triggerOnBearing("west", CompassPoint.WEST);
    }

    /**
     * Trigger an action on south west.
     * 
     * @return triggers true on south and west.
     */
    public Trigger southWest() {
        return triggerOnBearing("south west", CompassPoint.SOUTH_WEST);
    }

    /**
     * Trigger an action on south.
     * 
     * @return triggers true on south.
     */
    public Trigger south() {
        return triggerOnBearing("south", CompassPoint.SOUTH);
    }

    /**
     * Trigger an action on south and east.
     * 
     * @return triggers true on south and east.
     */
    public Trigger southEast() {
        return triggerOnBearing("south east", CompassPoint.SOUTH_EAST);
    }

    /**
     * Trigger an action on east.
     * 
     * @return triggers true on east.
     */

    public Trigger east() {
        return triggerOnBearing("east", CompassPoint.EAST);
    }

    /**
     * Trigger an action on north east.
     * 
     * @return triggers true on north and east.
     */
    public Trigger northEast() {
        return triggerOnBearing("north east", CompassPoint.NORTH_EAST);
    }

    /**
     * Trigger an action on button press.
     * 
     * @return triggers true on button press.
     */
    public abstract Trigger button();


    private Trigger triggerOnBearing(String action, CompassPoint point) {
        return new Trigger(device, action + " " + name,
                () -> !isCentred() && CompassPoint.getClosestPoint(getBearing()) == point);
    }

    /**
     * Holds the bearing (in degrees) for each of the eight compass points.
     */
    public enum CompassPoint {
        // Order is important here.
        // Values match numbers used by the DPad on the gamepad.
        NORTH(0), NORTH_EAST(45), EAST(90), SOUTH_EAST(135), SOUTH(180), SOUTH_WEST(225), WEST(
                270), NORTH_WEST(315);

        final double bearing;

        private CompassPoint(double bearing) {
            this.bearing = bearing;
        }

        public static CompassPoint getClosestPoint(double bearing) {
            bearing = MathUtil.normaliseBearing(bearing);
            for (CompassPoint point : CompassPoint.values()) {
                if (bearing <= point.bearing + 360 / 8 / 2) {
                    return point;
                }
            }
            return NORTH;
        }
    }
}
