/**
 * Sequences for doing most actions on the robot.
 * 
 * If you add a new sequence, add it to allSequences at the end of this file.
 */
package frc.robot.controller;

import static frc.robot.lib.PoseHelper.createPose2d;

import edu.wpi.first.math.geometry.Pose2d;
import frc.robot.Config;
import frc.robot.controller.Sequence.SequenceBuilder;
import frc.robot.lib.LEDColour;
import java.util.List;

/**
 * Control sequences for most robot operations.
 */
public class Sequences {

    /**
     * Do nothing sequence.
     */
    public static Sequence getEmptySequence() {
        if (emptySeq == null) {
            emptySeq = new SequenceBuilder("empty").build();
        }
        return emptySeq;
    }

    private static Sequence emptySeq = null;

    private static Sequence exclusiveSeq = null;

    /**
     * The first sequence run in the autonomous period.
     */
    public static Sequence getStartSequence() {
        if (startSeq == null) {
            startSeq = new SequenceBuilder("start").build();
        }
        // startbuilder.add().doArcadeVelocityDrive();
        return startSeq;
    }

    private static Sequence startSeq = null;

    /**
     * Returns the sequence to reset the robot. Used to stop ejecting etc. The lift
     * is at intake height, the intake is stowed, all motors are off.
     * 
     * @return
     */
    public static Sequence getResetSequence() {
        if (resetSeq == null) {
            SequenceBuilder builder = new SequenceBuilder("empty");
            builder.then().doDefaultDrive();
            resetSeq = builder.build();
        }
        return resetSeq;
    }

    private static Sequence resetSeq = null;

    /**
     * Drive to a point on the field, relative to the starting point.
     * 
     * @param angle the final angle (relative to the field) in degrees.
     */
    public static Sequence getDriveToWaypointSequence(double x, double y, double angle) {
        Pose2d start = new Pose2d();
        Pose2d end = createPose2d(x, y, angle);
        SequenceBuilder builder = new SequenceBuilder(String.format("drive to %s", end));
        builder.then().driveRelativeWaypoints(start, List.of(), end, true);
        driveToWaypointSeq = builder.build();
        return driveToWaypointSeq;
    }

    private static Sequence driveToWaypointSeq = null;

    public static Sequence setDrivebaseToArcade() {
        SequenceBuilder builder = new SequenceBuilder("Arcade Drive Routine");
        builder.then().doArcadeDrive();
        return builder.build();
    }

    public static Sequence setDrivebaseToDefault() {
        SequenceBuilder builder = new SequenceBuilder("Default Drive Routine");
        builder.then().doDefaultDrive();
        return builder.build();
    }

    /**
     * Extends the intake and then runs the motor to intake the cargo.
     * 
     * @return
     */

    public static Sequence startIntaking() {
        SequenceBuilder builder = new SequenceBuilder("Start intaking");
        // Wait for the intake to extend before turning motor
        builder.then().deployIntake();
        builder.then().setIntakeRPS(Config.intake.targetRPS)
                .setVelcroDutyCycle(Config.velcro.dutyCycle)
                .setConveyorDutyCycle(Config.conveyor.dutyCycle);
        return builder.build();
    }

    public static Sequence reverseIntakingAndFeeder() {
        SequenceBuilder builder = new SequenceBuilder("Reverse intaking");
        // Wait for the intake to extend before turning motor
        builder.then().deployIntake();
        // Reverse one feeder early to release balls out of sync and reduce jams
        builder.then().setConveyorDutyCycle(0).setFeederDutyCycle(-Config.feeder.dutyCycle, true);
        builder.then().setDelayDelta(0.5);
        builder.then().setIntakeRPS(-Config.intake.targetRPS)
                .setVelcroDutyCycle(-Config.velcro.dutyCycle)
                .setConveyorDutyCycle(-Config.conveyor.dutyCycle)
                .setFeederDutyCycle(-Config.feeder.dutyCycle, false);
        return builder.build();
    }

