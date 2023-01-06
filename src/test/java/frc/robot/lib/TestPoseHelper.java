package frc.robot.lib;

import static frc.robot.lib.PoseHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Pose2d;
import org.junit.jupiter.api.Test;

/**
 * Test the PoseHelper class.
 * 
 * To run just this test, use:
 * ./gradlew test --tests "frc.robot.lib.TestPoseHelper"
 */
public class TestPoseHelper {
    Pose2d zero = createPose2d(0, 0, 0);
    Pose2d pose = createPose2d(-10, 10, 150);

    @Test
    public void testZero() {

        assertEquals(approachPose(zero, 0, 0), createPose2d(0, 0, 0));
    }

    @Test
    public void testPositiveDistances() {

        assertEquals(approachPose(zero, 2, 0), createPose2d(2, 0, 0));
        assertEquals(approachPose(pose, 19.5, 0), createPose2d(9.5, 10, 0));
    }

    @Test
    public void testNegativeDistances() {

        assertEquals(approachPose(zero, -2, 0), createPose2d(-2, 0, 0));
        assertEquals(approachPose(pose, -19.5, 0), createPose2d(-29.5, 10, 0));
    }

    @Test
    public void testAngles() {
        double dist = 5.0;

        // Testing 0, 90, 180, 270, 360
        assertEquals(approachPose(pose, dist, 0), createPose2d(-10 + dist, 10, 0));
        assertEquals(approachPose(pose, dist, 90), createPose2d(-10, 10 + dist, 90));
        assertEquals(approachPose(pose, dist, 180), createPose2d(-10 - dist, 10, 180));
        assertEquals(approachPose(pose, dist, 270), createPose2d(-10, 10 - dist, 270));
        assertEquals(approachPose(pose, dist, 360), createPose2d(-10 + dist, 10, 360));

    }

    @Test
    public void testWeirderAngles() {
        double dist = 5.0;

        assertEquals(approachPose(zero, dist, -90), createPose2d(0, -dist, -90));
        assertEquals(approachPose(zero, dist, 450), createPose2d(0, dist, 450));
        assertEquals(approachPose(zero, dist, 30),
                createPose2d(dist * Math.sqrt(3) / 2, dist / 2, 30));
        assertEquals(approachPose(pose, 0, 275), createPose2d(-10, 10, 275));
        assertEquals(approachPose(pose, -41.5, 300),
                createPose2d(-10 + -41.5 / 2, 10 - -41.5 * Math.sqrt(3) / 2, 300));
    }

    @Test
    public void testPoses() {

        Pose2d firstQuadrant = createPose2d(7, 13, 125);
        assertEquals(approachPose(firstQuadrant, -3, 45),
                createPose2d(7 - 3.0 / Math.sqrt(2), 13 - 3.0 / Math.sqrt(2), 45));

        Pose2d secondQuadrant = createPose2d(-11, 8, 339);
        assertEquals(approachPose(secondQuadrant, -3, 0), createPose2d(-11 - 3, 8, 0));

        Pose2d thirdQuadrant = createPose2d(-13, -198.4, 0);
        assertEquals(approachPose(thirdQuadrant, -3, 30),
                createPose2d(-13 - 3.0 * Math.sqrt(3) / 2, -198.4 - 3.0 / 2, 30));

        Pose2d fourthQuadrant = createPose2d(17, -2, 150);
        assertEquals(approachPose(fourthQuadrant, -3, -30),
                createPose2d(17 - 3.0 * Math.sqrt(3) / 2, -2 + 3.0 / 2, -30));

    }

}
