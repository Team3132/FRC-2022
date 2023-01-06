package org.strongback.hardware;



import org.strongback.components.Servo;

public class HardwareServo implements Servo {

    private final edu.wpi.first.wpilibj.Servo servo;

    HardwareServo(edu.wpi.first.wpilibj.Servo servo) {
        this.servo = servo;
    }

    public Servo setTarget(double value) {
        servo.set(value);
        return this;
    }

    public double getTarget() {
        return servo.get();
    }

    public boolean atTarget() {
        return true;
    }

    public void setBounds(double max, double deadbandMax, double center, double deadbandMin,
            double min) {
        servo.setBounds(max, deadbandMax, center, deadbandMin, min);
    }

}
