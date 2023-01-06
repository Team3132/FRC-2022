package frc.robot.interfaces;


/**
 * Interface to set and query drivebase telemetry.
 */
public abstract interface DriveTelemetry {

    /**
     * Reset the distance travelled of the left wheel.
     * 
     * @param pos the new position in metres.
     */
    public void setLeftDistance(double pos);

    /**
     * Reset the distance travelled of the right wheel.
     * 
     * @param pos the new position in metres.
     */
    public void setRightDistance(double pos);

    /**
     * Get the distance travelled by the left wheel.
     * 
     * @return position in metres
     */
    public double getLeftDistance();

    /**
     * Get the distance travelled by the right wheel.
     * 
     * @return position in metres
     */
    public double getRightDistance();

    /**
     * Get the current speed of the left wheel.
     * 
     * @return position in metres/second.
     */
    public double getLeftSpeed();

    /**
     * Get the current speed of the right wheel.
     * 
     * @return position in metres/second.
     */
    public double getRightSpeed();
}
