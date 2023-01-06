package org.strongback.mock;



import org.strongback.Strongback;
import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Servo;

/**
 * A {@link Servo} implementation for testing.
 */
@ThreadSafe
public class MockTimedServo implements Servo {
    private volatile double current;
    private final double min;
    private final double max;
    private final double travelPerSecond;
    private volatile double target;
    private volatile double endTime;

    public MockTimedServo(double min, double max, double travelPerSecond, double initial) {
        this.min = min;
        this.max = max;
        this.travelPerSecond = travelPerSecond;
        this.target = initial;
        this.endTime = Strongback.timeSystem().currentTime();
    }

    public MockTimedServo setTarget(double value) {
        value = Math.min(max, Math.max(min, value)); // bounds checking.
        target = value;
        endTime = Strongback.timeSystem().currentTime()
                + (Math.abs(target - current) / travelPerSecond);
        return this;
    }

    public double getTarget() {
        return target;
    }

    public boolean atTarget() {
        return Strongback.timeSystem().currentTime() >= endTime;
    }

    public void setBounds(double max, double deadbandMax, double center, double deadbandMin,
            double min) {
        // do nothing with these.
    }
}
