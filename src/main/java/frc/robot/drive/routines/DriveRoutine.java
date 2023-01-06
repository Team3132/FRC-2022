package frc.robot.drive.routines;



import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.interfaces.LogHelper;
import frc.robot.lib.MathUtil;
import org.strongback.components.Motor.ControlMode;

/*
 * This interface class defines the interface to the drive controls.
 * 
 * Each drive control system can operate independently.
 */
public abstract class DriveRoutine implements LogHelper {
    protected String name;
    protected ControlMode mode;

    protected DriveRoutine(String name, ControlMode mode) {
        this.name = name;
        this.mode = mode;
    }

    /*
     * Whether to drive both fieldConfig, or just the left or right side for our Arcade to Tank
     * conversion.
     */
    public enum DriveSide {
        BOTH, LEFT, RIGHT
    };

    /**
     * This drive routine was requested by an action.
     * Get any neessary details from it (eg waypoints)
     * 
     * @param parameters
     */
    public void reset(DriveRoutineParameters parameters) {}

    /**
     * DriveMotion determines the power that should be applied to the left and right
     * hand fieldConfig of the robot by the drivebase.
     * 
     * @param leftSpeed The current speed of the robot on the left side.
     * @param rightSpeed The current speed of the robot on the right side.
     * @return The power to apply to each side of the robot by the drivebase.
     */
    public abstract DriveMotion getMotion(double leftSpeed, double rightSpeed);

    /**
     * Returns true if there is nothing more to do.
     * Used by the controller to know if it needs to keep waiting
     * Most routines will return true.
     * 
     * @return if no more time is needed to finish the current driving.
     */
    public boolean hasFinished() {
        return true;
    }

    /**
     * Return the name of the Drive Control
     * 
     * @return name of the drive control
     */
    public String getName() {
        return name;
    }

    /**
     * Return the name of the Drive Control
     * 
     * @return name of the drive control
     */
    public ControlMode getControlMode() {
        return mode;
    }

    /**
     * Activate this driveControl. Perform any initialisation needed with the assumption
     * that the robot is currently in the correct position
     */
    public void enable() {

    }

    /**
     * Prepare the drive control for deactivation. Stop all independent tasks and safe all controls.
     * Deactivate can be called before activate.
     */
    public void disable() {

    }

    public double limit(double value) {
        if (value < -1.0)
            value = -1.0;
        if (value > 1.0)
            value = 1.0;
        return value;
    }

    public DriveMotion arcadeToTank(double moveValue, double turnValue, double scale) {
        return arcadeToTank(moveValue, turnValue, scale, DriveSide.BOTH);
    }

    public DriveMotion arcadeToTank(double moveValue, double turnValue, double scale,
            DriveSide driveSide) {
        // double im = moveValue;
        // double it = turnValue;
        moveValue = limit(moveValue);
        turnValue = limit(turnValue);
        double leftMotorSpeed = 0;
        double rightMotorSpeed = 0;

        if (moveValue > 0.0) {
            if (turnValue > 0.0) {
                leftMotorSpeed = moveValue - turnValue;
                rightMotorSpeed = Math.max(moveValue, turnValue);
            } else {
                leftMotorSpeed = Math.max(moveValue, -turnValue);
                rightMotorSpeed = moveValue + turnValue;
            }
        } else {
            if (turnValue > 0.0) {
                leftMotorSpeed = -Math.max(-moveValue, turnValue);
                rightMotorSpeed = moveValue + turnValue;
            } else {
                leftMotorSpeed = moveValue - turnValue;
                rightMotorSpeed = -Math.max(-moveValue, -turnValue);
            }
        }
        /*
         * Adjust here for left and right only changes if necessary.
         */
        switch (driveSide) {
            default:
            case BOTH:
                // all is good. Do nothing
                break;
            case LEFT:
                leftMotorSpeed = leftMotorSpeed - rightMotorSpeed;
                rightMotorSpeed = 0.0;
                break;
            case RIGHT:
                leftMotorSpeed = 0.0;
                rightMotorSpeed = rightMotorSpeed - leftMotorSpeed;
                break;
        }
        leftMotorSpeed = scale * MathUtil.clamp(leftMotorSpeed, -1.0, 1.0);
        rightMotorSpeed = scale * MathUtil.clamp(rightMotorSpeed, -1.0, 1.0);
        // System.out.printf("A2T(%f, %f) -> %f,%f\n", im, it, leftMotorSpeed, rightMotorSpeed);
        return new DriveMotion(leftMotorSpeed, rightMotorSpeed);
    }
}
