package frc.robot.drive.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.strongback.mock.MockClock;

public class TestPositionCalc {

    /**
     * Check the speed and position calculation
     */
    @Test
    public void testBasic() {
        // assertThat(calculateSpeed(p), is(equalTo(0.0)));
        double position = 10; // Initial position is at 10".
        double speed = 1;
        double maxJerk = 1;
        double maxSpeed = 2;
        MockClock clock = new MockClock();

        PositionCalc calc = new PositionCalc(position, speed, maxSpeed, maxJerk, clock);
        long dtMSec = 1000; // A whole second.
        clock.incrementByMilliseconds(dtMSec);
        double newPos = calc.update();
        assertEquals(newPos - position, speed, 0.1);
        position = newPos;

        // Update the target speed;
        calc.setTargetSpeed(speed + 2); // = 3 m/s, noting that max speed = 2 m/s
        clock.incrementByMilliseconds(dtMSec);
        newPos = calc.update();
        double lastSpeed = speed;
        speed = Math.min(speed + maxJerk, maxSpeed);
        double avgSpeed = (lastSpeed + speed) / 2;
        // Final speed should have increased by 1 (due to the jerk),
        // but the average should be be speed + 0.5
        assertEquals(newPos - position, avgSpeed, 0.1);
        assertEquals(calc.getSpeed(), speed, 0.1);
        position = newPos;

        // Run it again to get to the target speed.
        clock.incrementByMilliseconds(dtMSec);
        newPos = calc.update();
        lastSpeed = speed;
        speed = Math.min(speed + maxJerk, maxSpeed);
        avgSpeed = (lastSpeed + speed) / 2;
        // Final speed should have increased again by 1 (due to the jerk),
        // but the average should be be speed + 0.5
        assertEquals(newPos - position, avgSpeed, 0.1);
        assertEquals(calc.getSpeed(), speed, 0.1);
        position = newPos;

        // Now it should keep going at the target speed.
        clock.incrementByMilliseconds(dtMSec);
        newPos = calc.update();
        // Final speed should have increased again by 1 (due to the jerk),
        // but the average should be be speed + 0.5
        assertEquals(newPos - position, speed, 0.1);
        assertEquals(calc.getSpeed(), speed, 0.1);
        position = newPos;

        // Now drop the target speed by one.
        calc.setTargetSpeed(speed - 1);
        clock.incrementByMilliseconds(dtMSec);
        newPos = calc.update();
        avgSpeed = speed - maxJerk / 2;
        speed -= maxJerk;
        // Final speed should have decreased by 1 (due to the jerk),
        // but the average should be be speed - 0.5
        assertEquals(newPos - position, avgSpeed, 0.1);
        assertEquals(calc.getSpeed(), speed, 0.1);
        position = newPos;

        // With the speed = current speed, it should maintain this speed.
        clock.incrementByMilliseconds(dtMSec);
        newPos = calc.update();
        assertEquals(newPos - position, speed, 0.1);
        assertEquals(calc.getSpeed(), speed, 0.1);
        position = newPos;
    }
}
