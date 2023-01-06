package frc.robot.drive.util;

import static frc.robot.interfaces.Drivebase.DriveRoutineParameters.generateTrajectory;
import static frc.robot.lib.PoseHelper.createPose2d;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import frc.robot.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests trajectory generation and caching
 * 
 * To run the tests: ./gradlew test --tests "frc.robot.drive.util.TestTrajectoryCaching"
 */
public class TestTrajectoryCaching {

    Pose2d start = createPose2d(0, 0, 0);
    List<Translation2d> interiorWaypoints = List.of();
    Pose2d end = createPose2d(1, 1, 0);
    boolean forward = true;
    boolean relative = true; // should not affect trajectories

    Path tempDir;

    /**
     * Check that cached trajectories return the same trajectory as
     * TrajectoryGenerator
     * 
     * @throws IOException
     */
    private void testTrajectory(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end,
            boolean forward,
            boolean relative) throws IOException {
        // Ensure there is no existing trajectory file
        clearPath(start, interiorWaypoints, end, forward);

        // Double check that file does not exist
        assertFalse(Files.exists(getPath(start, interiorWaypoints, end, forward)));

        // Creating trajectory file (and saving it to deploy/paths/test)
        Trajectory trajectoryA =
                generateTrajectory(start, interiorWaypoints, end, forward, relative, tempDir);

        // Double check that file has been created
        assertTrue(Files.exists(getPath(start, interiorWaypoints, end, forward)));

        // Compare created trajectory to TrajectoryGenerator's trajectory
        Trajectory expectedTrajectory = TrajectoryGenerator.generateTrajectory(start,
                interiorWaypoints, end, createConfig(forward));
        assertTrue(trajectoryA.getStates().equals(expectedTrajectory.getStates()));

        // Creating trajectory from existing file we created earlier
        Trajectory trajectoryB =
                generateTrajectory(start, interiorWaypoints, end, forward, relative, tempDir);
        // Compare trajectory to TrajectoryGenerator's trajectory
        assertTrue(trajectoryB.getStates().equals(expectedTrajectory.getStates()));
    }

    /**
     * Test that we are actually reading from a cached file by caching trajectoryA under
     * trajectoryB's file name,
     * and checking that generateTrajectory() will actually get trajectoryA from the trajectoryB
     * when given trajectoryB
     * as input
     */

    @Test
    public void testReadingFromFile() {
        // initialise some values for two test trajectories
        Pose2d testStartA = createPose2d(-31, 32, 10);
        Translation2d testTranslationA1 = new Translation2d(1, 1);
        Translation2d testTranslationA2 = new Translation2d(-2, -2);
        List<Translation2d> testInteriorWaypointsA = List.of(testTranslationA1, testTranslationA2);
        Pose2d testEndA = createPose2d(53, 31, -80);
        boolean testForwardA = true;

        Pose2d testStartB = createPose2d(-34, 18, 10);
        Translation2d testTranslationB1 = new Translation2d(21, 1);
        Translation2d testTranslationB2 = new Translation2d(-23, -2);
        List<Translation2d> testInteriorWaypointsB = List.of(testTranslationB1, testTranslationB2);
        Pose2d testEndB = createPose2d(53, 12, -50);
        boolean testForwardB = true;

        // get the path for trajectoryB
        int hashB = Arrays.deepHashCode(
                new Object[] {testStartB, testInteriorWaypointsB, testEndB, testForwardB});
        Path trajectoryPathB =
                Paths.get(tempDir.toString(), String.valueOf(hashB) + ".wpilib.json");

        // ensure the file doesn't already exist
        try {
            Files.deleteIfExists(trajectoryPathB);
        } catch (IOException e) {
            fail(e.toString());
        }

        // generate trajectoryA and save it under trajectoryB's hash
        Trajectory trajectoryA = TrajectoryGenerator.generateTrajectory(testStartA,
                testInteriorWaypointsA, testEndA, createConfig(testForwardA));
        try {
            TrajectoryUtil.toPathweaverJson(trajectoryA, trajectoryPathB);
        } catch (IOException e) {
            fail(e.toString());
        }

        // giving our generateTrajectory() method details for trajectory B, check that the
        // trajectory we get is actually trajectoryA
        // (which is saved under trajectoryB's hash)
        Trajectory trajectory = generateTrajectory(testStartB, testInteriorWaypointsB, testEndB,
                testForwardB, relative, tempDir);
        assertTrue(trajectoryA.getStates().equals(trajectory.getStates()));
    }

    @Test
    public void testInitial() throws IOException {
        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /**
     * Changing Pose2d start
     * 
     * @throws IOException
     */
    @Test
    public void testStart() throws IOException {
        start = createPose2d(-1, -1, 30);

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
     * Changing List<Translation2d> interiorWaypoints
     */
    @Test
    public void testInteriorWaypoints() throws IOException {
        Translation2d translation1 = new Translation2d(2, 2);
        Translation2d translation2 = new Translation2d(-5, -5);
        interiorWaypoints = List.of(translation1, translation2);

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
     * Changing Pose2d end
     */
    @Test
    public void testEnd() throws IOException {
        end = createPose2d(1, 1, 5);

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
     * Changing boolean forward
     */
    @Test
    public void initialTest() throws IOException {
        forward = false;

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    /*
     * Changing boolean relative
     */
    @Test
    public void testRelative() throws IOException {
        relative = false;

        testTrajectory(start, interiorWaypoints, end, forward, relative);
    }

    @BeforeEach
    public void createTempDir() {
        if (tempDir == null) {
            try {
                tempDir = Files.createTempDirectory("trajectories");
                System.out.println(tempDir.toString());

            } catch (IOException e) {
                fail(e.toString());
                return;
            }
        }
        return;
    }

    /**
     * returns path for trajectory file
     * 
     * @throws IOException
     */
    private Path getPath(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end,
            boolean forward)
            throws IOException {
        int hash = Arrays.deepHashCode(new Object[] {start, interiorWaypoints, end, forward});
        // System.out.println(hash);
        return Paths.get(tempDir.toString(), String.valueOf(hash) + ".wpilib.json");
    }

    /**
     * Removes trajectory file
     */
    private void clearPath(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end,
            boolean forward) {
        try {
            Files.deleteIfExists(getPath(start, interiorWaypoints, end, forward));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create config for trajectory
     */
    private TrajectoryConfig createConfig(boolean forward) {
        TrajectoryConfig config =
                new TrajectoryConfig(Config.drivebase.trajectory.maxSpeedMetersPerSecond,
                        Config.drivebase.trajectory.maxAccelerationMetersPerSecondSquared)
                                // Add kinematics to ensure max speed is actually obeyed
                                .setKinematics(Config.drivebase.trajectory.driveKinematics)
                                // Apply the voltage constraint
                                .addConstraint(Config.drivebase.trajectory.autoVoltageConstraint)
                                .setReversed(!forward);
        return config;
    }
}
