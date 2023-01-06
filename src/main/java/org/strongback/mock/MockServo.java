package org.strongback.mock;



import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Servo;

/**
 * A {@link Servo} implementation for testing.
 */
@ThreadSafe
public class MockServo implements Servo {
    private volatile double current;

    public MockServo setTarget(double current) {
        this.current = current;
        return this;
    }

    public double getTarget() {
        return current;
    }

    public boolean atTarget() {
        return true;
    }

    public void setBounds(double max, double deadbandMax, double center, double deadbandMin,
            double min) {
        // do nothing with these.
    }
}
