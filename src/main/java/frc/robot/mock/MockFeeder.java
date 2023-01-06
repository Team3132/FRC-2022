package frc.robot.mock;



import frc.robot.interfaces.Feeder;
import frc.robot.lib.Subsystem;

public class MockFeeder extends Subsystem implements Feeder {

    public MockFeeder(String name) {
        super(name);
    }

    private double dutyCycle = 0;

    @Override
    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    @Override
    public double getDutyCycle() {
        return this.dutyCycle;
    }

}
