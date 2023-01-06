package frc.robot.controller;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import frc.robot.Config;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.interfaces.Drivebase.DriveRoutineType;
import frc.robot.interfaces.Jevois.CameraMode;
import frc.robot.lib.LEDColour;
import frc.robot.lib.TimeAction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.strongback.components.Solenoid.Position;

/**
 * A single step in a Sequence. This step needs to be completely applied before the Sequence can
 * move to the next step.
 * 
 * Examples:
 * - deploy the intake, which might take half a second to move.
 * - set the shooter wheel speed
 * - drive forward at half speed
 * - wait for 500 milliseconds
 */

public class State {
    // Double and Boolean are used instead of double and boolean
    // so that null can be used to indicate that the state shouldn't
    // be changed and the current state be preserved.

    // Time
    public TimeAction timeAction = null; // How we should/shouldn't delay at the end of this state

    // Message to be logged when this is executed.
    public String logString = null;

    // Location
    public Pose2d currentPose = null;

    // Intake
    public Position intakePosition = null; // Intake is either extended or retracted.
    public Double intakeRPS = null;
    public Double velcroDutyCycle = null;

    // Shooter
    public Double shooterRPS = null; // Set the shooter target speed.
    public Boolean shooterUpToSpeed = null;
    public Double hoodAngle = null;
    public Boolean hoodAtTarget = null;

    // Feeder
    public Double feederLeftDutyCycle = null;
    public Double feederRightDutyCycle = null;

    // Conveyor
    public Double conveyorDutyCycle = null; // Set the conveyor target speed.

    // Vision
    public CameraMode cameraMode = null;

    // Driving / Climbing
    public DriveRoutineParameters drive = null;

    // Climber
    public Double climberLeftDutyCycle = null;
    public Double climberRightDutyCycle = null;

    // LED strip
    public LEDColour ledColour = null;

    // Gamepad Rumble
    public Double gamepadRumbleIntensity = null;

    // Time
    /**
     * Set absolute time that the robot has to wait until.
     * Use this or setDelayDelta(), not both.
     * 
     * @param time measured in seconds, eg time_t.
     */
    public State setDelayUntilTime(double time) {
        timeAction = new TimeAction(TimeAction.Type.DELAY_UNTIL, time);
        return this;
    }

    /**
     * Wait for delta seconds.
     * Use this or setDelayUntilTime(), not both.
     * 
     * @param seconds to apply to the current time.
     */
    public State setDelayDelta(double seconds) {
        timeAction = new TimeAction(TimeAction.Type.DELAY_DELTA, seconds);
        return this;
    }

    /**
     * Set Status Message
     * 
     * @param Status to get as a string.
     */
    public State setLog(String message) {
        logString = message;
        return this;
    }

