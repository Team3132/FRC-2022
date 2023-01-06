package frc.robot.drive.util;



import frc.robot.lib.MathUtil;
import org.strongback.components.Clock;

/**
 * Takes a target speed for a wheel and calculates the expected encoder
 * position had the wheel been doing the target speed.
 * 
 * Used as an input to a positional PID on both sides of the robot.
 */
public class PositionCalc {

    private double position = 0;
    private double speed = 0;
    private double accel;
    private double targetSpeed;
    private double maxSpeed;
    private double maxJerk;
    private Clock clock;
    private double lastTime;

    public PositionCalc(double initialPosition, double initialSpeed, double maxSpeed,
            double maxJerk, Clock clock) {
        this.position = initialPosition;
        this.speed = this.targetSpeed = initialSpeed;
        this.accel = 0;
        this.maxSpeed = maxSpeed;
        this.maxJerk = maxJerk;
        this.clock = clock;
        setTargetSpeed(initialSpeed); // Will clamp it to be within maxSpeed.
        lastTime = clock.currentTime();
    }

    /**
     * Calculates the new speed and position based on the current speed,
     * target speed and max jerk.
     * 
     * @return the new ideal encoder position.
     */
    public double update() {
        double lastSpeed = speed;
        double now = clock.currentTime();
        double dtSec = now - lastTime;
        lastTime = now;
        assert (dtSec > 0);
        double dv = dtSec * maxJerk;
        // Cap the acceleration so that the targetSpeed isn't overshot.
        dv = Math.min(Math.abs(targetSpeed - speed), dv);
        // Work out if the acceleration should be added to or subtracted from the speed.
        dv *= Math.signum(targetSpeed - speed);
        // Add the acceleration to the speed.
        speed += dv;
        accel = dv / dtSec; // Likely to be maxJerk unless already close to target speed.
        // Update the position based on the average speed over the last period.
        double averagespeed = (speed + lastSpeed) / 2;
        position += dtSec * averagespeed;
        // Logger.debug("calc speed = %f", speed);
        // Logger.debug("calc pos = %f", position);
        return position;
    }

    /**
     * Tell the wheel the desired speed.
     * Jerk will be used to control how quickly the speed can be updated.
     */
    public void setTargetSpeed(double speed) {
        this.targetSpeed = MathUtil.clamp(speed, -maxSpeed, maxSpeed);
        // Logger.debug("target speed = %f", targetSpeed);
    }

    public double getPosition() {
        return position;
    }

    /**
     * Calculated speed.
     * 
     * @return speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the last calculated acceleration.
     * 
     * @return acceleration
     */
    public double getAccel() {
        return accel;
    }

    public void reset(double initialPosition, double initialSpeed) {
        this.position = initialPosition;
        this.speed = this.targetSpeed = initialSpeed;
        accel = 0;
        lastTime = clock.currentTime();
    }
}
