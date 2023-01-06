package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.drive.routines.ConstantDrive;
import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.Drivebase;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import java.util.Map;
import java.util.TreeMap;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

/**
 * Subsystem responsible for the drivetrain
 *
 * Normally there are multiple motors per side, but these have been set to
 * follow one motor per side and these are passed to the drivebase.
 *
 * It periodically queries the joysticks (or the auto routine) for the
 * speed/power for each side of the drivebase.
 */
public class DrivebaseImpl extends Subsystem implements Drivebase {
    private DriveRoutineParameters parameters = DriveRoutineParameters.getConstantPower(0);
    private DriveRoutine routine = null;
    private ControlMode controlMode = ControlMode.DutyCycle; // The mode the talon should be in.
    private final Motor left;
    private final Motor right;
    private DriveMotion currentMotion;

    public DrivebaseImpl(Motor left, Motor right) {
        super("Drive");
        this.left = left;
        this.right = right;

        currentMotion = new DriveMotion(0, 0);
        routine = new ConstantDrive("Constant Drive", ControlMode.DutyCycle);
        disable(); // disable until we are ready to use it.
        Chart.register(() -> currentMotion.left, "%s/setpoint/Left", name);
        Chart.register(() -> currentMotion.right, "%s/setpoint/Right", name);
        Chart.register(() -> left.getPosition(), "%s/position/Left", name);
        Chart.register(() -> right.getPosition(), "%s/position/Right", name);
        Chart.register(() -> left.getSpeed(), "%s/speed/Left", name);
        Chart.register(() -> right.getSpeed(), "%s/speed/Right", name);
        Chart.register(() -> left.getOutputVoltage(), "%s/outputVoltage/Left", name);
        Chart.register(() -> right.getOutputVoltage(), "%s/outputVoltage/Right", name);
        Chart.register(() -> left.getOutputPercent(), "%s/outputPercentage/Left", name);
        Chart.register(() -> right.getOutputPercent(), "%s/outputPercentage/Right", name);
        Chart.register(() -> left.getOutputCurrent(), "%s/outputCurrent/Left", name);
        Chart.register(() -> right.getOutputCurrent(), "%s/outputCurrent/Right", name);
    }

    @Override
    public void setDriveRoutine(DriveRoutineParameters parameters) {
        if (this.parameters != null && parameters.equals(this.parameters)) {
            debug("Parameters are identical not setting these");
            return;
        }
        // Drive routine has changed.
        this.parameters = parameters; // Remember it for next time.
        // Find a routine to handle it
        DriveRoutine newRoutine = driveModes.getOrDefault(parameters.type, null);
        if (newRoutine == null) {
            error("Bad drive mode %s", parameters.type);
            return;
        }
        // Tell the drive routine to change what it is doing.
        newRoutine.reset(parameters);
        debug("Switching to %s drive routine using ControlMode %s", newRoutine.getName(),
                newRoutine.getControlMode());
        if (routine != null)
            routine.disable();
        newRoutine.enable();
        routine = newRoutine;
        controlMode = newRoutine.getControlMode();
    }

    @Override
    public DriveRoutineParameters getDriveRoutineParameters() {
        return parameters;
    }

    @Override
    synchronized public void update() {
        // Query the drive routine for the desired wheel speed/power.
        if (routine == null)
            return; // No drive routine set yet.
        // Ask for the power to supply to each side. Pass in the current wheel speeds.
        DriveMotion motion = routine.getMotion(left.getSpeed(), right.getSpeed());
        // Logger.debug("drive subsystem motion = %.1f, %.1f", motion.left,
        // motion.right);
        if (motion.equals(currentMotion)) {
            return; // No change.
        }
        // The TalonSRX doesn't have a watchdog (unlike the WPI_ version), so no need to
        // updated it often.
        currentMotion = motion; // Save it for logging.
        left.set(controlMode, motion.left);
        right.set(controlMode, motion.right);
    }

    @Override
    public void enable() {

        super.enable();

        if (routine != null)
            routine.enable();
    }

    public void disable() {
        super.disable();
        if (routine != null)
            routine.disable();
        left.set(ControlMode.DutyCycle, 0.0);
        right.set(ControlMode.DutyCycle, 0.0);
        currentMotion.left = 0;
        currentMotion.right = 0;
    }

    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber("Left drive motor duty cycle", currentMotion.left);
        SmartDashboard.putNumber("Right drive motor duty cycle", currentMotion.right);
        SmartDashboard.putNumber("Left drive pos", left.getPosition());
        SmartDashboard.putNumber("Right drive pos", right.getPosition());
        SmartDashboard.putString("Drive control", routine.getName());
    }

    /**
     * Will return false if the current drive routine wants to keep control. For
     * example, spline driving will want to keep driving until it's done.
     */
    @Override
    public boolean hasFinished() {
        return routine.hasFinished();
    }

    private Map<DriveRoutineType, DriveRoutine> driveModes =
            new TreeMap<DriveRoutineType, DriveRoutine>();

    @Override
    public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine) {
        debug("Registered %s drive routine", routine.getName());
        driveModes.put(mode, routine);
    }

    @Override
    public void setLeftDistance(double pos) {
        left.setPosition(pos);
    }

    @Override
    public void setRightDistance(double pos) {
        right.setPosition(pos);
    }

    @Override
    public double getLeftDistance() {
        return left.getPosition();
    }

    @Override
    public double getRightDistance() {
        return right.getPosition();
    }

    @Override
    public double getLeftSpeed() {
        return left.getSpeed();
    }

    @Override
    public double getRightSpeed() {
        return right.getSpeed();
    }
}
