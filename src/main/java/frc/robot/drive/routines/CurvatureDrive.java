package frc.robot.drive.routines;



import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.lib.MathUtil;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Switch;
import org.strongback.components.ui.ContinuousRange;

/*
 * Curvature drive, or an implementation of Cheesy Drive from 2016
 */
public class CurvatureDrive extends DriveRoutine {
    private double quickStopAccumulator = 0.0;
    private ContinuousRange throttleCR;
    private ContinuousRange wheelCR;
    private Switch isQuickTurn;

    public static final double DEFAULT_MINIMUM_SPEED = 0.02;
    public static final double DEFAULT_MAXIMUM_SPEED = 1.0;
    private static final double SENSITIVITY_TURN = 1.0;

    public CurvatureDrive(ContinuousRange throttle, ControlMode mode, ContinuousRange wheel,
            Switch isQuickTurn) {
        super("Curvature", mode);
        this.throttleCR = throttle;
        this.wheelCR = wheel;
        this.isQuickTurn = isQuickTurn;
    }

    @Override
    public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
        double wheel = limit(this.wheelCR.read());
        double throttle = limit(this.throttleCR.read());

        double overPower;
        double angularPower;

        if (isQuickTurn.isTriggered()) { // do we do quick turn this time through?
            if (Math.abs(throttle) < DEFAULT_MINIMUM_SPEED) {
                double alpha = 0.1;
                quickStopAccumulator = (1 - alpha) * quickStopAccumulator
                        + alpha * MathUtil.clamp(wheel, -1.0, 1.0) * 2;
            }
            overPower = 1.0;
            angularPower = wheel;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * wheel * SENSITIVITY_TURN - quickStopAccumulator;
            if (quickStopAccumulator > 1) {
                quickStopAccumulator -= 1;
            } else if (quickStopAccumulator < -1) {
                quickStopAccumulator += 1;
            } else {
                quickStopAccumulator = 0.0;
            }
        }

        double rightPwm = throttle - angularPower;
        double leftPwm = throttle + angularPower;
        if (leftPwm > 1.0) {
            rightPwm -= overPower * (leftPwm - 1.0);
            leftPwm = 1.0;
        } else if (rightPwm > 1.0) {
            leftPwm -= overPower * (rightPwm - 1.0);
            rightPwm = 1.0;
        } else if (leftPwm < -1.0) {
            rightPwm += overPower * (-1.0 - leftPwm);
            leftPwm = -1.0;
        } else if (rightPwm < -1.0) {
            leftPwm += overPower * (-1.0 - rightPwm);
            rightPwm = -1.0;
        }
        return new DriveMotion(leftPwm, rightPwm);
    }
}
