package frc.robot.drive.routines;



import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.Config;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.interfaces.Location;
import frc.robot.interfaces.Vision;
import frc.robot.interfaces.Vision.TargetDetails;
import frc.robot.lib.MathUtil;
import org.strongback.components.Clock;

/**
 * Turn the robot to face the vision target if one is visible.
 * 
 * It will give up if it can't see the goal or if it's pointing at the goal.
 * 
 * Differs from VisionAssist because there is no driver control of the speed.
 */
public class VisionAim extends AutoDriveBase {
    private Vision vision;
    private Location location;
    private Clock clock;
    private boolean finished = false;

    public VisionAim(DriveTelemetry telemetry,
            Vision vision, Location location, Clock clock) {
        super("visionAim", telemetry, clock);
        this.vision = vision;
        this.location = location;
        this.clock = clock;
    }

    @Override
    public double getTargetSpeed() {
        return 0;
    }


    @Override
    public double getTargetTurn() {
        if (vision == null || !vision.isConnected()) {
            finished = true; // Can't do anything if vision isn't working.
            return 0;
        }
        TargetDetails target = vision.getTargetDetails();
        if (!target.isValid(clock.currentTime())) {
            finished = true; // No target, give up.
            return 0;
        }
        // We have a recent target position relative to the robot starting position.
        Pose2d robotPose = location.getCurrentPose();
        double angle = -MathUtil.absoluteToRelativeAngle(robotPose, target.pose).getDegrees();
        finished = Math.abs(angle) < Config.drivebase.routine.visionAim.angleToleranceDegrees;
        return Config.drivebase.routine.visionAim.angleScale * angle;
    }

    @Override
    public boolean hasFinished() {
        return finished;
    }

    @Override
    public void reset(DriveRoutineParameters parameters) {
        super.reset(parameters);
        finished = false; // Reset finished in case getTargetTurn hasn't been called recently.
    }
}
