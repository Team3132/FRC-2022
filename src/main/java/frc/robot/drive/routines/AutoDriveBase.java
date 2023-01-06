package frc.robot.drive.routines;



import frc.robot.drive.util.PositionPID;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.lib.LowPassFilter;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import java.util.function.DoubleSupplier;
import org.strongback.components.Clock;
import org.strongback.components.Motor.ControlMode;

/**
 * Base class for the special driving classes like VisionAim and PIDDrive.
 * 
 * Takes a drive speed and a turn speed and attempts to enforce that
 * they keep to those speed.
 * 
 * Useful for when the robot is hard to turn (high traction wheels etc) and it
 * needs a bit more power to keep up.
 * 
 * All values in metric.
 */
public abstract class AutoDriveBase extends DriveRoutine {
    private PositionPID leftPID, rightPID;

    public AutoDriveBase(String name, DriveTelemetry telemetry, Clock clock) {
        super(name, ControlMode.DutyCycle);
        Log.info("Drivebase", "Starting to drive positional PID");

        /*
         * There is an issue here. If the targetSpeed increases too rapidly, then even
         * with the amount of turn subtracted off, it may still exceed the maximum jerk,
         * making the drivebase go straight. It could somehow add on the turn factor
         * after capping the change in acceleration, but that is getting ugly. Instead,
         * apply a low pass filter to the targetSpeed so that it can't change too
         * quickly and then set a high jerk so the jerk doesn't limit the acceleration.
         */
        LowPassFilter filteredSpeed = new LowPassFilter(this::getTargetSpeed, 0.2);
        leftPID = createPID("Drive/" + name + "/left", () -> {
            return filteredSpeed.getAsDouble() + getTargetTurn();
        }, telemetry::getLeftDistance,
                telemetry::getLeftSpeed, clock);
        rightPID = createPID("Drive/" + name + "/right", () -> {
            return filteredSpeed.getAsDouble() - getTargetTurn();
        }, telemetry::getRightDistance, telemetry::getRightSpeed, clock);
        Chart.register(this::getTargetSpeed, "Drive/" + name + "/target/speed");
        Chart.register(this::getTargetTurn, "Drive/" + name + "/target/turn");
    }

    /**
     * This is the target forward speed that the robot should be trying
     * to achieve.
     * 
     * @return speed in metres per second.
     */
    public abstract double getTargetSpeed();

    /**
     * This is the rate of turn. It is added to the left side and subtracted from the right side.
     * How fast the robot will turn depends on the distance between the wheels.
     * 
     * @return delta wheel speed in metres per second.
     */
    public abstract double getTargetTurn();

    /**
     * Should return true when the drivebase should stop running
     */
    @Override
    public abstract boolean hasFinished();

    private PositionPID createPID(String name, DoubleSupplier targetSpeed,
            DoubleSupplier distance,
            DoubleSupplier speed, Clock clock) {
        PositionPID pid = new PositionPID(name, targetSpeed, distance, speed, clock);
        double kV = 0.3;
        double kA = 0, kI = 0, kD = 0;
        double kP = 0.64;
        pid.setVAPID(kV, kA, kP, kI, kD);
        return pid;
    }

    @Override
    public void reset(DriveRoutineParameters parameters) {
        leftPID.reset();
        rightPID.reset();
    }

    @Override
    public void enable() {
        // Don't do anything on enable, wait for the next reset().
    }

    @Override
    public void disable() {
        leftPID.disable();
        rightPID.disable();
    }

    @Override
    public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
        // Calculate the new speeds for both left and right motors.
        double leftPower = leftPID.getMotorPower();
        double rightPower = rightPID.getMotorPower();
        Log.debug("Drivebase", "%s: left=%f right=%f", name, leftPower, rightPower);
        return new DriveMotion(leftPower, rightPower);
    }
}
