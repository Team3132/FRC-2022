package org.strongback.mock;



import org.strongback.components.ui.ContinuousRange;

/*
 * Class that holds a double (in the range -1..1)
 * Used for mock input devices and other things that need a continuous range
 */
public class MockContinuousRange implements ContinuousRange {
    private double value;

    public MockContinuousRange(double initial) {
        set(initial);
    }

    public double read() {
        return value;
    }

    public MockContinuousRange set(double value) {
        this.value = Math.min(Math.max(value, -1.0), 1.0);
        return this;
    }
}
