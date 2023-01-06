package frc.robot;



import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.VideoMode;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.net.PortForwarder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.controller.Controller;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.LogHelper;
import frc.robot.lib.ConfigServer;
import frc.robot.lib.LEDColour;
import frc.robot.lib.LogServer;
import frc.robot.lib.PowerMonitor;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import frc.robot.lib.log.LogFileNumber;
import frc.robot.subsystems.Subsystems;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.function.Supplier;
import org.jibble.simplewebserver.SimpleWebServer;
import org.strongback.Executable;
import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.ui.Dancepad;
import org.strongback.components.ui.DiagnosticBox;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.Gamepad;
import org.strongback.hardware.Hardware;

public class Robot extends TimedRobot implements Executable, LogHelper {
    private Clock clock;

    // User interface.
    private FlightStick driverLeftJoystick, driverRightJoystick;
    private Gamepad operatorGamepad;
    private Dancepad dancepad;
    private DiagnosticBox operatorBox;

    // Main logic
    private Controller controller;

    // Subsystems/misc
    private Subsystems subsystems;
    @SuppressWarnings("unused")
    private PowerMonitor pdp;
    private Auto auto;
    private LogServer logServer;

    /*
     * We wish to delay our full setup until; the driver's station has connected. At
     * at that point we have received the number of joysticks, the configuration of
     * the joysticks, and information about autonomous selections.
     * 
     * This can be done by waiting until robotPeriodic() is called the first time,
     * as this is called once we are in communications with the driver's station.
     */
    @Override
    public void robotInit() {
        clock = Strongback.timeSystem();
        startWebServer();
        startConfigServer();
        info("Waiting for driver's station to connect before setting up UI");
        // Do the reset of the initialization in init().
    }

    private boolean setupCompleted = false;

    public void maybeInit() {
        if (setupCompleted)
            return;
        try {
            init();
            setupCompleted = true;
        } catch (Exception e) {
            // Write the exception to the log file.
            exception("Exception caught while initializing robot", e);
            throw e; // Cause it to abort the robot startup.
        }
    }

    /**
     * Initialize the robot now that the drivers station has connected.
     */
    public void init() {
        Strongback.logConfiguration();
        Strongback.setExecutionPeriod(Config.intervals.executorCycleMSec);
        startLogServer();
        info("initialization started");

        createInputDevices();

        // Setup the hardware/subsystems. Listed here so can be quickly jumped to.
        subsystems = new Subsystems(clock, operatorGamepad);
        subsystems.createLEDStrip();
        subsystems.createPneumatics(); // Must be called before createDrivebase if using solenoids.
        subsystems.createDrivebase();
        subsystems.createLocation();
        subsystems.createIntake();
        subsystems.createVelcroIntake();
        subsystems.createShooter();
        subsystems.createFeeders();
        subsystems.createConveyor();
        subsystems.createOverrides();
        subsystems.createVision();
        subsystems.createClimber();
        subsystems.createMonitor();

        createPowerMonitor();
        createCameraServers();

        // Create the brains of the robot. This runs the sequences.
        controller = new Controller(subsystems);

        // Setup the interface to the user, mapping buttons to sequences for the
        // controller.
        setupUserInterface();

        createTimeEventSymlinks(); // All subsystems have registered by now, enable logging.
        if (!Config.charting.enabled) {
            error("Chart sampling disabled");
        } else {
            // Low priority means run every 20 * 4 = 80ms, or at 12.5Hz
            // It polls almost everything on the CAN bus, so don't want it to be too fast.
            Strongback.executor().register(new Chart(), Priority.LOW);
        }
        Strongback.executor().register(this, Priority.LOW);

        // Start the scheduler to keep all the subsystems working in the background.
        Strongback.start();

        // Setup the auto sequence chooser.
        auto = new Auto();

        // Write out the example config and print any config warnings.
        Config.finishLoadingConfig();

        info("Robot initialization successful");
    }

    /**
     * Called every 20ms while the drivers station is connected.
     */
    @Override
    public void robotPeriodic() {
        // Nothing to do. Dashboard is updated by the executor.
    }

