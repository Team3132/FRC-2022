package frc.robot.lib;

/**
 * Joystick game-pad button mapping.
 * This makes recognizing the buttons used easier.
 */
public class GamepadButtonsX {

    // GamepadButtons X - Y
    public static final int X_BUTTON = 3;
    public static final int A_BUTTON = 1;
    public static final int B_BUTTON = 2;
    public static final int Y_BUTTON = 4;

    // Bumpers
    public static final int LEFT_BUMPER = 5;
    public static final int RIGHT_BUMPER = 6;
    public static final int LEFT_TRIGGER = 2;
    public static final int RIGHT_TRIGGER = 3;

    // Back and Start
    public static final int BACK_BUTTON = 7;
    public static final int START_BUTTON = 8;

    // Thumbstick press
    public static final int LEFT_THUMBSTICK_CLICK = 9;
    public static final int RIGHT_THUMBSTICK_CLICK = 10;

    // Axis
    public static final int LEFT_X_AXIS = 0;
    public static final int LEFT_Y_AXIS = 1;
    public static final int LEFT_TRIGGER_AXIS = 2;
    public static final int RIGHT_TRIGGER_AXIS = 3;
    public static final int RIGHT_X_AXIS = 4;
    public static final int RIGHT_Y_AXIS = 5;

    public static final double TRIGGER_THRESHOLD = 0.6; // trigger greater than this is considered
                                                        // 'on' as a switch
    public static final double AXIS_THRESHOLD = 0.3; // axis greater than this is considered 'on' as
                                                     // a switch

    public static final int DPAD_NORTH = 0;
    public static final int DPAD_NORTH_EAST = 45;
    public static final int DPAD_EAST = 90;
    public static final int DPAD_SOUTH_EAST = 135;
    public static final int DPAD_SOUTH = 180;
    public static final int DPAD_SOUTH_WEST = 225;
    public static final int DPAD_WEST = 270;
    public static final int DPAD_NORTH_WEST = 315;
    public static final int DPAD_UNTRIGGERED = -1;

}
