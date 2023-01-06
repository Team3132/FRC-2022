package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.Conveyor;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

public class ConveyorImpl extends Subsystem implements Conveyor {
    private final Motor motor;

    public ConveyorImpl(Motor motor) {
        super("Conveyor");
        this.motor = motor;

        Chart.register(() -> motor.getOutputCurrent(), "%s/outputCurrent", name);
        Chart.register(() -> motor.getOutputPercent(), "%s/outputPercent", name);
    }

    @Override
    public void setDutyCycle(double dutyCycle) {
        debug("Setting conveyor motor duty cycle to: %f", dutyCycle);
        motor.set(ControlMode.DutyCycle, dutyCycle);
    }

    @Override
    public double getDutyCycle() {
        return motor.getOutputPercent();
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber("Conveyor percent output",
                motor.getOutputPercent());
    }

    @Override
    public void enable() {
        motor.set(ControlMode.DutyCycle, 0);
    }

    @Override
    public void disable() {
        motor.set(ControlMode.DutyCycle, 0);
    }
}
