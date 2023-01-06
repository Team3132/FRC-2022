package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.Intake;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;
import org.strongback.components.Solenoid.Position;

/**
 * Pneumatically driven arm and using one motor to intake game objects
 */
public class IntakeImpl extends Subsystem implements Intake {
    private Motor motor;
    private double targetRPS;
    private Solenoid solenoid;

    public IntakeImpl(Motor motor, Solenoid solenoid) {
        super("Intake");
        this.motor = motor;
        this.solenoid = solenoid;
        Chart.register(() -> solenoid.isExtended(), "%s/extended", name);
        Chart.register(() -> solenoid.isRetracted(), "%s/retracted", name);
        Chart.register(motor::getOutputVoltage, "%s/outputVoltage", name);
        Chart.register(motor::getOutputPercent, "%s/outputPercent", name);
        Chart.register(motor::getOutputCurrent, "%s/outputCurrent", name);
        // FIXME: getSupplyCurrent is not implemented for SPARK MAX
        Chart.register(motor::getSupplyCurrent, "%s/supplyCurrent", name);
        Chart.register(motor::getSpeed, "%s/rps", name);
        Chart.register(() -> targetRPS, "%s/targetRPS", name);

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
    public boolean isInPosition() {
        return solenoid.isInPosition();
    }

    @Override
    public void setPosition(Position position) {
        solenoid.setPosition(position);
    }

    @Override
    public boolean isExtended() {
        return solenoid.isExtended();
    }

    @Override
    public boolean isRetracted() {
        return solenoid.isRetracted();
    }

    /**
     * Set the speed on the intake wheels.
     */
    @Override
    public void setTargetRPS(double rps) {
        if (rps == targetRPS) {
            return;
        }
        targetRPS = rps;
        // Note that if velocity mode is used and the speed is ever set to 0,
        // change the control mode from percent output, to avoid putting
        // unnecessary load on the battery and motor.
        if (rps == 0) {
            debug("Turning intake wheel off.");
            motor.set(ControlMode.DutyCycle, 0);
        } else {
            motor.set(ControlMode.Speed, rps);
        }
        debug("Setting intake target speed to %f", targetRPS);
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }

    /**
     * Update the operator console with the status of the intake subsystem.
     */
    @Override
    public void updateDashboard() {
        SmartDashboard.putString("Intake position", solenoid.toString());
        SmartDashboard.putNumber("Intake motor current", motor.getOutputCurrent());
        SmartDashboard.putNumber("Intake motor target RPS", getTargetRPS());
        SmartDashboard.putNumber("Intake motor actual RPS", motor.getSpeed());
    }
}
