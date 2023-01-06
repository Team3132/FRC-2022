package frc.robot.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestMovementSimulator {

    @BeforeEach
    public void setUp() {}

    // Save having to set the same parameters over and over again.
    private class CalSpeedParameters {
        double dt = 1; // 1 second.
        double currSpeed = 0; // zero inches/sec.
        double idealSpeed = 0;
        double maxA = 10;
    }

    // Helper function.
    private double calculateSpeed(CalSpeedParameters p) {
        return MovementSimulator.calculateSpeed(p.dt, p.currSpeed, p.idealSpeed, p.maxA);
    }

    /**
     * Check the speed calculation
     */
    @Test
    public void calculateSpeed() {
        CalSpeedParameters p = new CalSpeedParameters();
        // All zeros, no movement.
        assertEquals(calculateSpeed(p), 0.0);

        // Set an ideal speed that can be easily obtained.
        p.idealSpeed = 5;
        assertEquals(calculateSpeed(p), 5.0, 0.1);
        // Set an ideal speed that requires max acceleration.
        p.idealSpeed = 10;
        assertEquals(calculateSpeed(p), 10.0, 0.1);
        // Set an ideal speed that requires max negative acceleration.
        p.idealSpeed = -10;
        assertEquals(calculateSpeed(p), -10.0, 0.1);
        // Set an ideal speed that requires more than max acceleration.
        p.idealSpeed = 15;
        assertEquals(calculateSpeed(p), 10.0, 0.1);
        // Set an ideal speed that requires more than max neg acceleration.
        p.idealSpeed = -15;
        assertEquals(calculateSpeed(p), -10.0, 0.1);

        // Same tests but with a non-zero current speed and see if acceleration is correctly added.
        p.currSpeed = 50;
        p.idealSpeed = 55;
        assertEquals(calculateSpeed(p), 55.0, 0.1);
        // Set an ideal speed that requires max acceleration.
        p.idealSpeed = 60;
        assertEquals(calculateSpeed(p), 60.0, 0.1);
        // Set an ideal speed that requires max negative acceleration.
        p.idealSpeed = 40;
        assertEquals(calculateSpeed(p), 40.0, 0.1);
        // Set an ideal speed that requires more than max acceleration.
        p.idealSpeed = 65;
        assertEquals(calculateSpeed(p), 60.0, 0.1);
        // Set an ideal speed that requires more than max neg acceleration.
        p.idealSpeed = 35;
        assertEquals(calculateSpeed(p), 40.0, 0.1);

        // Ideal speed is on the other side of zero to current speed.
        p.currSpeed = 15;
        p.idealSpeed = -15;
        assertEquals(calculateSpeed(p), 5.0, 0.1);
        // Keep going to see if it gets to idealSpeed.
        p.currSpeed = 5;
        assertEquals(calculateSpeed(p), -5.0, 0.1);
        p.currSpeed = -5;
        assertEquals(calculateSpeed(p), -15.0, 0.1);
        // And once there, it should stay at ideal speed.
        p.currSpeed = -15;
        assertEquals(calculateSpeed(p), -15.0, 0.1);
    }

    // Save having to set the same parameters over and over again.
    private class CalIdealSpeedParameters {
        double x = 0; // inches
        double targetX = 0; // inches
        double maxSpeed = 0; // inches/sec
        double maxAccel = 10; // inches/sec/sec
    }

    // Helper function.
    private double calculateIdealSpeed(CalIdealSpeedParameters p) {
        return MovementSimulator.calculateIdealSpeedAtPos(p.x, p.targetX, p.maxSpeed, p.maxAccel);
    }

    /**
     * Check the ideal speed calculation
     */
    @Test
    public void calculateIdealSpeedAtPos() {
        CalIdealSpeedParameters p = new CalIdealSpeedParameters();
        // All zeros, no movement.
        assertEquals(calculateIdealSpeed(p), 0.0);

        // Test max speed.
        p.maxSpeed = 20;
        p.maxAccel = 5; // Four seconds to get to max speed.
        // No movement as on target.
        assertEquals(calculateIdealSpeed(p), 0.0);
        p.targetX = 10000; // Long way out, use max speed.
        assertEquals(calculateIdealSpeed(p), p.maxSpeed, 0.1);
        p.targetX = -10000; // Long way back, use max -ve speed.
        assertEquals(calculateIdealSpeed(p), -p.maxSpeed, 0.1);

        // Real tests.
        // Target is 5 inches away, or one second at max deacceleration.
        // v = a * sqrt(2*d/a) = 5 * sqrt(2*5/5) = 5 * sqrt(2)
        p.targetX = 5;
        assertEquals(calculateIdealSpeed(p), 5.0 * Math.sqrt(2), 0.1);
        // Target is 10 inches away. d=0.5at^2 => t=sqrt(2d/a) = sqrt(2*10/5) = sqrt(4) = 2
        // v = a*t = 5 * 2 = 10
        p.targetX = 10;
        assertEquals(calculateIdealSpeed(p), 10.0, 0.1);
        // Target is still 10 inches away.
        p.x = 50;
        p.targetX = p.x + 10;
        assertEquals(calculateIdealSpeed(p), 10.0, 0.1);
        // Target is in the other direction.
        p.targetX = p.x - 10;
        assertEquals(calculateIdealSpeed(p), -10.0, 0.1);
        // Target is a long way in the other direction.
        p.targetX = p.x - 1000;
        assertEquals(calculateIdealSpeed(p), -p.maxSpeed, 0.1);
    }

    /**
     * Check the PositionCalculator for target and pos == 0
     */
    @Test
    public void positionCalculatorZero() {
        final double kMaxSpeed = 20;
        final double kMaxAccel = 5;
        MovementSimulator calc = new MovementSimulator("sim", kMaxSpeed, kMaxAccel);

        assertEquals(calc.getTargetPos(), 0.0);
        assertEquals(calc.getPos(), 0.0);
        assertEquals(calc.getSpeed(), 0.0);
    }

    /**
     * Check the PositionCalculator ramping up and hitting max speed.
     */
    @Test
    public void positionCalculatorRampUp() {
        final double kMaxSpeed = 20;
        final double kMaxAccel = 5;
        MovementSimulator calc = new MovementSimulator("sim", kMaxSpeed, kMaxAccel);

        // Set a target at 100 inches.
        calc.setTargetPos(100);
        // How long to get to the 10 inches?
        // d = 1/2at^2
        // t = sqrt(2d/a)
        // = sqrt(10/5)
        // = 2
        calc.step(1);
        // At t=1, should be at...
        // d = 1/2at^2
        // = 1/2*5*1
        // = 2.5
        assertEquals(calc.getPos(), 2.5, 0.1);
        // Speed will be:
        // v = at
        // = 5*1 = 5
        assertEquals(calc.getSpeed(), 5, 0.1);
        // Add another second, should be 10 inches in.
        calc.step(1);
        // d = 1/2at^2
        // = 1/2*5*4
        // = 10
        assertEquals(calc.getPos(), 10, 0.1);
        // Speed will be:
        // v = at
        // = 5*2 = 10
        assertEquals(calc.getSpeed(), 10, 0.1);
        // Solve for max speed
        // v = at
        // t = v/a
        // = 20/5
        // = 4
        calc.step(2); // Already two seconds in.
        // d = 1/2at^2
        // = 1/2*5*4^2
        // = 40
        double pos = 40;
        assertEquals(calc.getPos(), pos, 0.1);
        // And the speed should be the max of 20.
        assertEquals(calc.getSpeed(), 20, 0.1);
        // Now increase the time by 1 seconds.
        calc.step(1); // 5 seconds in.
        // 1 seconds at 20 inches/sec gives 20 inches
        pos += 20;
        assertEquals(calc.getPos(), pos, 0.1);
        // And the speed should still be the max of 20.
        assertEquals(calc.getSpeed(), 20, 0.1);
    }

    /**
     * Check the PositionCalculator ramping down and hitting target speed.
     */
    @Test
    public void positionCalculatorRampDown() {
        final double kMaxSpeed = 20;
        final double kMaxAccel = 5;
        MovementSimulator calc = new MovementSimulator("sim", kMaxSpeed, kMaxAccel);

        double pos = 100;
        calc.setPos(pos);
        calc.setSpeed(20); // max speed.
        calc.setTargetPos(pos + 40); // Just long enough to ramp down from max speed.

        calc.step(1);
        pos += 1 * (20 + 15) / 2.0;
        assertEquals(calc.getSpeed(), 15, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (15 + 10) / 2.0;
        assertEquals(calc.getSpeed(), 10, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (10 + 5) / 2.0;
        assertEquals(calc.getSpeed(), 5, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (5 + 0) / 2.0;
        assertEquals(calc.getSpeed(), 0, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        assertEquals(calc.getPos(), calc.getTargetPos(), 0.1);
    }

    /**
     * Check the PositionCalculator changing direction / overshooting.
     */
    @Test
    public void positionCalculatorDirectionChange() {
        final double kMaxSpeed = 20;
        final double kMaxAccel = 5;
        MovementSimulator calc = new MovementSimulator("sim", kMaxSpeed, kMaxAccel);

        double pos = 100;
        calc.setPos(pos);
        calc.setSpeed(20); // max speed.
        calc.setTargetPos(pos - 40); // Behind it.
        // Should cause it to take 40 inches to slow down to a stop,
        // then back over that 40 inches to get up to max speed in the
        // other direction and another 40 inches to again come to a stop.

        calc.step(1);
        pos += 1 * (20 + 15) / 2.0;
        assertEquals(calc.getSpeed(), 15, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (15 + 10) / 2.0;
        assertEquals(calc.getSpeed(), 10, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (10 + 5) / 2.0;
        assertEquals(calc.getSpeed(), 5, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        // Come to a complete stop, now 80 inches too far along.
        calc.step(1);
        pos += 1 * (5 + 0) / 2.0;
        assertEquals(calc.getSpeed(), 0, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (0 + -5) / 2.0;
        assertEquals(calc.getSpeed(), -5, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (-5 + -10) / 2.0;
        assertEquals(calc.getSpeed(), -10, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        calc.step(1);
        pos += 1 * (-10 + -15) / 2.0;
        assertEquals(calc.getSpeed(), -15, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        // Now up to max speed in reverse.
        calc.step(1);
        pos += 1 * (-15 + -20) / 2.0;
        assertEquals(calc.getSpeed(), -20, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        // Ramp down again. Step in two second chunks.
        calc.step(2);
        pos += 2 * (-20 + -10) / 2.0;
        assertEquals(calc.getSpeed(), -10, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        // Another two seconds and should be at the target.
        calc.step(2);
        pos += 2 * (-10 + 0) / 2.0;
        assertEquals(calc.getSpeed(), 0, 0.1);
        assertEquals(calc.getPos(), pos, 0.1);

        // Check against the real target.
        assertEquals(calc.getPos(), calc.getTargetPos(), 0.1);

        // Another two seconds and should be no movement.
        calc.step(2);
        assertEquals(calc.getSpeed(), 0, 0.1);
        assertEquals(calc.getPos(), calc.getTargetPos(), 0.1);
    }
}
