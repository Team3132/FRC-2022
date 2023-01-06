package frc.robot.lib;

/**
 * 1D motion/speed/position simulator/calculator.
 * 
 * Used to simulate the motion of the lift/intake/outtake in one dimension.
 * It moves ~perfectly respecting the maximum acceleration and velocity.
 * 
 * Calculation is unit agnostic.
 */
public class MovementSimulator {
    private String name;
    private double pos = 0; // inches
    private double speed = 0; // inches/second
    private double targetPos = 0; // inches
    private final double maxSpeed; // inches/second
    private final double maxAccel; // inches/second/second
    private final double tolerance;
    private double minPos; // Smallest possible position. Used to cap target.
    private double maxPos; // Smallest possible position. Used to cap target.

    public MovementSimulator(String name, double maxSpeed, double maxAccel) {
        this(name, maxSpeed, maxAccel, Double.MIN_VALUE, Double.MAX_VALUE, 1);
    }

    public MovementSimulator(String name, double maxSpeed, double maxAccel, double minPos,
            double maxPos, double tolerance) {
        assert (maxAccel >= 0);
        assert (maxSpeed >= 0);
        assert (minPos < maxPos);
        assert (tolerance > 0);
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.maxAccel = maxAccel;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.tolerance = tolerance;
    }

    /**
     * Based on distance to targetX, calculate the acceleration, speed and
     * position.
     * 
     * Moves in 1ms increments so the errors are low due to not calculating
     * the ideal speed at any other place than the current location.
     * 
     * @return position
     */
    public double step(double dt) {
        final double kMaxIncrement = 0.001; // 1 ms
        while (dt > 0) {
            final double increment = Math.min(dt, kMaxIncrement);
            dt -= increment;
            if (doubleEquals(pos, targetPos, tolerance)) {
                // At target.
                speed = 0;
                pos = targetPos;
                // System.out.printf("Setting pos = %f\n", pos);
                return pos;
            }
            double idealSpeed = calculateIdealSpeedAtPos(pos, targetPos, maxSpeed, maxAccel);
            double newSpeed = calculateSpeed(increment, speed, idealSpeed, maxAccel);
            pos += increment * (speed + newSpeed) / 2;
            pos = Math.min(pos, maxPos);
            pos = Math.max(pos, minPos);
            speed = newSpeed;
        }
        // System.out.printf("step %.3f/%.3f speed = %.2f\n", pos, targetPos, speed);
        return pos;
    }

    /**
     * Calculates a new speed based on a ideal speed, the current speed and the
     * maximum acceleration. Need to have small dt (<=10ms) to keep error low.
     * 
     * @param dt time from now
     * @param currSpeed the current speed
     * @param idealSpeed ideal speed at now (not in dt)
     * @param maxAccel maximum acceleration allowed.
     * @return new speed
     */
    public static double calculateSpeed(double dt, double currSpeed, double idealSpeed,
            double maxAccel) {
        // Try to get to idealSpeed.
        double maxChangeInSpeed = dt * maxAccel; // Both +ve and -ve values.
        if (currSpeed + maxChangeInSpeed < idealSpeed) {
            // Even at max acceleration, can't achieve ideal speed, then
            // increase speed by the max acceleration.
            return currSpeed + maxChangeInSpeed;
        }
        if (currSpeed - maxChangeInSpeed > idealSpeed) {
            // More than the max allowed change, return the max allowed change.
            // Likely that we will overshoot the target.
            return currSpeed - maxChangeInSpeed;
        }
        // The ideal speed is within the max allowed change, use the
        // ideal speed.
        return idealSpeed;
    }

    /**
	 * Calculate trapezial speed so that it slows down as quickly as possible
	 * at the last minute at max deacceleration and respects max speed.
	 * 
	 * @formatter:on
	 * 
	 * Speed
	 * ^
	 * |---------------  max speed.
	 * |               \                  
	 * |                \                  
	 * |                 \ Slope of max deacceleration.             
	 * |                  \                  
	 * +-------------------X--> Position
	 * 
	 * @formatter:off
	 * 
	 * @param x current position.
	 * @param targetX the target position.
	 * @return ideal speed at position x
	 */
	 public static double calculateIdealSpeedAtPos(double x, double targetX, double maxSpeed, double maxAccel) {
		// Remaining distance to target
		final double d = Math.abs(targetX - x);
		// If we were accelerating from the target, what speed would we be at now? 
		// d = 0.5at^2  => t = sqrt(2d/a) = total time before it has to start to slow down.
		// v = a*t
		//   = a * sqrt(2d/a)
		// Solve for max velocity at d.
		final double v = maxAccel * Math.sqrt(2 * d / maxAccel);
		return Math.min(v, maxSpeed) * Math.signum(targetX - x);
	}

	static private boolean doubleEquals(double a, double b, double tolerance) {
		return Math.abs(a - b) < tolerance;
	}
	
	/**
	 * Override the current position.
	 * Normally only for unit testing.
	 * @param pos the position to use.
	 * @return this
	 */
	public MovementSimulator setPos(double pos) {
		this.pos = pos;
		targetPos = pos;
		return this;
	}
	
	public double getPos() {
		return pos;
	}

	public boolean isInPosition() {
		//System.out.printf("isInPosition returns %s %f %f < %f\n", Math.abs(pos - targetPos) < tolerance, pos, targetPos, tolerance);
		return Math.abs(pos - targetPos) < tolerance;
	}
	/**
	 * Set the target position to move to.
	 * @param target the new target.
	 * @return this
	 */
	public MovementSimulator setTargetPos(double target) {
		this.targetPos = Math.max(Math.min(target, maxPos), minPos);
		//System.out.printf("targetPos = %f\n", target);
		return this;
	}
	
	public double getTargetPos() {
		return targetPos;
	}
	
	public double getSpeed() {
		return speed;
	}

	/**
	 * Override the current speed.
	 * Normally only for unit testing.
	 * @param speed
	 * @return this
	 */
	public MovementSimulator setSpeed(double speed) {
		this.speed = speed;
		return this;
	}
	
	public MovementSimulator setMaxPos(double value) {
		maxPos = value;
		pos = Math.min(pos, maxPos);
		return this;
	}
	
	public double getMaxPos() {
		return maxPos;
	}

	public MovementSimulator setMinPos(double value) {
		minPos = value;
		pos = Math.max(pos, minPos);
		return this;
	}
	
	public double getMinPos() {
		return minPos;
	}
	
	@Override
	public String toString() {
		return String.format("[%sSimulator pos %.2f target pos %.2f speed %.2s]", name, pos, targetPos, speed);
	}
}