    public static Sequence ejectSingleBall(boolean ejectLeft) {
        SequenceBuilder builder =
                new SequenceBuilder("Eject " + (ejectLeft ? "left" : "right") + "side intaking");
        builder.then().setHoodAngle(Config.shooter.hood.resetAngle)
                .setShooterRPS(Config.shooter.speed.ejectRPS);
        builder.then().waitForHood().waitForShooter();
        builder.then().setConveyorDutyCycle(Config.conveyor.dutyCycle)
                .setFeederDutyCycle(Config.feeder.dutyCycle, ejectLeft);
        return builder.build();
    }

    public static Sequence stopIntaking() {
        SequenceBuilder builder = new SequenceBuilder("Stop intaking");
        builder.then().setIntakeRPS(0).setVelcroDutyCycle(0);
        builder.then().setConveyorDutyCycle(Config.conveyor.idleDutyCycle);
        builder.createInterruptState();
        return builder.build();
    }

    // This is needed after reversing the intake + feeders, and should only be used when the
    // feeders were running so as to not interfere with the shooter when the driver stops intaking
    public static Sequence stopIntakingAndFeeder() {
        SequenceBuilder builder = new SequenceBuilder("Stop intaking and feeder");
        builder.then().setIntakeRPS(0).setVelcroDutyCycle(0).setFeederDutyCycle(0, true)
                .setFeederDutyCycle(0, false);
        builder.then().setConveyorDutyCycle(Config.conveyor.idleDutyCycle);
        builder.createInterruptState();
        return builder.build();
    }

    public static Sequence raiseIntake() {
        SequenceBuilder builder = new SequenceBuilder("Raise intake");
        builder.then().stowIntake();
        return builder.build();
    }

