package org.strongback.hardware;



import org.strongback.Strongback;
import org.strongback.components.Servo;

public class HardwareTimedServo implements Servo {

    private final edu.wpi.first.wpilibj.Servo servo;
    private final double min;
    private final double max;
    private final double travelPerSecond;
    private double target;
    private double endTime;

    HardwareTimedServo(edu.wpi.first.wpilibj.Servo servo, double min, double max,
            double travelPerSecond, double initial) {
        this.servo = servo;
        this.min = min;
        this.max = max;
        this.travelPerSecond = travelPerSecond;
        this.target = initial;
        this.endTime = Strongback.timeSystem().currentTime();
        servo.set(target);
    }

    public Servo setTarget(double value) {
        value = Math.min(max, Math.max(min, value)); // bounds checking.
        if (value != target) {
            double current = target; // where we currently are
            /*
             * We have finished the previous movement.
             * Otherwise we would need to wait until it has finished.
             */
            target = value;
            endTime = Strongback.timeSystem().currentTime()
                    + (Math.abs(target - current) / travelPerSecond);
            servo.set(target);
        }
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
        servo.setBounds(max, deadbandMax, center, deadbandMin, min);
    }

}
