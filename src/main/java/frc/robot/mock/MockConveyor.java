package frc.robot.mock;



import frc.robot.interfaces.Conveyor;
import frc.robot.lib.Subsystem;

public class MockConveyor extends Subsystem implements Conveyor {
    private double dutyCycle = 0;

    public MockConveyor() {
        super("MockConveyor");
    }

    @Override
    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    @Override
    public double getDutyCycle() {
        return dutyCycle;
    }
}
