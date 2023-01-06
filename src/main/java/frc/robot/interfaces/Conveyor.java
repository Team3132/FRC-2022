package frc.robot.interfaces;



import org.strongback.Executable;

/**
 * This is a system to store balls from the intake and then pass them to the shooter.
 */
public interface Conveyor extends Subsystem, Executable, DashboardUpdater {

    public double getDutyCycle();

    public void setDutyCycle(double dutyCycle);
}
