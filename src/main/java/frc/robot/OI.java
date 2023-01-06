package frc.robot;



import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.drive.routines.*;
import frc.robot.interfaces.*;
import frc.robot.interfaces.Drivebase.DriveRoutineType;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.MathUtil;
import frc.robot.lib.log.Log;
import frc.robot.subsystems.*;
import org.strongback.Strongback;
import org.strongback.SwitchReactor;
import org.strongback.components.Clock;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid.Position;
import org.strongback.components.Switch;
import org.strongback.components.ui.*;

public class OI {

    private SwitchReactor reactor = Strongback.switchReactor();
    private Controller controller;
    private Subsystems subsystems;

    public OI(Controller controller, Subsystems subsystems) {
        this.controller = controller;
        this.subsystems = subsystems;
    }

    /**
     * Configure the driver interface.
     * 
     * @param left the drivers left joystick
     * @param right the drivers right joystick
     */
    public void configureDriverJoysticks(FlightStick left, FlightStick right) {
        // Intake
        right.trigger().onPress(run(Sequences.startIntaking()));
        right.trigger().onRelease(run(Sequences.stopIntakingNoConveyor()));

        // Vision line up until the driver releases the button.
        // Driver can specify the forwards speed.
        left.thumb().onPress(run(Sequences.visionAim()));
        left.thumb().onRelease(run(Sequences.setDrivebaseToDefault()));
        right.thumb().onPress(run(Sequences.visionAssist()));
        right.thumb().onRelease(run(Sequences.setDrivebaseToDefault()));

        left.button(3).onPress(run(Sequences.constantDrivePower(-0.22)));
        left.button(3).onRelease(run(Sequences.unstickWheelsAfterClimb()));

        // Arcade
        right.button(3).onPress(run(Sequences.setDrivebaseToDefault()));

        // Reverse intake
        right.button(4).onPress(run(Sequences.reverseIntakingAndFeeder()));
        right.button(4).onRelease(run(Sequences.stopIntakingAndFeeder()));
    }

