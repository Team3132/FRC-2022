package frc.robot.drive.routines;



import frc.robot.interfaces.DriveTelemetry;
import java.util.function.DoubleSupplier;
import org.strongback.components.Clock;

/**
 * Use supplied speed and turn rate to drive using a positional PID.
 */
public class PIDDrive extends AutoDriveBase {
    private DoubleSupplier speed, turn;

    /**
     * Create a PIDDrive routine.
     * 
     * @param name the name to register with the drive routine with.
     * @param speed the supplier of the target speed in metres/sec
     * @param turn the supplier target delta to apply to each wheel in metres/sec
     * @param maxVelocityJerk the maximum change in wheel speeds per second
     * @param telemetry details about how the drivebase is performing
     * @param clock access to the current time
     */
    public PIDDrive(String name, DoubleSupplier speed, DoubleSupplier turn,
            DriveTelemetry telemetry, Clock clock) {
        super(name, telemetry, clock);
        this.speed = speed;
        this.turn = turn;
    }

    @Override
    public double getTargetSpeed() {
        return speed.getAsDouble();
    }


    @Override
    public double getTargetTurn() {
        return turn.getAsDouble();
    }


    @Override
    public boolean hasFinished() {
        // Allow it to be interrupted any time.
        return true;
    }
}
