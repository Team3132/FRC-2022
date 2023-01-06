package frc.robot.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Config;
import org.junit.jupiter.api.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;

/**
 * Test the LocationHistory class - a work in progress.
 * 
 * To run just this test, use:
 * ./gradlew test --tests "frc.robot.lib.TestLocationHistory"
 */
public class TestLocationHistory {

    // Check that a pose matches expected values.
    public void assertPosition(double x, double y, double h, double timeSec, Pose2d actual) {
        // System.out.printf("Checking expected position(%s)\n", expected);
        // System.out.printf(" against actual position(%s)\n", actual);
        assertEquals(x, actual.getX(), 0.01);
        assertEquals(y, actual.getY(), 0.01);
        assertEquals(h, actual.getRotation().getDegrees(), 0.01);
        // Ignore the speed for now.
    }

    @Test
    public void testHistory() {
        MockClock clock = Mock.clock();
        LocationHistory history = new LocationHistory(clock);

        for (int i = 0; i < Config.location.history.memorySecs
                * Config.location.history.cycleSpeedHz; i++) {
            Pose2d p = new Pose2d(20 * i, i, new Rotation2d(0));
            history.addLocation(p, clock.currentTime());
            assertPosition(20 * i, i, 0, clock.currentTime(),
                    history.getLocation(clock.currentTime()));
            clock.incrementByMilliseconds(20);
        }
    }
}
