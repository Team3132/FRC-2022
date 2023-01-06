package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.interfaces.DriveTelemetry;
import org.junit.jupiter.api.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockDoubleSupplier;
import org.strongback.mock.MockGyroscope;

/**
 * Test the Location class.
 * 
 * To run just this test, use:
 * ./gradlew test --tests "frc.robot.subsystems.TestLocation"
 */
public class TestLocation {

    // Check that a pose matches expected values.
    public void assertPosition(double x, double y, double h, Pose2d actual) {
        Pose2d expected = new Pose2d(x, y, new Rotation2d(Math.toRadians(h)));
        System.out.printf("Checking expected location(%s)\n", expected);
        System.out.printf("   against actual location(%s)\n", actual);
        assertEquals(x, actual.getX(), 0.01);
        assertEquals(y, actual.getY(), 0.01);
        assertEquals(h, actual.getRotation().getDegrees(), 0.01);
        // Ignore the speed for now.
    }

    @Test
    public void testLocation() {
        MockDoubleSupplier leftDistance = Mock.doubleSupplier();
        MockDoubleSupplier rightDistance = Mock.doubleSupplier();
        DriveTelemetry telemetry = new DriveTelemetry() {
            @Override
            public void setLeftDistance(double pos) {
                leftDistance.setValue(pos);
            }

            @Override
            public void setRightDistance(double pos) {
                rightDistance.setValue(pos);
            }

            @Override
            public double getLeftDistance() {
                return leftDistance.getAsDouble();
            }

            @Override
            public double getRightDistance() {
                return rightDistance.getAsDouble();
            }

            @Override
            public double getLeftSpeed() {
                return 0;
            }

            @Override
            public double getRightSpeed() {
                return 0;
            }

        };
        MockGyroscope gyro = Mock.gyroscope();
        MockClock clock = Mock.clock();
        LocationImpl location = new LocationImpl(telemetry, gyro, clock);

        // Initial location should always be 0,0,0
        gyro.setAngle(0); // Facing towards the opposing alliances wall.
        clock.incrementByMilliseconds(20);
        assertPosition(0, 0, 0, location.getCurrentPose());
        location.execute(0);

        // Roll forward both encoders 0.1 metres so that the robot drives
        // straight forward towards the other end of the field.
        leftDistance.setValue(leftDistance.getValue() + 0.1);
        rightDistance.setValue(rightDistance.getValue() + 0.1);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        // Towards other end of the field is x=10, y=0
        assertPosition(0.1, 0, 0, location.getCurrentPose());

        // Roll backwards the same distance and see if we get to the same position.
        leftDistance.setValue(leftDistance.getValue() - 0.1);
        rightDistance.setValue(rightDistance.getValue() - 0.1);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 0, location.getCurrentPose());

        // Pretend to turn 180 degrees on the spot, the angle should change and nothing else.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() - 20);
        gyro.setAngle(180);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 180, location.getCurrentPose());

        // Turn again and the angle should be back to 0.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() - 20);
        gyro.setAngle(0);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 0, location.getCurrentPose());

        // Roll forward and a bit clockwise.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() + 10);
        gyro.setAngle(-45); // Clockwise is a -ve angle.
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(13.50, -5.59, -45,
                location.getCurrentPose());

        // Reset heading and the heading will be zero again, but the gryo will be 45 degrees further
        // ahead.
        location.resetHeading();
        assertPosition(13.50, -5.59, 0,
                location.getCurrentPose());
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(13.50, -5.59, 0,
                location.getCurrentPose());
    }
}
