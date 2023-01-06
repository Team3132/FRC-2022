package frc.robot.drive.routines;



import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.lib.GamepadButtonsX;
import org.strongback.Strongback;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Switch;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.DirectionalAxis;

/**
 * Curvature drive with driving on the DPad if pressed
 */
public class CheesyDpadDrive extends DriveRoutine {
    private DirectionalAxis dPad;
    private ContinuousRange throttleCR;
    private ContinuousRange wheelCR;
    private Switch isQuickTurn;
    private double lastTime;
    private static final double timeThreashold = 3; // milliseconds

    private static final double kTurnDeadband = 0.02;
    private static final double kThrottleDeadband = 0.02;
    private static final double kNegInertiaScalar = 1.0;
    private static final double kTurnSensitivity = 0.85;
    private static final double kQuickStopDeadband = 0.2;
    private static final double kQuickStopWeight = 0.1;
    private static final double kQuickStopScalar = 5.0;
    private static final double kTurnNonLinearity = 0.25;

    private double mOldTurn = 0.0;
    private double mQuickStopAccumlator = 0.0;
    private double mNegInertiaAccumlator = 0.0;

    static final double kTopHatSpeed = 0.7;

    public CheesyDpadDrive(String name, DirectionalAxis dPad, ContinuousRange throttle,
            ContinuousRange wheel,
            Switch isQuickTurn) {
        super(name, ControlMode.DutyCycle);
        this.dPad = dPad;
        this.throttleCR = throttle;
        this.wheelCR = wheel;
        this.isQuickTurn = isQuickTurn;
        this.lastTime = Strongback.timeSystem().currentTime();
    }

    @Override
    public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
        double turn = kTopHatSpeed;
        double throttle = kTopHatSpeed;
        boolean quickTurn = false;
        int dPadDir = dPad.getDirection();
        switch (dPadDir) {
            case GamepadButtonsX.DPAD_NORTH:
                turn = 0;
                throttle *= 0.5;
                break;
            case GamepadButtonsX.DPAD_NORTH_EAST:
                turn *= -0.25;
                throttle *= 0.25;
                break;
            case GamepadButtonsX.DPAD_EAST:
                turn *= -0.5;
                throttle = 0;
                break;
            case GamepadButtonsX.DPAD_SOUTH_EAST:
                turn *= 0.25;
                throttle *= -0.25;
                break;
            case GamepadButtonsX.DPAD_SOUTH:
                turn = 0;
                throttle *= -0.5;
                break;
            case GamepadButtonsX.DPAD_SOUTH_WEST:
                turn *= -0.25;
                throttle *= -0.25;
                break;
            case GamepadButtonsX.DPAD_WEST:
                turn *= 0.5;
                throttle = 0;
                break;
            case GamepadButtonsX.DPAD_NORTH_WEST:
                turn *= 0.5;
                throttle *= -0.5;
                break;
            default:
                // if the DPad is not triggered use the joystick values instead
        }

        if (dPadDir == GamepadButtonsX.DPAD_UNTRIGGERED) {
            turn = limit(this.wheelCR.read());
            throttle = limit(this.throttleCR.read());
            quickTurn = isQuickTurn.isTriggered();
        } else {
            return new DriveMotion(throttle - turn, throttle + turn);
        }

        double linearPower, angularPower;

        turn = handleDeadband(turn, kTurnDeadband);
        throttle = handleDeadband(throttle, kThrottleDeadband);

        double negInertia = turn - mOldTurn;
        mOldTurn = turn;

        final double denominator = Math.sin(Math.PI / 2.0 * kTurnNonLinearity);
        // Apply a sin function that's scaled to make it feel better.
        turn = Math.sin(Math.PI / 2.0 * kTurnNonLinearity * turn) / denominator;
        turn = Math.sin(Math.PI / 2.0 * kTurnNonLinearity * turn) / denominator;

        double negInertiaPower = negInertia * kNegInertiaScalar;
        mNegInertiaAccumlator += negInertiaPower;

        turn = turn + mNegInertiaAccumlator;
        if (mNegInertiaAccumlator > 1) {
            mNegInertiaAccumlator -= 1;
        } else if (mNegInertiaAccumlator < -1) {
            mNegInertiaAccumlator += 1;
        } else {
            mNegInertiaAccumlator = 0;
        }
        linearPower = throttle;

        angularPower = Math.abs(throttle) * turn * kTurnSensitivity - mQuickStopAccumlator;
        if (mQuickStopAccumlator > 1) {
            mQuickStopAccumlator -= 1;
        } else if (mQuickStopAccumlator < -1) {
            mQuickStopAccumlator += 1;
        } else {
            mQuickStopAccumlator = 0.0;
        }

        double overPower, leftPower, rightPower;

        // Quickturn!
        if (quickTurn) {
            if (Math.abs(linearPower) < kQuickStopDeadband) {
                double alpha = kQuickStopWeight;
                mQuickStopAccumlator =
                        (1 - alpha) * mQuickStopAccumlator + alpha * limit(turn) * kQuickStopScalar;
            }
            overPower = 1.0;
            angularPower = (turn * turn * Math.signum(turn)) * 0.8;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * turn * kTurnSensitivity - mQuickStopAccumlator;
            if (mQuickStopAccumlator > 1) {
                mQuickStopAccumlator -= 1;
            } else if (mQuickStopAccumlator < -1) {
                mQuickStopAccumlator += 1;
            } else {
                mQuickStopAccumlator = 0.0;
            }
        }

        rightPower = leftPower = linearPower;
        leftPower -= angularPower;
        rightPower += angularPower;

        if (leftPower > 1.0) {
            rightPower -= overPower * (leftPower - 1.0);
            leftPower = 1.0;
        } else if (rightPower > 1.0) {
            leftPower -= overPower * (rightPower - 1.0);
            rightPower = 1.0;
        } else if (leftPower < -1.0) {
            rightPower += overPower * (-1.0 - leftPower);
            leftPower = -1.0;
        } else if (rightPower < -1.0) {
            leftPower += overPower * (-1.0 - rightPower);
            rightPower = -1.0;
        }
        /*
         * System.out.printf("quickstop:%f, neg:%f\n", mQuickStopAccumlator,
         * mNegInertiaAccumlator);
         * System.out.printf("left:%f, right:%f, turn:%f, throttle:%f\n", leftPower,
         * rightPower, turn, throttle); System.out.println(leftPower == rightPower);
         */

        if (Strongback.timeSystem().currentTime() - lastTime > timeThreashold) {
            System.out.println(
                    "BAD!! UDPATE TIME: " + (Strongback.timeSystem().currentTime() - lastTime));
        }
        lastTime = Strongback.timeSystem().currentTime();

        return new DriveMotion(limit(leftPower), limit(rightPower));
    }

    private static double handleDeadband(double val, double deadband) {
        return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
    }
}
