package frc.robot.subsystems;



import edu.wpi.first.wpilibj.PneumaticsModuleType;
import frc.robot.Config;
import frc.robot.interfaces.*;
import frc.robot.lib.JevoisImpl;
import frc.robot.lib.LEDColour;
import frc.robot.lib.MotorFactory;
import frc.robot.lib.NavXGyroscope;
import frc.robot.mock.*;
import frc.robot.simulator.IntakeSimulator;
import java.io.IOException;
import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;
import org.strongback.components.Motor;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Servo;
import org.strongback.components.Solenoid;
import org.strongback.components.ui.InputDevice;
import org.strongback.hardware.Hardware;
import org.strongback.mock.Mock;

/**
 * Contains the subsystems for the robot.
 * 
 * Makes it easy to pass all subsystems around.
 */
public class Subsystems implements DashboardUpdater, LogHelper {
    // Not really a subsystem, but used by all subsystems.
    public Clock clock;
    public LEDStrip ledStrip;
    public Location location;
    public Drivebase drivebase;
    public Intake intake;
    public Intake hwIntake;
    public Feeder velcroIntake; // Feeder subsystem is just a wrapper for a motor, perfect for this
                                // subsystem too.
    public Climber climberLeft;
    public Climber climberRight;
    public OverridableSubsystem<Intake> intakeOverride;
    public Conveyor conveyor;
    public Conveyor hwConveyor; // Keep track of the real hardware for dashboard update
    public OverridableSubsystem<Conveyor> conveyorOverride;
    public Shooter shooter;
    public Shooter hwShooter;
    public Feeder feederLeft;
    public Feeder feederRight;
    public OverridableSubsystem<Shooter> shooterOverride;
    public PneumaticsModule pcm;
    public Vision vision;
    public Jevois jevois;
    public Monitor monitor;
    public InputDevice gamepad;

    public Subsystems(Clock clock, InputDevice gamepad) {
        this.clock = clock;
        this.gamepad = gamepad;
    }

    public void createOverrides() {
        createIntakeOverride();
        createConveyorOverride();
        createShooterOverride();
    }

    public void enable() {
        info("Enabling subsystems");
        gamepad.setRumbleLeft(0);
        gamepad.setRumbleRight(0);
        drivebase.enable();
        hwIntake.enable();
        velcroIntake.enable();
        climberLeft.enable();
        climberRight.enable();
        hwShooter.enable();
        feederLeft.enable();
        feederRight.enable();
        hwConveyor.enable();
    }

    public void disable() {
        info("Disabling Subsystems");
        gamepad.setRumbleLeft(0);
        gamepad.setRumbleRight(0);
        drivebase.disable();
        hwIntake.disable();
        velcroIntake.disable();
        climberLeft.disable();
        climberRight.disable();
        hwShooter.disable();
        feederLeft.disable();
        feederRight.disable();
        hwConveyor.disable();
    }

    @Override
    public void updateDashboard() {
        drivebase.updateDashboard();
        hwIntake.updateDashboard();
        location.updateDashboard();
        hwConveyor.updateDashboard();
        hwShooter.updateDashboard();
        vision.updateDashboard();
        climberLeft.updateDashboard();
        climberRight.updateDashboard();
        feederLeft.updateDashboard();
        feederRight.updateDashboard();
    }

