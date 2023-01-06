package frc.robot.lib;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Config;

public class PoseHelper {

    /**
     * Create a new pose
     * 
     * @param x the x value of the pose in meters
     * @param y the y value of the pose in meters
     * @param degrees the angle of the pose in degrees
     * 
     * @return a pose2d with the corresponding (x,y,a)
     */
    public static Pose2d createPose2d(double x, double y, double degrees) {
        return new Pose2d(x, y, Rotation2d.fromDegrees(degrees));
    }

    /**
     * Stops at a distance as we approach a pose
     * 
     * @param pose the pose to approach
     * @param dist distance away from pose
     * @param bearing the bearing we approach pose from
     */
    public static Pose2d approachPose(Pose2d pose, double dist, double bearing) {
        double x = dist * MathUtil.cos(bearing);
        double y = dist * MathUtil.sin(bearing);

        return createPose2d(pose.getTranslation().getX() + x, pose.getTranslation().getY() + y,
                bearing);
    }

    /**
     * Stops half robot length away from balls to intake
     * 
     * @param pose the pose of the target ball
     * @param bearing the bearing we approach pose from
     */
    public static Pose2d intakeAt(Pose2d pose, double bearing) {
        return approachPose(pose, Config.robot.halfRobotLength, bearing);
    }
}