    /**
     * Called when the robot starts the disabled mode. Normally on first start and
     * after teleop and autonomous finish.
     */
    @Override
    public void disabledInit() {
        // Start forwarding to port 22 (ssh port) for pulling logs using rsync.
        PortForwarder.add(Config.logging.rsync.port, Config.logging.rsync.hostname, 22);
        maybeInit(); // Called before robotPeriodic().
        info("disabledInit");

        // Tell the controller to give up on whatever it was processing.
        controller.disable();
        // Disable all subsystems
        subsystems.disable();
    }

    /**
     * Called every 20ms while the robot is in disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        subsystems.ledStrip.updateRainbow();
    }

    /**
     * Called once when the autonomous period starts.
     */
    @Override
    public void autonomousInit() {
        PortForwarder.remove(Config.logging.rsync.port); // Stop forwarding port to stop rsync and
                                                         // save bandwidth.
        Log.restartLogs();
        Chart.restartCharts();
        createTimeEventSymlinks();
        info("auto has started");
        controller.enable();
        subsystems.enable();

        controller.run(Sequences.getStartSequence());
        Pose2d resetPose = new Pose2d(0, 0, new Rotation2d(0));
        subsystems.location.setCurrentPose(resetPose);

        // Kick off the selected auto program.
        auto.executedSelectedSequence(controller);
    }

    /**
     * Called every 20ms while in the autonomous period.
     */
    @Override
    public void autonomousPeriodic() {}

    /**
     * Called once when the teleop period starts.
     */
    @Override
    public void teleopInit() {
        // Stop forwarding port to stop rsync and save bandwidth.
        PortForwarder.remove(Config.logging.rsync.port);
        Log.restartLogs();
        Chart.restartCharts();
        createTimeEventSymlinks();
        info("teleop has started");
        controller.enable();
        subsystems.enable();
        controller.run(Sequences.setDrivebaseToDefault());
        subsystems.ledStrip.setAlliance(getAllianceLEDColour().get());
    }

    /**
     * Called every 20ms while in the teleop period. All the logic is kicked off
     * either in response to button presses or by the strongback scheduler. No
     * spaghetti code here!
     */

    @Override
    public void teleopPeriodic() {
        // While in teleop out of a match, the match time is -1.
        if (0 <= DriverStation.getMatchTime()
                && DriverStation.getMatchTime() <= Config.ledStrip.countdown) {
            subsystems.setLEDFinalCountdown(DriverStation.getMatchTime());
        }
    }

    /**
     * Called when the test mode is enabled.
     */
    @Override
    public void testInit() {
        Log.restartLogs();
        Chart.restartCharts();
        createTimeEventSymlinks();
        controller.enable();
        subsystems.enable();
    }

    /**
     * Called every 20ms during test mode.
     */
    @Override
    public void testPeriodic() {}

    /**
     * Create the camera servers so the driver & operator can see what the robot can
     * see.
     */
    public void createCameraServers() {
        if (Config.vision.present) {
            // Select FIRST Python processor on the Jevois camera by setting a particular
            // resolution, frame rate and format.
            CameraServer.startAutomaticCapture(0).setVideoMode(VideoMode.PixelFormat.kYUYV,
                    Config.vision.camera.resolution.width,
                    Config.vision.camera.resolution.height, Config.vision.camera.framesPerSecond);
        } else {
            debug("Vision not enabled, not creating a vision camera server");
        }

        if (Config.camera.present) {
            // Add Axis network camera by hostname
            CameraServer.addAxisCamera("10.31.32.3");
        } else {
            debug("Network camera not enabled, not creating a network camera server");
        }
    }

    /**
     * Motor the power draw. With the wpilibj interface this can slow down the
     * entire robot due to lock conflicts.
     */
    private void createPowerMonitor() {
        // Do not monitor if not present, or we have been asked not to monitor
        boolean enabled = Config.pdp.present || Config.pdp.monitor;
        if (enabled) {
            pdp = new PowerMonitor(
                    new PowerDistribution(Config.pdp.canId,
                            PowerDistribution.ModuleType.kCTRE),
                    Config.pdp.channels,
                    enabled);
        }
    }

    /**
     * Start a websocket to publish new log messages to websocket clients
     */
    private void startLogServer() {
        logServer =
                new LogServer(
                        new InetSocketAddress("0.0.0.0", Config.logging.liveloggingserver.port));
        logServer.setReuseAddr(true);
    }

