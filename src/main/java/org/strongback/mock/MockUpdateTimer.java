package org.strongback.mock;



import org.strongback.Strongback;
import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Clock;

/*
 * Helper class that provides a difference timer.
 * On each call it returns the amount of time between the last call.
 */
@ThreadSafe
public class MockUpdateTimer {
    private volatile double updateTime;
    private final Clock clock = Strongback.timeSystem();


    public MockUpdateTimer init() {
        updateTime = clock.currentTime();
        return this;
    }

    public double diff() {
        double now = clock.currentTime();
        double diff = now - updateTime;
        updateTime = now;
        return diff;
    }
}
