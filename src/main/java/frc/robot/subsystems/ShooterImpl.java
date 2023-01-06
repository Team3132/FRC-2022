package frc.robot.subsystems;



import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Config;
import frc.robot.interfaces.Shooter;
import frc.robot.lib.MathUtil;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Servo;

/**
 * On the 2022 robot, there are two shooter motors.
 * One with an encoder and the rest without, that are under PID control for speed control.
 * There are multiple servos controlled in sync to control the hood.
 */
public class ShooterImpl extends Subsystem implements Shooter {

    private final Motor flywheel;
    private final Servo[] hoodServos;
    private double targetRPS = 0;

    public ShooterImpl(Motor flywheel, Servo[] hoodServos) {
        super("Shooter");
        this.flywheel = flywheel;
        this.hoodServos = hoodServos;
        Chart.register(() -> getTargetRPS(), "%s/targetSpeed", name);
        Chart.register(flywheel::getSpeed, "%s/rps", name);
        Chart.register(flywheel::getOutputVoltage, "%s/outputVoltage", name);
        Chart.register(flywheel::getOutputPercent, "%s/outputPercent", name);
        Chart.register(flywheel::getSupplyCurrent, "%s/outputCurrent", name);
        if (hoodServos.length != 0) {
            Chart.register(hoodServos[0]::getTarget, "%s/hoodTargetDegrees", name);
        }
    }

    @Override
    public void disable() {
        super.disable();
        setTargetRPS(0);
    }

    /**
     * Set the speed on the shooter wheels.
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
            debug("Turning shooter wheel off.");
            flywheel.set(ControlMode.DutyCycle, 0);
        } else {
            flywheel.set(ControlMode.Speed, rps);
        }
        debug("Setting shooter target speed to %f", targetRPS);
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }

    @Override
    public boolean isAtTargetSpeed() {
        return Math.abs(
                flywheel.getSpeed() - getTargetRPS()) < Config.shooter.speed.toleranceRPS;
    }

    @Override
    public void setHoodTargetAngle(double degrees) {
        // The servo should be fully extended when the hood is at 0 degrees and fully retracted when
        // the hood is at max (currently 25) degrees.
        double target =
                MathUtil.scale(degrees, Config.shooter.hood.minAngle, Config.shooter.hood.maxAngle,
                        Config.shooter.hood.maxExtension, Config.shooter.hood.minExtension);
        for (Servo servo : hoodServos) {
            servo.setTarget(target);
        }
    }

    @Override
    public double getHoodTargetAngle() {
        if (hoodServos.length == 0) {
            return 0;
        }
        return MathUtil.scale(hoodServos[0].getTarget(), Config.shooter.hood.maxExtension,
                Config.shooter.hood.minExtension, Config.shooter.hood.minAngle,
                Config.shooter.hood.maxAngle);
    }

    @Override
    public boolean isHoodAtTargetAngle() {
        if (hoodServos.length == 0) {
            return true;
        }
        return hoodServos[0].atTarget();
    }

    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber("Shooter target rps", getTargetRPS());
        SmartDashboard.putNumber("Shooter actual rps", flywheel.getSpeed());
        SmartDashboard.putString("Shooter status",
                isAtTargetSpeed() ? "At target" : "Not at target");
        if (hoodServos.length != 0) {
            SmartDashboard.putNumber("Shooter hood target degrees", hoodServos[0].getTarget());
        }
    }
}
