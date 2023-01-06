package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.Feeder;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

public class FeederImpl extends Subsystem implements Feeder {

    private final Motor motor;

    public FeederImpl(Motor motor, String name) {
        super(name);
        this.motor = motor;
        Chart.register(() -> getDutyCycle(), "%s/dutyCycle", name);
        Chart.register(motor::getOutputVoltage, "%s/outputVoltage", name);
        Chart.register(motor::getOutputPercent, "%s/outputPercent", name);
        Chart.register(motor::getSupplyCurrent, "%s/outputCurrent", name);
    }

    @Override
    public void enable() {
        motor.set(ControlMode.DutyCycle, 0);
    }

    @Override
    public void disable() {
        motor.set(ControlMode.DutyCycle, 0);
    }

    @Override
    public void setDutyCycle(double dutyCycle) {
        motor.set(ControlMode.DutyCycle, dutyCycle);
    }

    @Override
    public double getDutyCycle() {
        return motor.getOutputPercent();
    }

    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber(name + " duty cycle", getDutyCycle());
    }
}