    // Testing methods
    public static Sequence startIntakingNoConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Start Intaking only");
        builder.then().deployIntake();
        builder.then().setIntakeRPS(Config.intake.targetRPS)
                .setVelcroDutyCycle(Config.velcro.dutyCycle);
        return builder.build();
    }

    public static Sequence stopIntakingNoConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Stop intaking only");
        builder.then().setIntakeRPS(0).setVelcroDutyCycle(0);
        builder.createInterruptState();
        return builder.build();
    }

    public static Sequence reverseIntakingNoConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Reverse intaking only");
        builder.then().setIntakeRPS(-Config.intake.targetRPS)
                .setVelcroDutyCycle(-Config.velcro.dutyCycle);
        return builder.build();
    }

    public static Sequence startConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Start conveyor");
        builder.then().setConveyorDutyCycle(Config.conveyor.dutyCycle);
        return builder.build();
    }

    public static Sequence reverseConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Reverse conveyor");
        builder.then().setConveyorDutyCycle(-Config.conveyor.dutyCycle);
        return builder.build();
    }

    public static Sequence stopConveyor() {
        SequenceBuilder builder = new SequenceBuilder("Stop conveyor");
        builder.then().setConveyorDutyCycle(0);
        return builder.build();
    }

    /**
     * Spin up the shooter to get ready for a shot. To spin down use a button mapped to
     * stopShooting()
     */
    public static Sequence spinUpShooter(double angle, double speed) {
        SequenceBuilder builder = new SequenceBuilder("spinUpShooter" + speed + " " + angle);
        builder.then().setGamepadRumbleIntensity(0);
        builder.then().setHoodAngle(angle).setShooterRPS(speed);
        builder.then().waitForShooter().waitForHood();
        builder.then().setGamepadRumbleIntensity(0.75);
        return builder.build();
    }

    /**
     * Shoot the balls using whatever hood position and shooter speed is currently
     * set
     * 
     * WARNING: This sequence will never finish if the shooter speed is currently
     * set to zero It sets the LEDs to purple if this happens
     */
    public static Sequence startShooting() {
        SequenceBuilder builder = new SequenceBuilder("Start Shooting");
        // Another sequence should have set the shooter speed and hood position already
        builder.then().setConveyorDutyCycle(Config.conveyor.dutyCycle);
        builder.then().setGamepadRumbleIntensity(0);
        // Don't wait for shooter here. The hood is likely slower and we trust the operator to shoot
        // at the right time. (Rumble on the gamepad will indicate when the shooter is ready.)
        builder.then().waitForHood(); // .waitForShooter();
        builder.then().setFeederDutyCycle(Config.feeder.dutyCycle, true);
        builder.then().setDelayDelta(Config.feeder.leftRightDelay);
        builder.then().setFeederDutyCycle(Config.feeder.dutyCycle, false);

        return builder.build();
    }

    public static Sequence stopShooting() {
        SequenceBuilder builder = new SequenceBuilder("Stop shooting");
        // Turn off everything.
        builder.then().setShooterRPS(0).setFeederDutyCycle(0, true).setFeederDutyCycle(0, false)
                .setConveyorDutyCycle(Config.conveyor.idleDutyCycle)
                .setHoodAngle(Config.shooter.hood.resetAngle).setGamepadRumbleIntensity(0);
        builder.createInterruptState();
        return builder.build();
    }

    public static Sequence startDriveByVision() {
        SequenceBuilder builder = new SequenceBuilder("Start drive by vision");
        builder.then().doVisionDrive();
        return builder.build();
    }

    /**
     * Aim the robot if a vision target is visible.
     * Quits as soon as it's aimed.
     */
    public static Sequence visionAim() {
        SequenceBuilder builder = new SequenceBuilder("Vision aim");
        builder.then().doVisionAim();
        // Will move on when the vision target is visible, otherwise it will
        // give up quickly allowing the rest of the auto routine to run.
        return builder.build();
    }

    /**
     * Assist the driver by taking over the steering when a vision
     * target is visible.
     */
    public static Sequence visionAssist() {
        SequenceBuilder builder = new SequenceBuilder("Vision assist");
        builder.then().doVisionAssist();
        // Stay driving in vision assist mode.
        return builder.build();
    }

    public static Sequence constantDrivePower(double power) {
        SequenceBuilder builder = new SequenceBuilder("Constant drive power " + power);
        builder.then().setDrivebasePower(power);
        return builder.build();
    }

    /**
     * When we climb on the low bar our front wheels our hooks engage but our front wheels stop us
     * from swinging forward.
     * This sequence runs the drivebase forwards for a brief moment allowing us to start swinging
     */
    public static Sequence unstickWheelsAfterClimb() {
        SequenceBuilder builder = new SequenceBuilder("Unstick wheels after climb");
        builder.then().setDrivebasePower(0.22);
        builder.then().setDelayDelta(0.5); // Can't be too long otherwise we'll get stuck on the
                                           // back wheels
        builder.then().doDefaultDrive();
        return builder.build();
    }

    public static Sequence extendClimber(boolean left) {
        SequenceBuilder builder = new SequenceBuilder("Extend climber");
        builder.then().setClimberDutyCycle(Config.climber.dutyCycle, left);
        return builder.build();
    }

    public static Sequence retractClimber(boolean left) {
        SequenceBuilder builder = new SequenceBuilder("Retract climber");
        builder.then().setClimberDutyCycle(-Config.climber.dutyCycle, left);
        return builder.build();
    }

    public static Sequence stopClimber(boolean left) {
        SequenceBuilder builder = new SequenceBuilder("Stop climber");
        builder.then().setClimberDutyCycle(0, left);
        builder.createInterruptState();
        return builder.build();
    }

    public static Sequence setLEDColour(LEDColour c) {
        SequenceBuilder builder = new SequenceBuilder("set LEDS to " + c);
        builder.then().setColour(c);
        return builder.build();
    }

    // For testing. Needs to be at the end of the file.
    public static Sequence[] allSequences = new Sequence[] {
            getEmptySequence(), getStartSequence(), getResetSequence(),
            startIntaking(), stopIntaking(),
            reverseIntakingAndFeeder(), stopIntakingAndFeeder(),
            startIntakingNoConveyor(), stopIntakingNoConveyor(),
            startConveyor(), reverseConveyor(),
            visionAssist(), visionAim()
    };
}
