package frc.robot.interfaces;



import org.strongback.Executable;

/*
 * The climber consists two tube in tubes that are on either side of the robot.
 * There is a spring providing constant force pushing inner tube, and a winch at the base
 * to control the extension of the inner tube.
 * 
 * Currently the two tubes are controlled by separate motors but should be driven in sync
 * The motors have limit switches at the base and use encoders to record position and set the top
 * limit
 */
public interface Climber extends Subsystem, Executable, DashboardUpdater {

    public void setDutyCycle(double dutyCycle);

    public double getDutyCycle();
}
