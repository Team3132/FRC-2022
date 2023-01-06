package frc.robot.lib;

import static frc.robot.lib.MathUtil.absoluteToRelativeAngle;
import static frc.robot.lib.MathUtil.angleToBearing;
import static frc.robot.lib.MathUtil.distanceBetween;
import static frc.robot.lib.MathUtil.getAngleDiff;
import static frc.robot.lib.MathUtil.normalise;
import static frc.robot.lib.MathUtil.normaliseBearing;
import static frc.robot.lib.MathUtil.relativeToAbsolute;
import static frc.robot.lib.MathUtil.scale;
import static frc.robot.lib.MathUtil.scaleUnclamped;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import org.junit.jupiter.api.Test;

public class TestMathUtil {
    @Test
    public void testScale() {
        assertEquals(17.5, scale(0.75, 0, 1, 10, 20));
        assertEquals(12.5, scale(0.75, 0, 1, 20, 10));
        assertEquals(12.5, scale(0.75, 1, 0, 10, 20));
        assertEquals(17.5, scale(0.75, 1, 0, 20, 10));
        assertEquals(10, scale(-1, 0, 1, 10, 20));
        assertEquals(20, scale(1.1, 0, 1, 10, 20));
    }

    @Test
    public void testScaleUnclamped() {
        assertEquals(17.5, scaleUnclamped(0.75, 0, 1, 10, 20));
        assertEquals(12.5, scaleUnclamped(0.75, 0, 1, 20, 10));
        assertEquals(12.5, scaleUnclamped(0.75, 1, 0, 10, 20));
        assertEquals(17.5, scaleUnclamped(0.75, 1, 0, 20, 10));
        assertEquals(0, scaleUnclamped(-1, 0, 1, 10, 20));
        assertEquals(30, scaleUnclamped(2, 0, 1, 10, 20));
    }

    @Test
    public void testGetAngleDiff() {
        assertEquals(getAngleDiff(0, 0), 0.0, 0.1);
        assertEquals(getAngleDiff(5, 0), 5.0, 0.1);
        assertEquals(getAngleDiff(0, 5), -5.0, 0.1);
        assertEquals(getAngleDiff(-5, 0), -5.0, 0.1);
        assertEquals(getAngleDiff(0, -5), 5.0, 0.1);
        // Weird angles that aren't in the range of -180...180
        assertEquals(getAngleDiff(361, 361), 0.0, 0.1);
        assertEquals(getAngleDiff(1, 361), 0.0, 0.1);
        assertEquals(getAngleDiff(-1, -361), 0.0, 0.1);
        assertEquals(getAngleDiff(361, -361), 2.0, 0.1);
        // Check the shortest distane calculation
        assertEquals(getAngleDiff(-175, 175), 10.0, 0.1);
        assertEquals(getAngleDiff(175, -175), -10.0, 0.1);
        // Directly opposite angles.
        assertEquals(getAngleDiff(270, 90), 180.0, 0.1);
        assertEquals(getAngleDiff(-90, 90), 180.0, 0.1);
        assertEquals(getAngleDiff(-270, -90), 180.0, 0.1);
    }

    @Test
    public void testNormalise() {
        assertEquals(normalise(10000, 10), 0, 0.1);
        assertEquals(normalise(15, 10), 5, 0.1);
        assertEquals(normalise(14.9, 10), 4.9, 0.1);
        assertEquals(normalise(10, 10), 0, 0.1);
        assertEquals(normalise(5, 10), 5, 0.1);
        assertEquals(normalise(0, 10), 0, 0.1);
        assertEquals(normalise(-5, 10), 5, 0.1);
        assertEquals(normalise(-10, 10), 0, 0.1);
        assertEquals(normalise(-14.9, 10), -4.9, 0.1);
        assertEquals(normalise(-15, 10), 5, 0.1);
        assertEquals(normalise(-10000, 10), 0, 0.1);
    }

    @Test
    public void testAngleToBearing() {
        assertEquals(angleToBearing(-180), 270, 0.1);
        assertEquals(angleToBearing(-90), 180, 0.1);
        assertEquals(angleToBearing(0), 90, 0.1);
        assertEquals(angleToBearing(90), 0, 0.1);
        assertEquals(angleToBearing(180), 270, 0.1);
        assertEquals(angleToBearing(360), 90, 0.1);
        assertEquals(angleToBearing(3600), 90, 0.1);
        assertEquals(angleToBearing(36000), 90, 0.1);
    }

    @Test
    public void testNormaliseBearing() {
        assertEquals(normaliseBearing(-360), 0, 0.1);
        assertEquals(normaliseBearing(-180), 180, 0.1);
        assertEquals(normaliseBearing(-10), 350, 0.1);
        assertEquals(normaliseBearing(0), 0, 0.1);
        assertEquals(normaliseBearing(10), 10, 0.1);
        assertEquals(normaliseBearing(90), 90, 0.1);
        assertEquals(normaliseBearing(180), 180, 0.1);
        assertEquals(normaliseBearing(350), 350, 0.1);
        assertEquals(normaliseBearing(360), 0, 0.1);
        assertEquals(normaliseBearing(3600), 0, 0.1);
    }

