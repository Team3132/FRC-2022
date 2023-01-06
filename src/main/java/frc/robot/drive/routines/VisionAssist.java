package frc.robot.drive.routines;



import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.Config;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Location;
import frc.robot.interfaces.Vision;
import frc.robot.interfaces.Vision.TargetDetails;
import frc.robot.lib.MathUtil;
import java.util.function.DoubleSupplier;
import org.strongback.components.Clock;

/**
 * Turn the robot to face the vision target while driving at the specified speed.
 * If it can't see the goal, it let the driver steer the robot to align the robot
 * roughly at the goal. Once the goal has been found, the robot will take back
 * turning control.
 * 
 * Differs from VisionAim as the driver can set the speed and in some situations
 * can turn. The driver can also abort at any time.
 */
public class VisionAssist extends AutoDriveBase {
    private DoubleSupplier speed, turn;
    private Vision vision;
    private Location location;
    private Clock clock;

    public VisionAssist(DoubleSupplier speed, DoubleSupplier turn, DriveTelemetry telemetry,
            Vision vision, Location location, Clock clock) {
        super("visionAssist", telemetry,
                clock);
        this.speed = speed;
        this.turn = turn;
        this.vision = vision;
        this.location = location;
        this.clock = clock;
    }

    @Override
    public double getTargetSpeed() {
        // Under auto there aren't any values from the joystick, so this will be zero.
        return speed.getAsDouble();
    }


    @Override
    public double getTargetTurn() {
        TargetDetails target = vision.getTargetDetails();
        if (!target.isValid(clock.currentTime())) {
            // Target isn't visible, use the joystick for turning.
            return turn.getAsDouble();
        }
        // We have a recent target position relative to the robot starting position.
        // Calculate the turn value.
        Pose2d robotPose = location.getCurrentPose();
        double angle = -MathUtil.absoluteToRelativeAngle(robotPose, target.pose).getDegrees();
        return Config.drivebase.routine.visionAssist.angleScale * angle;
    }

    @Override
    public boolean hasFinished() {
        // Always return true so the driver can always go back into normal arcade driving.
        return true;
    }
}
