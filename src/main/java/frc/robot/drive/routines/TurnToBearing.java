package frc.robot.drive.routines;



import frc.robot.Config;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.interfaces.Location;
import frc.robot.interfaces.Vision;
import frc.robot.lib.MathUtil;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Clock;

/**
 * Turns the robot on the spot to a specific bearing.
 * Useful in auto routines to turn to face a certain heading.
 */
public class TurnToBearing extends AutoDriveBase {
    private double targetBearing = 0;
    private Location location;

    public TurnToBearing(DriveTelemetry telemetry,
            Vision vision, Location location, Clock clock) {
        super("turnToBearing", telemetry, clock);
        this.location = location;
        Chart.register(() -> targetBearing, "Drive/turnToBearing/target");
        Chart.register(this::getAngleDelta, "Drive/turnToBearing/diff");
    }

    @Override
    public double getTargetSpeed() {
        // Only turn.
        return 0;
    }


    @Override
    public double getTargetTurn() {
        // At 5 degrees out, want something like 0.1 m/s
        // 0.1 = scale * 5
        // scale = 0.1 / 5
        // scale = 0.02
        // Not that there is a cap on drivebase speed to prevent it going too fast at bigger angles.
        return Config.drivebase.routine.turnToBearing.scale * getAngleDelta();
    }


    public double getAngleDelta() {
        double actualBearing = location.getBearing();
        return MathUtil.clamp(MathUtil.getAngleDiff(actualBearing, targetBearing), -100, 100);
    }


    @Override
    public boolean hasFinished() {
        return Math.abs(getAngleDelta()) < 2;
    }

    @Override
    public void reset(DriveRoutineParameters parameters) {
        // Save the target bearing.
        targetBearing = parameters.value;
        super.reset(parameters);
    }
}