    // Location
    public State setCurrentPostion(Pose2d pose) {
        currentPose = pose;
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    // Intake
    public State deployIntake() {
        intakePosition = Position.EXTENDED;
        domains.add(Domain.INTAKE);
        return this;
    }

    public State stowIntake() {
        intakePosition = Position.RETRACTED;
        domains.add(Domain.INTAKE);
        return this;
    }

    public State setIntakeRPS(double rps) {
        intakeRPS = Double.valueOf(rps);
        domains.add(Domain.INTAKE);
        return this;
    }

    public State setVelcroDutyCycle(double dutyCycle) {
        velcroDutyCycle = Double.valueOf(dutyCycle);
        domains.add(Domain.INTAKE);
        return this;
    }

    public State setShooterRPS(double rps) {
        shooterRPS = Double.valueOf(rps);
        domains.add(Domain.SHOOTER);
        return this;
    }

    public State setHoodAngle(double degrees) {
        hoodAngle = Double.valueOf(degrees);
        domains.add(Domain.SHOOTER);
        return this;
    }

    public State waitForHood() {
        hoodAtTarget = true;
        domains.add(Domain.SHOOTER);
        return this;
    }

    public State waitForShooter() {
        shooterUpToSpeed = true;
        domains.add(Domain.SHOOTER);
        return this;
    }

    public State setGamepadRumbleIntensity(double value) {
        gamepadRumbleIntensity = Double.valueOf(value);
        return this;
    }

    public State setFeederDutyCycle(double dutyCycle, boolean left) {
        if (left) {
            feederLeftDutyCycle = Double.valueOf(dutyCycle);
        } else {
            feederRightDutyCycle = Double.valueOf(dutyCycle);
        }
        domains.add(Domain.FEEDER);
        return this;
    }

    // Conveyor
    public State setConveyorDutyCycle(double dutyCycle) {
        conveyorDutyCycle = Double.valueOf(dutyCycle);
        domains.add(Domain.CONVEYOR);
        return this;
    }

    // Vision
    public State doCameraWebcam() {
        this.cameraMode = CameraMode.WEBCAM;
        return this;
    }

    public State doCameraVision() {
        this.cameraMode = CameraMode.VISION;
        return this;
    }

    // Climber
    public State setClimberDutyCycle(double dutyCycle, boolean left) {
        if (left) {
            climberLeftDutyCycle = Double.valueOf(dutyCycle);
        } else {
            climberRightDutyCycle = Double.valueOf(dutyCycle);
        }
        domains.add(Domain.CLIMBER);
        return this;
    }

    // LED strip

    public State setColour(LEDColour c) {
        ledColour = c;
        domains.add(Domain.LED);
        return this;
    }

    // Drive base
    /**
     * Set the power levels on the drive base.
     * Used to drive the robot forward or backwards in a
     * "straight" line for the climb.
     */
    public State setDrivebasePower(double power) {
        drive = DriveRoutineParameters.getConstantPower(power);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Set the speed on the drive base.
     * Used to drive the robot forward or backwards in a
     * "straight" line for the L3 climb.
     */
    public State setDrivebaseSpeed(double speed) {
        drive = DriveRoutineParameters.getConstantSpeed(speed);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    public State doDefaultDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.get(Config.drivebase.driveRoutine));
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Put the drive base in arcade drive mode using velocity control for the driver.
     */
    public State doArcadeVelocityDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.ARCADE_VELOCITY);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Put the drive base in arcade drive mode for the driver.
     */
    public State doArcadeDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Put the drive base in arcade climb mode for the driver.
     */
    public State doArcadeClimb() {
        drive = new DriveRoutineParameters(DriveRoutineType.ARCADE_CLIMB);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Put the drive base in DDR Pad drive mode for the driver.
     */
    public State doDDRDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.DDRPAD_DRIVE);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Put the drive base in cheesy drive mode for the driver.
     */
    public State doCheesyDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.CHEESY);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    public State doPositionPIDArcade() {
        drive = new DriveRoutineParameters(DriveRoutineType.POSITION_PID_ARCADE);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    public State doTurnToHeading(double heading) {
        drive = new DriveRoutineParameters(DriveRoutineType.TURN_TO_BEARING);
        drive.value = heading;
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Using the camera, drive to a specified distance in front of the goal.
     */
    public State doVisionDrive() {
        drive = new DriveRoutineParameters(DriveRoutineType.VISION_DRIVE);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Automatically turn to the vision target if one can be seen.
     */
    public State doVisionAim() {
        drive = new DriveRoutineParameters(DriveRoutineType.VISION_AIM);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Help the driver drive towards the vision target by taking over the steering.
     */
    public State doVisionAssist() {
        drive = new DriveRoutineParameters(DriveRoutineType.VISION_ASSIST);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    public State driveRelativeWaypoints(String filename) throws IOException {
        logString = String.format("Running path: %s", filename);
        drive = DriveRoutineParameters.getDriveWaypoints(filename);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Add waypoints for the drive base to drive through.
     * Note: The robot will come to a complete halt after each list
     * of Waypoints, so each State will cause the robot to drive and then
     * halt ready for the next state. This should be improved.
     * Waypoints are relative to the robots position.
     * 
     * @param start the assumed starting point and angle.
     * @param waypoints list of Waypoints to drive through.
     * @param end the end point and angle.
     * @param forward drive forward through waypoints.
     */
    public State driveRelativeWaypoints(Pose2d start, List<Translation2d> interiorWaypoints,
            Pose2d end,
            boolean forward) {
        drive = DriveRoutineParameters.getDriveWaypoints(start, interiorWaypoints, end, forward,
                true);
        domains.add(Domain.DRIVEBASE);
        return this;
    }

    /**
     * Auto fill the endState to be applied when a sequence is interrupted
     */
    public void fillInterrupt(State newState) {
        intakePosition = fillParam(intakePosition, newState.intakePosition);
        intakeRPS = fillParam(intakeRPS, newState.intakeRPS);
        velcroDutyCycle = fillParam(velcroDutyCycle, newState.velcroDutyCycle);
        shooterRPS = fillParam(shooterRPS, newState.shooterRPS);
        hoodAngle = fillParam(hoodAngle, newState.hoodAngle);
        feederLeftDutyCycle = fillParam(feederLeftDutyCycle, newState.feederLeftDutyCycle);
        feederRightDutyCycle = fillParam(feederRightDutyCycle, newState.feederRightDutyCycle);
        conveyorDutyCycle = fillParam(conveyorDutyCycle, newState.conveyorDutyCycle);
        climberLeftDutyCycle = fillParam(climberLeftDutyCycle, newState.climberLeftDutyCycle);
        climberRightDutyCycle = fillParam(climberRightDutyCycle, newState.climberRightDutyCycle);
        domains.addAll(newState.domains);
    }

    /**
     * Set value to newValue if newValue is non null
     */
    private static <T> T fillParam(T value, T newValue) {
        if (newValue != null)
            return newValue;
        else
            return value;
    }

    /**
     * Append the description and value for this parameter if value is non null.
     * 
     * @param name of the parameter.
     * @param value of the parameter. May be null.
     * @param result - StringBuilder to add to.
     */
    private static <T> void maybeAdd(String name, T value, ArrayList<String> result) {
        if (value == null)
            return; // Ignore this value.
        result.add(name + ":" + value);
    }

    @Override
    public String toString() {
        ArrayList<String> result = new ArrayList<String>();
        maybeAdd("logString", logString, result);
        maybeAdd("cameraMode", cameraMode, result);
        maybeAdd("climberLeftDutyCycle", climberLeftDutyCycle, result);
        maybeAdd("climberRightDutyCycle", climberRightDutyCycle, result);
        maybeAdd("drive", drive, result);
        maybeAdd("intakePosition", intakePosition, result);
        maybeAdd("intakeRPS", intakeRPS, result);
        maybeAdd("velcroDutyCycle", velcroDutyCycle, result);
        maybeAdd("conveyorDutyCycle", conveyorDutyCycle, result);
        maybeAdd("shooterRPS", shooterRPS, result);
        maybeAdd("shooterUpToSpeed", shooterUpToSpeed, result);
        maybeAdd("hoodAngle", hoodAngle, result);
        maybeAdd("hoodAtTarget", hoodAtTarget, result);
        maybeAdd("feederLeftDutyCycle", feederLeftDutyCycle, result);
        maybeAdd("feederRightDutyCycle", feederRightDutyCycle, result);
        maybeAdd("timeAction", timeAction, result);
        maybeAdd("gamepadRumbleIntensity", gamepadRumbleIntensity, result);
        return "[" + String.join(",", result) + "]";
    }

    // These are the different domains that can be controlled. Sequences
    // will abort any current running sequences if they have any overlap
    // in domains.
    private EnumSet<Domain> domains = EnumSet.noneOf(Domain.class);

    public EnumSet<Domain> getDomains() {
        return domains;
    }
}
