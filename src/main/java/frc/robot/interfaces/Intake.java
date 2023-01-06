package frc.robot.interfaces;

/**
 * Ball intake. Spinning rollers on a extendable pneumatically driven arm.
 */



import org.strongback.Executable;
import org.strongback.components.Solenoid.Position;

public interface Intake extends Subsystem, Executable, DashboardUpdater {

    /**
     * Set the position of the intake, either extended or retracted.
     * 
     * @param position extended or retracted.
     * @return this
     */
    public void setPosition(Position position);

    /**
     * A way to check if the intake has finished moving into the desired position.
     * 
     * @return true if it has finished moving.
     */
    public boolean isInPosition();

    /**
     * @return the state of the intake solenoid.
     */
    public boolean isExtended();

    public boolean isRetracted();

    // Set intake speed
    public void setTargetRPS(double rps);

    public double getTargetRPS();
}