    /**
     * Tests that relativeToAbsolute(), absoluteToRelativeAngle() and distanceBetween()
     * correctly convert between relative and absolute poses.
     * 
     * These tests are as if `from` is the robot and `to` is a vision target.
     */
    @Test
    public void testRelativeToAbsolute() {
        /*
         * Basic test where the robot is at (x=0,y=0,a=0) and the vision
         * target can be seen at an angle and distance "d" along the x-axis.
         * 
         * @formatter:off
         * 
         *  y+ ^
         *     |
         *     |
         *     |
         *     |
         *    (R>) ------- (<T) ---->
         *                         x+
         * @formatter:on
         * 
         * Note: For angles 0 degrees is along the positive x-axis and gets more positive in
         * the anticlockwise direction.
         * 
         * This is one of the most simple possible tests where most of the values are zero.
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(0, 0), new Rotation2d(0));
            double distance = 1;
            Rotation2d relativeAngle = Rotation2d.fromDegrees(0);
            Rotation2d skew = Rotation2d.fromDegrees(0);
            Pose2d expected =
                    new Pose2d(new Translation2d(distance, 0), Rotation2d.fromDegrees(180));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }

        /*
         * Move the target around the robot by 45 degrees while keeping the target
         * pointing at the robot.
         * 
         * @formatter:off
         * 
         *  y+ ^
         *     |       (T)   
         *     |       /
         *     |
         *     |
         *    (R>) ----------------->
         *                         x+
         * @formatter:on
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(0, 0), new Rotation2d(0));
            double distance = Math.sqrt(2);
            Rotation2d relativeAngle = Rotation2d.fromDegrees(45);
            Rotation2d skew = Rotation2d.fromDegrees(0);
            Pose2d expected = new Pose2d(new Translation2d(1, 1), Rotation2d.fromDegrees(180 + 45));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }

        /*
         * Move the robot along the x-axis so it is facing straight up into the target.
         * 
         * @formatter:off
         * 
         *  y+ ^
         *     |        (T)   
         *     |         |
         *     |
         *     |         ^
         *     +--------(R)--------->
         *                         x+
         * @formatter:on
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(1, 0), Rotation2d.fromDegrees(90));
            double distance = 1;
            Rotation2d relativeAngle = Rotation2d.fromDegrees(0);
            Rotation2d skew = Rotation2d.fromDegrees(0);
            Pose2d expected = new Pose2d(new Translation2d(1, 1), Rotation2d.fromDegrees(270));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }

        /*
         * Move the target to the y-axis pointing along the a-axis.
         * 
         * @formatter:off
         * 
         *  y+ ^
         *    (T>)   
         *     |
         *     |
         *     |         ^
         *     +--------(R)--------->
         *                         x+
         * @formatter:on
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(1, 0), Rotation2d.fromDegrees(90));
            double distance = Math.sqrt(2);
            Rotation2d relativeAngle = Rotation2d.fromDegrees(45);
            Rotation2d skew = Rotation2d.fromDegrees(45);
            Pose2d expected = new Pose2d(new Translation2d(0, 1), Rotation2d.fromDegrees(0));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }

        /*
         * Move the robot down off the x-axis and the target down by the same amount to
         * (0,0).
         * 
         * @formatter:off
         * 
         *  y+ ^
         *     | 
         *     |
         *     |
         *     |         
         *    (T>)------------------>
         *     |                   x+
         *     |
         *     |         ^
         *     |        (R)   
         * @formatter:on
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(1, -1), Rotation2d.fromDegrees(90));
            double distance = Math.sqrt(2);
            Rotation2d relativeAngle = Rotation2d.fromDegrees(45);
            Rotation2d skew = Rotation2d.fromDegrees(45);
            Pose2d expected = new Pose2d(new Translation2d(0, 0), Rotation2d.fromDegrees(0));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }

        /*
         * Move the robot into the bottom left quadrant
         * 
         * @formatter:off
         * 
         *                       y+ ^
         *                          | 
         *                          |
         *                          |
         *                          |         
         *    <---------------------+
         *    -x                    |
         *                   -(R)   |
         *                          |
         *               |          |
         *              (T)         |
         *                          |
         * @formatter:on
         */
        {
            Pose2d robotPose = new Pose2d(new Translation2d(-1, -1), Rotation2d.fromDegrees(180));
            double distance = Math.sqrt(2);
            Rotation2d relativeAngle = Rotation2d.fromDegrees(45);
            Rotation2d skew = Rotation2d.fromDegrees(45);
            Pose2d expected = new Pose2d(new Translation2d(-2, -2), Rotation2d.fromDegrees(90));
            Pose2d actual = relativeToAbsolute(robotPose, relativeAngle, distance, skew);
            assertEquals(expected, actual);
            // Check back in the other direction.
            assertEquals(relativeAngle, absoluteToRelativeAngle(robotPose, expected));
            assertEquals(distance, distanceBetween(robotPose, expected), 0.01);
        }
    }
}
