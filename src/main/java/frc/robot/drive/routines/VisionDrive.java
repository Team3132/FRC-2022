package frc.robot.drive.routines;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.Config;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Location;
import frc.robot.interfaces.Vision;
import frc.robot.interfaces.Vision.TargetDetails;
import frc.robot.lib.MathUtil;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Clock;

/**
 * Drive to a point a configured distance in front of the goal and
 * turn the robot to the target.
 */
public class VisionDrive extends AutoDriveBase {
    private Vision vision;
    private Location location;
    private Clock clock;

    public VisionDrive(DriveTelemetry telemetry,
            Vision vision, Location location, Clock clock) {
        super("visionAssist", telemetry,
                clock);
        this.vision = vision;
        this.location = location;
        this.clock = clock;
        Chart.register(() -> getVisionWaypoint().getX(), "Drive/visionDrive/waypoint/x");
        Chart.register(() -> getVisionWaypoint().getY(), "Drive/visionDrive/waypoint/y");
    }

    @Override
    public double getTargetSpeed() {
        if (vision == null || !vision.isConnected())
            return 0;
        TargetDetails target = vision.getTargetDetails();

        if (!target.isValid(clock.currentTime()))
            return 0;
        // We have a recent target position relative to the robot starting position.
        Pose2d robotPose = location.getCurrentPose();
        double distanceToTarget = MathUtil.distanceBetween(robotPose, target.pose);
        double distanceBeforeGoal = Config.drivebase.routine.visionDrive.distanceBeforeGoal;
        double speed = Config.drivebase.routine.visionDrive.speedScale
                * Math.max(0, distanceToTarget - distanceBeforeGoal);
        // Cap speed so that the robot doesn't try to go too fast.
        return Math.min(speed, Config.drivebase.routine.visionDrive.maxSpeed);
    }

    @Override
    public double getTargetTurn() {
        if (vision == null || !vision.isConnected())
            return 0;
        TargetDetails target = vision.getTargetDetails();
        if (!target.isValid(clock.currentTime()))
            return 0;
        // We have a recent target position relative to the robot starting position.
        Pose2d robotPose = location.getCurrentPose();
        Pose2d waypoint = getVisionWaypoint();
        double angle = -MathUtil.absoluteToRelativeAngle(robotPose, waypoint).getDegrees();
        return Config.drivebase.routine.visionDrive.angleScale * angle;
    }

    public Pose2d getVisionWaypoint() {
        if (vision == null || !vision.isConnected()) {
            return new Pose2d(0, 0, new Rotation2d(0));
        }
        TargetDetails target = vision.getTargetDetails();
        Pose2d robotPose = location.getCurrentPose();
        double distanceToTarget = MathUtil.distanceBetween(robotPose, target.pose);
        if (distanceToTarget > Config.drivebase.routine.visionDrive.splineMinDistanceMetres) {
            // FIXME: The angle to add on looks wrong. It should be based on where the target is
            // pointing.
            return target.pose
                    .plus(new Transform2d(new Translation2d(
                            -robotPose.getTranslation().getDistance(target.pose.getTranslation())
                                    * Config.drivebase.routine.visionDrive.waypointDistanceScale,
                            0), new Rotation2d(0)));
        } else {
            return target.pose;
        }
    }

    @Override
    public boolean hasFinished() {
        // Stop when the target speed is close to zero which is when
        // the robot is close to where it should be or it can't see
        // a vision target.
        return Math.abs(getTargetSpeed()) < 0.1 && Math.abs(getTargetTurn()) < 0.1;
    }

}