    /**
     * Create the drivebase subsystem. Creates the motors.
     */
    public void createDrivebase() {
        if (!Config.drivebase.present) {
            debug("Using mock drivebase");
            drivebase = new MockDrivebase();
            return;
        }
        Motor leftMotor = MotorFactory.getDriveMotor(true, clock);
        Motor rightMotor = MotorFactory.getDriveMotor(false, clock);

        leftMotor.setPosition(0);
        rightMotor.setPosition(0);
        try {
            // Let the encoders get the message and have time to send it back to us.
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        error("Reset drive encoders to zero, currently are: %f, %f", leftMotor.getPosition(),
                rightMotor.getPosition());
        // metres.
        drivebase =
                new DrivebaseImpl(leftMotor, rightMotor);
        Strongback.executor().register(drivebase, Priority.HIGH);
    }

    /**
     * Create the location subsystem. Creates the gyro.
     */
    public void createLocation() {
        if (!Config.drivebase.present) {
            debug("No drivebase, using mock location");
            location = new MockLocation();
            return;
        }
        Gyroscope gyro = new NavXGyroscope("NavX", Config.navx.present);
        gyro.zero();
        // Encoders must return metres.
        location = new LocationImpl(drivebase, gyro, clock);
        Strongback.executor().register(location, Priority.HIGH);
    }

    public void createIntake() {
        if (!Config.intake.present) {
            intake = hwIntake = new MockIntake();
            debug("Intake not present, using a mock intake instead");
            return;
        }

        Solenoid intakeSolenoid = pcm.singleSolenoid(Config.intake.solenoidPort, 0.2, 0.2);
        Motor intakeMotor = MotorFactory.getIntakeMotor();
        intake = hwIntake = new IntakeImpl(intakeMotor, intakeSolenoid);
    }

    public void createVelcroIntake() {
        if (!Config.velcro.present) {
            velcroIntake = new MockFeeder("velcro");
            return;
        }
        Motor velcro = MotorFactory.getVelcroMotor();
        velcroIntake = new FeederImpl(velcro, "velcro");
    }

    public void createIntakeOverride() {
        // Setup the diagBox so that it can take control.
        IntakeSimulator simulator = new IntakeSimulator();
        MockIntake mock = new MockIntake();
        intakeOverride = new OverridableSubsystem<Intake>("intake", Intake.class, intake, simulator,
                mock);
        // Plumb accessing the intake through the override.
        intake = intakeOverride.getNormalInterface();
        Strongback.executor().register(simulator, Priority.HIGH);
    }

    public void createClimber() {
        if (!Config.climber.present) {
            climberLeft = new MockClimber();
            climberRight = new MockClimber();
            debug("Climber not present, using a mock climber instead");
            return;
        }
        Motor left = MotorFactory.getClimberMotor(true);
        Motor right = MotorFactory.getClimberMotor(false);
        left.setPosition(0);
        right.setPosition(0);
        climberLeft = new ClimberImpl(left, "left climber");
        climberRight = new ClimberImpl(right, "right climber");
    }

    public void createLEDStrip() {
        if (!Config.ledStrip.present) {
            ledStrip = new MockLEDStrip();
            debug("LED Strip not present, using a mock LED Strip instead.");
            return;
        }
        ledStrip = new LEDStripImpl(Config.ledStrip.pwmPort, Config.ledStrip.numLEDs);
    }

    public void createMonitor() {
        monitor = new MonitorImpl();
    }

    public void setLEDFinalCountdown(double time) {
        ledStrip.setProgressColour(LEDColour.RED, LEDColour.GREEN,
                1 - (time / Config.ledStrip.countdown));
    }

    public void createConveyor() {
        if (!Config.conveyor.present) {
            conveyor = hwConveyor = new MockConveyor();
            debug("Created a mock conveyor!");
            return;
        }

        Motor motor = MotorFactory.getConveyorMotor();
        conveyor = hwConveyor = new ConveyorImpl(motor);
        Strongback.executor().register(conveyor, Priority.LOW);
    }

    public void createConveyorOverride() {
        // Setup the diagBox so that it can take control.
        MockConveyor simulator = new MockConveyor(); // Nothing to simulate, use the mock
        MockConveyor mock = new MockConveyor();
        conveyorOverride =
                new OverridableSubsystem<Conveyor>("conveyor", Conveyor.class, conveyor, simulator,
                        mock);
        conveyor = conveyorOverride.getNormalInterface();
    }

    public void createShooter() {
        if (!Config.shooter.present) {
            shooter = hwShooter = new MockShooter();
            debug("Created a mock shooter!");
            return;
        }

        Motor flywheel = MotorFactory.getShooterMotor(clock);
        Servo[] hoodServos = new Servo[Config.shooter.hood.channels.length];
        for (int i = 0; i < Config.shooter.hood.channels.length; i++) {
            // Use initial value slightly off middle of safe extension range as this value is near
            // desired starting positions. Setting the initial position doesn't move the servo but
            // makes the code think it is there, hence the slightly off value.
            hoodServos[i] =
                    Hardware.Servos.timedServo(Config.shooter.hood.channels[i], 0, 1, 0.1, 0.124);
            // TODO: Measure correct travel speed
            // Clamping of position is done in shooter class
            hoodServos[i].setBounds(2, 1.5, 1.5, 1.5, 1);
        }

        shooter = hwShooter = new ShooterImpl(flywheel, hoodServos);
    }

    public void createFeeders() {
        if (!Config.feeder.present) {
            feederLeft = new MockFeeder("left feeder");
            feederRight = new MockFeeder("right feeder");
            debug("Created mock feeders!");
            return;
        }

        Motor left = MotorFactory.getFeederMotor(true);
        Motor right = MotorFactory.getFeederMotor(false);

        feederLeft = new FeederImpl(left, "left feeder");
        feederRight = new FeederImpl(right, "right feeder");
    }

    public void createShooterOverride() {
        // Setup the diagBox so that it can take control.
        MockShooter simulator = new MockShooter(); // Nothing to simulate, use a mock instead.
        MockShooter mock = new MockShooter();
        shooterOverride = new OverridableSubsystem<Shooter>("shooter", Shooter.class, shooter,
                simulator, mock);
        // Plumb accessing the shooter through the override.
        shooter = shooterOverride.getNormalInterface();
    }

    /**
     * Create the Pneumatics Control Module (PCM) subsystem.
     */
    public void createPneumatics() {
        if (!Config.pcm.present) {
            pcm = Mock.pneumaticsModule(Config.pcm.canId);
            debug("Created a mock compressor");
            return;
        }
        pcm = Hardware.pneumaticsModule(Config.pcm.canId, PneumaticsModuleType.CTREPCM);
    }

    public void createVision() {
        if (!Config.vision.present) {
            vision = new MockVision();
            debug("Created a mock vision subsystem");
            return;
        }
        try {
            jevois = new JevoisImpl();
            vision = new VisionImpl(jevois, location, clock, Config.vision.hMin,
                    Config.vision.sMin,
                    Config.vision.vMin, Config.vision.hMax, Config.vision.sMax, Config.vision.vMax);
        } catch (IOException e) {
            exception("Unable to create an instance of the jevois camera", e);
            e.printStackTrace();
            vision = new MockVision();
        }
    }

    @Override
    public String getName() {
        return "Subsystems";
    }
}
