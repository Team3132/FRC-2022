package frc.robot.mock;



import frc.robot.interfaces.Climber;
import frc.robot.lib.Subsystem;

public class MockClimber extends Subsystem implements Climber {

    private double dutyCycle = 0;

    public MockClimber() {
        super("MockClimber");
    }

    @Override
    public double getDutyCycle() {
        return this.dutyCycle;
    }

    @Override
    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
    }
}