    /**
     * Create the simple web server so we can interrogate the robot during
     * operation. The web server lives on a port that is available over the
     * firewalled link. We use port 5800, the first of the opened ports.
     * 
     */
    private void startWebServer() {
        File fileDir = new File(Config.logging.webserver.path);
        try {
            new SimpleWebServer(fileDir, Config.logging.webserver.port);
            debug("WebServer started at port: " + Config.logging.webserver.port);
        } catch (Exception e) {
            debug("Failed to start webserver on directory " + fileDir.getAbsolutePath());

            e.printStackTrace();
        }
    }

    /**
     * Creates the web server for allowing easy modification of the robot's config
     * file using port 5801.
     */
    private void startConfigServer() {
        String webRoot = Robot.isReal() ? Config.config.webserver.root : "src/main/deploy/www";
        try {
            new ConfigServer(webRoot, Config.config.configFilePath,
                    Config.config.robotNameFilePath, Config.config.webserver.port);
            debug("Config webserver started at port: " + Config.config.webserver.port);
        } catch (Exception e) {
            debug("Failed to start config webserver.");
            e.printStackTrace();
        }
    }

    /**
     * Create the joysticks
     */
    private void createInputDevices() {
        boolean allowMock = Robot.isSimulation() || Config.ui.joystick.allowMock;
        driverLeftJoystick =
                Hardware.HumanInterfaceDevices.logitechAttack3D("left joystick", 0, allowMock);
        driverRightJoystick =
                Hardware.HumanInterfaceDevices.logitechAttack3D("right joystick", 1, allowMock);
        operatorGamepad = Hardware.HumanInterfaceDevices.logitechF310("gamepad", 2, allowMock);
        operatorBox = Hardware.HumanInterfaceDevices.diagnositicBox(3);
        dancepad = Hardware.HumanInterfaceDevices.dancepad(4);
    }

    /**
     * Setup the button mappings on the joysticks and the operators button box if
     * it's attached.
     */
    private void setupUserInterface() {
        OI oi = new OI(controller, subsystems);
        oi.configureDriverJoysticks(driverLeftJoystick, driverRightJoystick);
        oi.configureOperatorJoystick(operatorGamepad);
        if (operatorBox.getButtonCount() > 0) {
            info("Operator box detected");
            oi.configureDiagBox(operatorBox);
        }
        if (dancepad.getButtonCount() > 0) {
            info("Dancepad detected");
            oi.configureDDRPad(dancepad);
        }
        oi.registerDriveRoutines(driverLeftJoystick, driverRightJoystick, dancepad);
        Chart.register(DriverStation::getMatchTime, "DriverStation/MatchTime");
    }

    /**
     * Create date and fms log symbolic links
     */
    private void createTimeEventSymlinks() {
        // Tell the logger what symbolic link to the log file based on the match name to
        // use.
        String matchDescription = "NoMatchDesc";
        if (DriverStation.getMatchType().toString() != "None") {
            matchDescription =
                    String.format("%s_%s_M%d_R%d_%s_P%d_L%d", DriverStation.getEventName(),
                            DriverStation.getMatchType().toString(), DriverStation.getMatchNumber(),
                            DriverStation.getReplayNumber(), DriverStation.getAlliance().toString(),
                            DriverStation.getLocation(), LogFileNumber.get());
        }
        Chart.registrationComplete(matchDescription);
        Log.createDateFiles(Calendar.getInstance(), matchDescription);
    }

    @Override
    public void execute(long timeInMillis) {
        // Logger.debug("Updating smartDashboard");
        maybeUpdateSmartDashboard();
    }

    private double lastDashboardUpdateSec = 0;

    /**
     * Possibly update the smartdashboard. Don't do this too often due to the amount
     * that is sent to the dashboard.
     */
    private void maybeUpdateSmartDashboard() {
        double now = Strongback.timeSystem().currentTime();
        if (now < lastDashboardUpdateSec + Config.intervals.dashboardUpdateSec)
            return;
        lastDashboardUpdateSec = now;
        subsystems.updateDashboard();
        // pdp.updateDashboard();
        controller.updateDashboard();
    }

    private Supplier<LEDColour> getAllianceLEDColour() {
        return new Supplier<LEDColour>() {
            @Override
            public LEDColour get() {
                switch (DriverStation.getAlliance()) {
                    case Red:
                        return LEDColour.RED;
                    case Blue:
                        return LEDColour.BLUE;
                    default:
                        return LEDColour.WHITE;
                }
            }
        };
    }

    @Override
    public String getName() {
        return "Robot";
    }
}
