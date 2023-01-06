package org.strongback.components;

/**
 * A Servo is a wrapper around the WPILib servo class. It is a device that we can set the desired
 * position and it will
 * move to that position.
 * 
 * A servo needs to know its minimum and maximum angle.
 * 
 * A linear servo needs to know its minimum and maximum distance.
 * 
 * We also restrict the travel to between the minimum and maximum extremes.
 */

public interface Servo {

    Servo setTarget(double position);

    double getTarget();

    boolean atTarget();

    void setBounds(double max, double deadbandMax, double center, double deadbandMin, double min);
}
