package frc.robot.interfaces;



import org.strongback.Executable;

/**
 * Single wheel shooter driven by two motors with an adjustable hood controlled by a servo.
 */
public interface Shooter extends Subsystem, Executable, DashboardUpdater {

    /**
     * Sets the speed on the shooter wheel.
     * 
     * @param rps is the target speed that is being given to the shooter.
     */
    public void setTargetRPS(double rps);

    public double getTargetRPS();

    public boolean isAtTargetSpeed();

    public void setHoodTargetAngle(double degrees);

    public double getHoodTargetAngle();

    public boolean isHoodAtTargetAngle();
}
