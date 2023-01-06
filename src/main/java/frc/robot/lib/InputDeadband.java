package frc.robot.lib;



import org.strongback.components.ui.ContinuousRange;

/*
 * Class to provide a deadband to joysticks.
 * The values within the deadband are reduced to zero, otherwise they are passed through unchanged.
 */
public class InputDeadband implements ContinuousRange {
    private ContinuousRange input;
    private double deadband;

    public InputDeadband(ContinuousRange input, double deadband) {
        this.input = input;
        this.deadband = deadband;
    }

    @Override
    public double read() {
        double value = input.read();
        if (Math.abs(value) < deadband) {
            return 0;
        }
        return value;
    }
}