    /**
     * Configure the operators interface.
     * 
     * @param gamepad the operators joystick
     */
    public void configureOperatorJoystick(Gamepad gamepad) {
        // Close/ender shot
        gamepad.leftTrigger().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.fenderAngle,
                Config.shooter.speed.fenderRPS)));
        // Auto line/front trench shot
        gamepad.leftBumper().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.autoAngle,
                Config.shooter.speed.autoRPS)));
        // Colourwheel/back trench shot
        gamepad.rightBumper().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.hangerAngle,
                Config.shooter.speed.hangerRPS)));

        // Acutally shoot the balls
        // YOU MUST use one of the spinUp buttons before pressing this
        gamepad.rightTrigger().whileTriggered(run(Sequences.startShooting()));
        gamepad.rightTrigger().onRelease(run(Sequences.stopShooting()));

        // Stow the intake
        gamepad.DPadAxis().north().onPress(run(Sequences.raiseIntake()));

        gamepad.DPadAxis().west().onPress(run(Sequences.ejectSingleBall(true)));
        gamepad.DPadAxis().west().onRelease(run(Sequences.stopShooting()));
        gamepad.DPadAxis().east().onPress(run(Sequences.ejectSingleBall(false)));
        gamepad.DPadAxis().east().onRelease(run(Sequences.stopShooting()));

        // Extend left climber
        gamepad.leftAxis().north().onPress(run(Sequences.extendClimber(true)));
        gamepad.leftAxis().north().onRelease(run(Sequences.stopClimber(true)));

        // Retract left climber
        gamepad.leftAxis().south().onPress(run(Sequences.retractClimber(true)));
        gamepad.leftAxis().south().onRelease(run(Sequences.stopClimber(true)));

        // Extend right climber
        gamepad.rightAxis().north().onPress(run(Sequences.extendClimber(false)));
        gamepad.rightAxis().north().onRelease(run(Sequences.stopClimber(false)));

        // Retract right climber
        gamepad.rightAxis().south().onPress(run(Sequences.retractClimber(false)));
        gamepad.rightAxis().south().onRelease(run(Sequences.stopClimber(false)));

        // Intake
        gamepad.aButton().onPress(run(Sequences.startIntaking()));
        gamepad.aButton().onRelease(run(Sequences.stopIntakingNoConveyor()));

        // Reverse intake
        gamepad.bButton().onPress(run(Sequences.reverseIntakingAndFeeder()));
        gamepad.bButton().onRelease(run(Sequences.stopIntakingAndFeeder()));

        gamepad.xButton().onPress(run(Sequences.stopConveyor()));
        gamepad.xButton().onRelease(run(Sequences.startConveyor()));

        gamepad.yButton().onPress(run(Sequences.stopConveyor()));
    }

    public void configureDDRPad(Dancepad dancepad) {
        // Toggle the intake
        dancepad.nwButton().onToggle("intake", run(Sequences.startIntaking()),
                run(Sequences.stopIntaking()));

        // Vision Automatic Line-up
        dancepad.neButton().onPress(run(Sequences.visionAim()));

        // Close/fender shot
        dancepad.selectButton().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.fenderAngle,
                Config.shooter.speed.fenderRPS)));

        // Auto line/front trench shot
        dancepad.R2Button().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.autoAngle,
                Config.shooter.speed.autoRPS)));

        // Colourwheel/back trench shot
        dancepad.R1Button().onPress(run(Sequences.spinUpShooter(Config.shooter.hood.hangerAngle,
                Config.shooter.speed.hangerRPS)));

        // Actually shoot the balls
        // YOU MUST use one of the spinUp buttons before pressing this
        dancepad.seButton().onPress(run(Sequences.startShooting()));
        dancepad.seButton().onRelease(run(Sequences.stopShooting()));
    }

    public void configureDiagBox(DiagnosticBox box) {
        // Shooter overrides.
        OverridableSubsystem<Shooter> shooterOverride = subsystems.shooterOverride;
        // Get the interface that the diag box uses.
        Shooter shooterIF = shooterOverride.getOverrideInterface();
        // Setup the switch for manual/auto/off modes.
        mapOverrideSwitch(box, DiagnosticBox.Colour.RED, shooterOverride);
        // While the shooter speed button is pressed, set the target speed. Does not
        // turn off.
        // TODO: Add feeders to the diag box.
        box.redButton(1).whileTriggered(() -> {
            shooterIF.setTargetRPS(1.5 * Config.shooter.speed.fenderRPS * box.getRedPot().read());
        });
        box.redButton(1).onRelease(() -> shooterIF.setTargetRPS(0));
        box.blueButton(1).whileTriggered(() -> {
            shooterIF.setHoodTargetAngle(MathUtil.scale(box.getBluePot().read(), -1, 1,
                    Config.shooter.hood.minAngle, Config.shooter.hood.maxAngle));
        });

        // Intake overrides.
        OverridableSubsystem<Intake> intakeOverride = subsystems.intakeOverride;
        // Get the interface that the diag box uses.
        Intake intakeIF = intakeOverride.getOverrideInterface();
        // Setup the switch for manual/auto/off modes.
        mapOverrideSwitch(box, DiagnosticBox.Colour.YELLOW, intakeOverride);
        // While the intake speed button is pressed, set the target speed. Does not turn
        // off.
        box.yellowButton(1)
                .whileTriggered(() -> intakeIF
                        .setTargetRPS(box.getYellowPot().read() * Config.intake.targetRPS));
        box.yellowButton(1).onRelease(() -> intakeIF.setTargetRPS(0));
        box.yellowButton(2).onPress(() -> intakeIF.setPosition(Position.EXTENDED));
        box.yellowButton(3).onPress(() -> intakeIF.setPosition(Position.RETRACTED));

        // Conveyor overrides.
        OverridableSubsystem<Conveyor> conveyorOverride = subsystems.conveyorOverride;
        // Get the interface that the diag box uses.
        Conveyor conveyorIF = conveyorOverride.getOverrideInterface();
        // Setup the switch for manual/auto/off modes.
        mapOverrideSwitch(box, DiagnosticBox.Colour.GREEN, conveyorOverride);
        // While the conveyor speed button is pressed, set the duty cycle. Does not turn
        // off.
        box.greenButton(1).whileTriggered(() -> conveyorIF
                .setDutyCycle(box.getGreenPot().read()));
        box.greenButton(1).onRelease(() -> conveyorIF.setDutyCycle(0));
    }

    /**
     * Registers all of the available drive routines that can be requested by the controller.
     */
    public void registerDriveRoutines(FlightStick left, FlightStick right,
            Dancepad dancepad) {
        // Convenience variables.
        Drivebase drivebase = subsystems.drivebase;
        Vision vision = subsystems.vision;
        Location location = subsystems.location;
        Clock clock = subsystems.clock;

        // Add the supported drive routines
        drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_POWER,
                new ConstantDrive("Constant Power", ControlMode.DutyCycle));
        drivebase.registerDriveRoutine(DriveRoutineType.CONSTANT_SPEED,
                new ConstantDrive("Constant Speed", ControlMode.Speed));

        drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_CLIMB,
                new ArcadeClimb("ArcadeClimb", 1.0,
                        // Throttle.
                        left.getAxis(1).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 0.36 : 1),
                        // Turn power.
                        right.getAxis(0).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 0.36 : 1)));

        // The old favourite arcade drive with throttling if a button is pressed.
        drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_DUTY_CYCLE,
                new ArcadeDrive("ArcadeDutyCycle", ControlMode.DutyCycle, 1.0,
                        // Throttle.
                        left.getAxis(1).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 0.36 : 1),
                        // Turn power.
                        right.getAxis(0).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 0.36 : 1)));

        // The old favourite arcade drive with throttling if a button is pressed but
        // using velocity mode.
        drivebase.registerDriveRoutine(DriveRoutineType.ARCADE_VELOCITY,
                new ArcadeDrive("ArcadeVelocity", ControlMode.Speed, Config.drivebase.maxSpeed,
                        // Throttle
                        left.getAxis(1).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 1 : 0.36),
                        // Turn power.
                        right.getAxis(0).invert().deadband(Config.ui.joystick.deadbandMinValue)
                                .squarePreservingSign()
                                .scale(() -> left.getTrigger().isTriggered() ? 1 : 0.36)));

        // DDR!
        if (dancepad.getButtonCount() > 0) {
            drivebase.registerDriveRoutine(DriveRoutineType.DDRPAD_DRIVE,
                    new ArcadeDrive("DDRPadDrive", ControlMode.DutyCycle, 1.0,
                            dancepad.getAxis(1).invert().scale(0.5), // Throttle.
                            dancepad.getAxis(0).invert().scale(0.4) // Turn power.
                    ));
        }

        // Cheesy drive.
        drivebase.registerDriveRoutine(DriveRoutineType.CHEESY,
                new CheesyDpadDrive("CheesyDPad", left.getDPad(0), // DPad
                        left.getAxis(GamepadButtonsX.LEFT_Y_AXIS), // Throttle
                        left.getAxis(GamepadButtonsX.RIGHT_X_AXIS), // Wheel (turn?)
                        left.getButton(GamepadButtonsX.RIGHT_TRIGGER_AXIS))); // Is quick turn

        // Drive through supplied waypoints using splines.
        drivebase.registerDriveRoutine(DriveRoutineType.TRAJECTORY,
                new TrajectoryDrive(location, clock));

        // Automatically drives to a set distance in front of the vision target.
        drivebase.registerDriveRoutine(DriveRoutineType.VISION_DRIVE,
                new VisionDrive(drivebase, vision, location, clock));

        // Turns the robot towards the vision target without changing the distance to the goal.
        drivebase.registerDriveRoutine(DriveRoutineType.VISION_AIM,
                new VisionAim(drivebase, vision, location, clock));

        // Assists the driver in steering the robot towards the goal. The robot will steer itself
        // while using the speed from the joystick. If a goal isn't visible, the robot will use the
        // turn value from the joystick.
        drivebase.registerDriveRoutine(DriveRoutineType.VISION_ASSIST,
                new VisionAssist(
                        () -> left.getAxis(1).invert().squarePreservingSign()
                                .deadband(Config.ui.joystick.deadbandMinValue).scale(2).read(),
                        () -> right.getAxis(0).squarePreservingSign()
                                .deadband(Config.ui.joystick.deadbandMinValue).scale(2).read(),
                        drivebase, vision, location, clock));

        // Turns on the spot to a specified bearing.
        drivebase.registerDriveRoutine(DriveRoutineType.TURN_TO_BEARING,
                new TurnToBearing(drivebase, vision, location, clock));

        // Map joysticks in arcade mode for testing/tuning.
        // Roughly matches the speed of normal arcade drive.
        final double maxSpeed = Config.drivebase.maxSpeed;
        final double maxTurn = 0.75 * Config.drivebase.maxSpeed;
        drivebase.registerDriveRoutine(DriveRoutineType.POSITION_PID_ARCADE,
                new PIDDrive("PIDDrive",
                        () -> left.getAxis(1).invert().squarePreservingSign()
                                .scale(maxSpeed).read(),
                        () -> right.getAxis(0).squarePreservingSign().scale(maxTurn).read(),
                        drivebase, clock));
    }

    /**
     * Three position switch showing up as two buttons. Allows switching between
     * automatic, manual and disabled modes.
     * 
     * @param box the button box as a joystick.
     * @param colour the colour of the override switch.
     * @param subsystem the subystem to set the mode on.
     */
    private void mapOverrideSwitch(DiagnosticBox box, DiagnosticBox.Colour colour,
            OverridableSubsystem<?> subsystem) {
        box.overrideSwitch(colour, () -> subsystem.setAutomaticMode(),
                () -> subsystem.setManualMode(),
                () -> subsystem.turnOff());
    }

    /**
     * Changes the sequences mapped to buttons depending on a mode. The mode can be
     * enabled or disabled based on a button. An example would be to have an
     * intaking or shooting mode, where the buttons run different sequences
     * depending on which buttons are pressed. Turning on the mode is one button and
     * turning it off is another.
     * 
     * Example:
     * 
     * <pre>
     * {@code
     * onMode(rightStick.getButton(5), rightStick.getButton(6), "climb/drive",
     *         Sequences.enableClimbMode(), Sequences.enableDriveMode())
     *                 .onPress(rightStick.getButton(1), Sequences.releaseClimberRatchet(),
     *                         Sequences.startSlowDriveForward())
     *                 .onRelease(rightStick.getButton(2), Sequences.releaseClimberRatchet(),
     *                         Sequences.driveFast());
     * }
     * </pre>
     * 
     * Button 5 enabled climb mode, button 6 enables drive mode. If in climb mode,
     * buttons 1 and 2 run different sequences.
     * 
     * @param switchOn condition used to enable the mode. Normally a button
     *        press.
     * @param switchOff condition used to disable the mode. Normally a button
     *        press.
     * @param name used for logging when the mode changes.
     * @param activateSeq sequence to run when the mode is actived.
     * @param deactiveSeq sequence to run when the mode is deactived.
     * @return the ModeSwitch for further chaining of more buttons based on the
     *         mode.
     */
    @SuppressWarnings("unused")
    private ModeSwitch onMode(Switch switchOn, Switch switchOff, String name, Sequence activateSeq,
            Sequence deactiveSeq) {
        return new ModeSwitch(switchOn, switchOff, name, activateSeq, deactiveSeq);
    }

    /**
     * Changes the sequences mapped to buttons depending on a mode. The mode can be
     * enabled or disabled based on a button. An example would be to have an
     * intaking or shooting mode, where the buttons run different sequences
     * depending on which buttons are pressed. Turning on the mode is one button and
     * turning it off is another.
     */
    @SuppressWarnings("unused")
    private class ModeSwitch {
        private boolean active = false;

        /**
         * Creates a ModeSwitch to track the state and run sequences on state change.
         * 
         * @param switchOn condition used to enable the mode. Normally a button
         *        press.
         * @param switchOff condition used to disable the mode. Normally a button
         *        press.
         * @param name used for logging when the mode changes.
         * @param activatedSeq sequence to run when the mode is actived.
         * @param deactivedSeq sequence to run when the mode is deactived.
         * @return the ModeSwitch for chaining of more buttons based on the mode.
         */
        public ModeSwitch(Switch switchOn, Switch switchOff, String name, Sequence activatedSeq,
                Sequence deactivedSeq) {
            Strongback.switchReactor().onTriggered(switchOn, () -> {
                if (active) {
                    return;
                }
                Log.debug("Sequences", "Activating " + name);
                controller.run(activatedSeq);
                active = true;
            });
            Strongback.switchReactor().onTriggered(switchOff, () -> {
                if (!active) {
                    return;
                }
                Log.debug("Sequences", "Deactivating " + name);
                controller.run(deactivedSeq);
                active = false;
            });
        }

        /**
         * Run different sequences depending on the mode on button press.
         * 
         * @param swtch condition to trigger a sequence to run. Normally a button
         *        press.
         * @param activeSeq sequence to run if the mode is active.
         * @param inactiveSeq sequence to run if the mode is inactive.
         * @return the ModeSwitch for further chaining of more buttons based on the
         *         mode.
         */
        public ModeSwitch onPress(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
            reactor.onTriggered(swtch, () -> {
                if (active) {
                    controller.run(activeSeq);
                } else {
                    controller.run(inactiveSeq);
                }
            });
            return this;
        }

        /**
         * Run different sequences depending on the mode on button release.
         * 
         * @param swtch condition to trigger a sequence to run. Normally a button
         *        release.
         * @param activeSeq sequence to run if the mode is active.
         * @param inactiveSeq sequence to run if the mode is inactive.
         * @return the ModeSwitch for further chaining of more buttons based on the
         *         mode.
         */
        public ModeSwitch onRelease(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
            reactor.onUntriggered(swtch, () -> {
                if (active) {
                    controller.run(activeSeq);
                } else {
                    controller.run(inactiveSeq);
                }
            });
            return this;
        }

        /**
         * Run different sequences depending on the mode while a button is pressed.
         * 
         * @param swtch condition to trigger a sequence to run. Normally while a
         *        button is pressed.
         * @param activeSeq sequence to run if the mode is active.
         * @param inactiveSeq sequence to run if the mode is inactive.
         * @return the ModeSwitch for further chaining of more buttons based on the
         *         mode.
         */
        public ModeSwitch whileTriggered(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
            reactor.whileTriggered(swtch, () -> {
                if (active) {
                    controller.run(activeSeq);
                } else {
                    controller.run(inactiveSeq);
                }
            });
            return this;
        }
    }

    /**
     * Converts a sequence into something runnable. Returns the sequence name in
     * toString() for the code that prints out the button mappings in Trigger.
     * 
     * @param sequence the sequence to run.
     * @return Runnable that can be executed by the Trigger methods.
     */
    public Runnable run(Sequence sequence) {
        return new Runnable() {
            @Override
            public void run() {
                controller.run(sequence);
            }

            @Override
            public String toString() {
                return sequence.getName();
            }
        };

    }
}
