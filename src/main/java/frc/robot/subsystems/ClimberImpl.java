package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.Climber;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

public class ClimberImpl extends Subsystem implements Climber {
    private Motor motor;

    public ClimberImpl(Motor motor, String name) {
        super(name);
        this.motor = motor;
        Chart.register(motor::getOutputVoltage, "%s/outputVoltage", name);
        Chart.register(motor::getOutputPercent, "%s/outputPercent", name);
        Chart.register(motor::getSupplyCurrent, "%s/outputCurrent", name);
        Chart.register(() -> getDutyCycle(), "%s/dutyCycle", name);
        Chart.register(motor::getPosition, "%s/position", name);
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
        Log.debug("climber", "duty cycle %f", dutyCycle);
        motor.set(ControlMode.DutyCycle, dutyCycle);
    }

    @Override
    public double getDutyCycle() {
        return motor.get();
    }

    /**
     * Update the operator console with the status of the climber deployment subsystem.
     */
    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber("Climber motor current", motor.getOutputCurrent());
        SmartDashboard.putNumber("Climber motor duty cycle", getDutyCycle());
    }
}
