package frc.robot;



import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.math.trajectory.constraint.TrajectoryConstraint;
import frc.robot.lib.ConfigReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.strongback.components.PIDF;

/**
 * Class responsible for updating values which are dependent on robot hardware.
 * (e.g. if subsystems are present or not) It reads from a text file Currently
 * the supported types are String, int, double, boolean and int array.
 * 
 * Example lines:
 * drivebase/present = true
 * drivebase/rampRate = 0.13125
 * drivebase/right/canIDs/withEncoders = 7,6
 * drivebase/right/canIDs/withoutEncoders = 5
 * 
 * The configuration can be overridden on each robot by changing a text file
 * stored on the robot allowing different robots to have different configuration
 * preventing having to modify the code each time it's pushed to a different bit
 * of hardware.
 * 
 * This is very useful for testing when parts of the hardware are not attached,
 * delivered or even broken.
 */
public class Config {

    /*
     * Drivebase parameters
     * 
     * Six wheel drop centre with 6" wheels.
     */
    public static class drivebase {
        public static final boolean present = getBoolean("drivebase/present", false);
        public static final String motorControllerType =
                getMotorControllerType("drivebase/motorControllerType",
                        motorController.defaultType);
        public static final int[] canIdsLeftWithEncoders =
                getIntArray("drivebase/left/canIDs/withEncoders",
                        new int[] {1, 2});
        public static final int[] canIdsLeftWithoutEncoders =
                getIntArray("drivebase/left/canIDs/withoutEncoders",
                        new int[] {});
        public static final int[] canIdsRightWithEncoders =
                getIntArray("drivebase/right/canIDs/withEncoders",
                        new int[] {3, 4});
        public static final int[] canIdsRightWithoutEncoders =
                getIntArray("drivebase/right/canIDs/withoutEncoders",
                        new int[] {});
        public static final boolean currentLimiting = getBoolean("drivebase/currentLimiting", true);
        public static final int contCurrent = getInt("drivebase/contCurrent", 38);
        public static final int peakCurrent = getInt("drivebase/peakCurrent", 80);
        public static final double rampRate = getDouble("drivebase/rampRate", 0.1);
        public static final PIDF pidf = getPIDF("drivebase", new PIDF(0, 0, 0, 0.7));
        // Max speed it can drive at in m/s
        public static final double maxSpeed = getDouble("drivebase/maxSpeed", 4.0);
        // How quickly it can accelerate in m/s/s
        public static final double maxJerk = getDouble("drivebase/maxJerk", 2.0);
        public static final boolean swapLeftRight = getBoolean("drivebase/swapLeftRight", false);
        public static final boolean sensorPhase = getBoolean("drivebase/sensor/phase", false);
        // Distance the robot moves per revolution of the wheels.
        public static final double wheelDiameterMetres = 6 * constants.inchesToMetres; // 6" wheels.
        public static final double metresPerRev = wheelDiameterMetres * Math.PI;
        public static final double gearboxRatio = 10.71;
        public static final double trackwidthMeters = 0.57;
        public static final String driveRoutine =
                getString("drivebase/driveRoutine", "ARCADE_DUTY_CYCLE");

        /**
         * Drive routine configuration
         * All units are in metres.
         */
        public static class routine {
            /**
             * Turn to bearing drive routine
             */
            public static class turnToBearing {
                public static final double scale = 0.04;
            }

            /**
             * Drive to the goal.
             */
            public static class visionDrive {
                public static final double speedScale = 0.06;
                public static final double angleScale = 0.6;
                public static final double splineMinDistanceMetres = 1.5;
                public static final double waypointDistanceScale = 0.5; // percentage 0 to 1
                public static final double maxSpeed = 2; // metres/sec
                public static final double distanceBeforeGoal = 1; // metres
            }

            /**
             * Assist the driver to aim at the goal by taking over steering when
             * a target can be seen.
             */
            public static class visionAssist {
                public static final double angleScale = 0.02;
            }

            /**
             * Automatically turn to a vision target, giving up if no target.
             */
            public static class visionAim {
                public static final double speedScale = 0.06;
                public static final double angleToleranceDegrees = 2; // degrees
                public static final double angleScale = 0.03;
            }
        }

        /**
         * Spline / trajectory driving.
         */
        public static final class trajectory {

            public static final DifferentialDriveKinematics driveKinematics =
                    new DifferentialDriveKinematics(trackwidthMeters);

            // The Robot Characterization Toolsuite provides a convenient tool for obtaining
            // these values for your robot.
            public static final double ksVolts = 0.64903;
            public static final double kvVoltSecondsPerMeter = 2.4053;
            public static final double kaVoltSecondsSquaredPerMeter = 0.48536;

            // PID values.
            public static final double kPDriveVel = 0.01; // should be 12.1
            // kD should be 0

            public static final double maxSpeedMetersPerSecond = 4;
            public static final double maxAccelerationMetersPerSecondSquared = 2;

            // Reasonable baseline values for a RAMSETE follower in units of meters and
            // seconds
            public static final double kRamseteB = 2;
            public static final double kRamseteZeta = 0.7;

            public static final double maxVoltage = 10;

            // Create a voltage constraint to ensure we don't accelerate too fast
            public static final TrajectoryConstraint autoVoltageConstraint =
                    new DifferentialDriveVoltageConstraint(new SimpleMotorFeedforward(ksVolts,
                            kvVoltSecondsPerMeter, kaVoltSecondsSquaredPerMeter), driveKinematics,
                            maxVoltage);
        }
    }

    public static class auto {
        public static class threeBall {
            public static final boolean returnToStart = getBoolean("auto/returnToStart", false);
        }
    }

    /**
     * NavX
     * 
     * Using the gyro for autonomous routines.
     */
    public static class navx {
        public static final boolean present = getBoolean("navx/present", true);
    }

    /*
     * Climber parameters.
     * 
     * The climber is a PTO from the drivebase. There is a solenoid to release the
     * ratchet.
     */
    public static class climber {
        public static final boolean present = getBoolean("climber/present", false);
        public static final PIDF pidf = getPIDF("climber", new PIDF(0, 0, 0, 0));
        public static final double dutyCycle = getDouble("climber/dutyCycle", 0.3);
        public static final int currentLimit = getInt("climber/currentLimit", 5);
        public static final double gearboxRatio = 21;
        public static final double metresPerRev = 0.3177;

        public static class left {
            public static final int canID = getInt("climber/left/canID", 50);
        }

        public static class right {
            public static final int canID = getInt("climber/right/canID", 51);
        }
    }

    /*
     * Intake parameters.
     * 
     * Uses a pneumatic to deploy and a motor to run mecanum wheels.
     */
    public static class intake {
        public static final boolean present = getBoolean("intake/present", false);
        public static final int canID = getInt("intake/canID", 10);
        public static final PIDF pidf = getPIDF("intake", new PIDF(0.015, 0.0, 15.0, 0.019));
        public static final int solenoidPort = getInt("intake/solenoidPort", 0);
        public static final double gearboxRatio = 5;
        public static final double targetRPS = 90;
        public static final int stallCurrent = getInt("intake/stallCurrent", 25);
        public static final int freeCurrent = getInt("intake/freeCurrent", 45);
        public static final int peakCurrentDuration = getInt("intake/peakCurrentDuration", 100);
    }

    public static class velcro {
        public static final boolean present = getBoolean("velcro/present", false);
        public static final int canID = getInt("velcro/canID", 11);
        public static final PIDF pidf = new PIDF(0.0, 0.0, 0.0, 0.0);
        public static final double dutyCycle = getDouble("velcro/dutyCycle", 0.3);
        public static final int contCurrent = getInt("velcro/contCurrent", 10);
        public static final int peakCurrent = getInt("velcro/peakCurrent", 20);
        public static final int peakCurrentDuration = getInt("velcro/peakCurrentDuration", 100);
    }

    /**
     * Conveyor parameters.
     * 
     * A conveyor belt for pushing the balls into the shooter.
     */
    public static class conveyor {
        public static final boolean present = getBoolean("conveyor/present", false);

        public static final int canID = getInt("conveyor/canID", 20);
        public static final PIDF pidf = new PIDF(0.0, 0.0, 0.0, 0.0);
        public static final double dutyCycle = getDouble("conveyor/dutyCycle", 1.0);
        public static final double ejectSingleDutyCycle =
                getDouble("conveyor/ejectSingleDutyCycle", 0.33);
        public static final double idleDutyCycle = getDouble("conveyor/idleDutyCycle", 0.3);

        public static final int contCurrent = getInt("conveyor/contCurrent", 15);
        public static final int peakCurrent = getInt("conveyor/peakCurrent", 25);
        public static final int peakCurrentDuration = getInt("conveyor/peakCurrentDuration", 100);
    }

    /**
     * Shooter parameters.
     * 
     * A single shooter wheel powered by two motors.
     */
    public static class shooter {
        public static final boolean present = getBoolean("shooter/present", false);
        public static final int[] canIds =
                getIntArray("shooter/shooterCanIDs", new int[] {40, -41});
        public static final PIDF pidf = getPIDF("shooter", new PIDF(0.35, 0.0, 3.5, 0.06));
        public static final double gearboxRatio = 24.0 / 16.0;

        public static final int contCurrent = getInt("shooter/contCurrent", 35);
        public static final int peakCurrent = getInt("shooter/peakCurrent", 50);
        public static final int peakCurrentDuration = getInt("shooter/peakCurrentDuration", 100);

        /**
         * Different shooting speeds depending on position on the field.
         */
        public static class speed {
            public static final double toleranceRPS = getDouble("shooter/toleranceRPS", 2.0);
            public static final double fenderRPS = getDouble("shooter/fenderRPS", 43.0);
            public static final double autoRPS = getDouble("shooter/autoRPS", 52.0);
            public static final double hangerRPS = getDouble("shooter/hangerRPS", 58.0);
            public static final double ejectRPS = getDouble("shooter/ejectRPS", 12.5);
        }

        public static class hood {
            public static final int[] channels =
                    getIntArray("shooter/hood/channels", new int[] {0, 1, 2});
            public static final double fenderAngle = getDouble("shooter/hood/fenderAngle", 2.5);
            public static final double autoAngle = getDouble("shooter/hood/autoAngle", 27.5);
            public static final double hangerAngle = getDouble("shooter/hood/hangerAngle", 30.0);
            public static final double resetAngle = getDouble("shooter/hood/resetAngle", 15.0);
            public static final double minAngle = 0;
            public static final double maxAngle = 30;
            // min/max extension (mm) are the safe limits at which the servo can operate, be
            // extremely careful tuning these values as servos may move beyond the safe limits and
            // break something
            public static final double minExtension = 0.0 / 140;
            public static final double maxExtension = 47.0 / 140;
        }
    }

    /**
     * Feeder parameters
     * 
     * Two motors controlled individually to feed balls into the shooter
     */
    public static class feeder {
        // TODO: set correct values
        public static final boolean present = getBoolean("feeder/present", false);
        public static final PIDF pidf = new PIDF(0.0, 0.0, 0.0, 0.0);
        public static final double dutyCycle = 1.0;
        // delay in seconds between left and right feeder motors starting
        public static final double leftRightDelay = getDouble("feeder/leftRightDelay", 0.6);
        public static final int contCurrent = getInt("feeder/contCurrent", 15);
        public static final int peakCurrent = getInt("feeder/peakCurrent", 25);
        public static final int peakCurrentDuration = getInt("feeder/peakCurrentDuration", 100);

        public static class left {
            public static final int canID = getInt("feeder/left/canID", 30);
        }

        public static class right {
            public static final int canID = getInt("feeder/right/canID", 31);
        }
    }

    /**
     * Location parameters
     *
     * Tracks current and historical position.
     */
    public static class location {
        public static class history {
            public static final int memorySecs = 5;
            public static final int cycleSpeedHz = 100;
        }
    }

    /**
     * PDP parameters
     * 
     * The motor controller wrappers also monitor current, so this is normally off.
     */
    public static class pdp {
        public static final boolean present = getBoolean("pdp/present", true);
        public static final int canId = getInt("pdp/canID", 0);
        // By default we do not monitor the PDP (CAN performance concerns)
        public static final boolean monitor = getBoolean("pdp/monitor", true);
        // By default we do not monitor any channels (motor controllers will also monitor)
        public static final int[] channels = getIntArray("pdp/channels", new int[0]);
    }

    /**
     * PCM parameters
     * 
     * Pneumatic control module controls intake, the climber ratchet, buddy climb, the
     * shooter hood and the colour wheel deployment.
     */
    public static class pcm {
        public static final boolean present = getBoolean("pcm/present", false);
        public static final int canId = getInt("pcm/canID", 60);
    }

    public static class camera {
        public static final boolean present = getBoolean("camera/present", false);
    }

    /**
     * Vision parameters
     * 
     * A jevois camera connected via USB for detecting the vision target on the
     * goal.
     */
    public static class vision {
        public static final boolean present = getBoolean("vision/present", false);
        // Filtering parameters
        public static final double hMin = getDouble("vision/hsvFilter/h/min", 65.0);
        public static final double hMax = getDouble("vision/hsvFilter/h/max", 97.0);
        public static final double sMin = getDouble("vision/hsvFilter/s/min", 60.0);
        public static final double sMax = getDouble("vision/hsvFilter/s/max", 255.0);
        public static final double vMin = getDouble("vision/hsvFilter/v/min", 62.0);
        public static final double vMax = getDouble("vision/hsvFilter/v/max", 252.0);
        // Vision driving
        public static final double maxTargetAgeSecs = 2;

        // Vision low pass filter. Disabled pending more testing.
        public static final double goalLowPassAlpha = 1;

        /**
         * Numbers to tell us where the camera is:
         */
        public static final double cameraHeight = getDouble("vision/cameraHeight", 0.6); // height
                                                                                         // of lens
                                                                                         // off the
                                                                                         // ground,
                                                                                         // in
                                                                                         // metres
        public static final double cameraPitch = getDouble("vision/cameraPitch", 20); // degrees
                                                                                      // from
                                                                                      // horizontal,
                                                                                      // tilting
                                                                                      // upwards is
                                                                                      // +ve
        public static final double cameraRoll = getDouble("vision/cameraRoll", 0); // degrees from
                                                                                   // horizontal,
                                                                                   // counterclockwise
                                                                                   // is +ve

        // Displacement from center of robot, X is positive in the forward direction, Y
        // is positive to the left hand side.
        public static final Pose2d cameraPosition =
                new Pose2d(0, 0.13 * Config.constants.inchesToMetres, new Rotation2d(0));

        /**
         * Camera server constants
         * 
         * These are magic numbers to tell the Jevois which vision processor to load.
         */
        public static class camera {
            public static class resolution {
                public static final int width = 640;
                public static final int height = 252;
            }

            public static final int framesPerSecond = 60;
        }
    }

    /**
     * LED strip
     * 
     * Used to indicate the state of the robot (balls count, shoot count, issues).
     */
    public static class ledStrip {
        public static final boolean present = getBoolean("ledStrip/present", false);
        public static final int pwmPort = getInt("ledStrip/pwmPort", 3);
        public static final int numLEDs = getInt("ledStrip/numLEDs", 59);
        public static final int countdown = 15;
        public static final double brightnessPercentage = 0.4;
    }

    /**
     * Charting important values for post match debugging.
     */
    public static class charting {
        public static final boolean enabled = getBoolean("charting/enabled", true);
    }

    /**
     * These things are immutable
     */
    public static class constants {
        public static final double fullCircle = 360.0; // size of a full circle in internal units
                                                       // (degrees)
        public static final double halfCircle = 180.0; // size of a half circle in internal units
                                                       // (degrees)
        public static final double quarterCircle = 90.0; // size of a quarter circle in internal
                                                         // units (degrees)
        public static final double inchesToMetres = 0.0254;

    }

    /**
     * Update intervals
     */
    public static class intervals {
        public static final long executorCycleMSec = 20; // 50Hz
        public static final double dashboardUpdateSec = 0.5;
    }

    /**
     * Motor controller values
     */
    public static class motorController {
        public static final String talonSRX = "TalonSRX";
        public static final String sparkMAX = "SparkMAX";
        public static final String defaultType = talonSRX;

        /**
         * Current limits
         * 
         * Normally used by motor controllers
         */
        public static class currentLimit {
            public static final int defaultContinuousAmps = 30;
            public static final int defaultPeakAmps = 40;
        }
    }

    /**
     * Encoder values
     */
    public static class encoder {
        public static final double falconTicks = 2048; // Falon inbuilt encoders.
        public static final double SparkMAXTicks = 42; // SparkMAX inbuild encoders.
        public static final double s4tTicks = 1440; // ticks per rev.
        public static final double versaIntegratedTicks = 4096; // ticks per rotation
    }

    /**
     * User Interface
     */
    public static class ui {
        public static class joystick {
            // below this we deadband the value away
            public static final double deadbandMinValue = 0.05;
            // Should mock joysticks be created if joystick not plugged in?
            // Stops error messages when joysticks missing
            public static final boolean allowMock = getBoolean("ui/joystick/allowMock", false);
        }
    }

    /**
     * Robot constants
     */
    public static class robot {
        public static final double robotLength = 37.311 * constants.inchesToMetres;
        public static final double halfRobotLength = robotLength / 2;
    }

    /**
     * Field / Auto, see location.java for more info on the coordinate system.
     */
    public static class field {
        public static final double fieldLength = 629.25 * constants.inchesToMetres;
        public static final double fieldCentreX = fieldLength / 2;
        public static final double fieldWidth = 323.25 * constants.inchesToMetres;
        public static final double fieldCentreY = fieldWidth / 2;

        public static final double trenchDistBetweenBalls = 36 * constants.inchesToMetres;

        public static final double goalYPos = fieldWidth - 94.66 * constants.inchesToMetres;
        public static final double autoLineXPos = fieldLength - 3.05;
        public static final double opposingTrenchBallsYPos = 27.75 * constants.inchesToMetres;
        // TODO: Check meaurement for trench balls
        public static final double allianceTrenchBallsYPos = fieldWidth - opposingTrenchBallsYPos;

        // Film Pos
        public static final Pose2d startingFilmPosGoal = new Pose2d(
                fieldLength - autoLineXPos - robot.halfRobotLength, 2.01, new Rotation2d(0));
        public static final Pose2d redevousFilmBalls =
                new Pose2d(fieldLength - 7.5, 3.15, new Rotation2d(Math.toRadians(-30)));
        public static final Pose2d shootingFilmPos =
                new Pose2d(fieldLength - robot.halfRobotLength - 1, goalYPos,
                        new Rotation2d(Math.toRadians(0)));
        public static final Pose2d endingFilmPosGoal =
                new Pose2d(startingFilmPosGoal.getTranslation().getX() + 0.05,
                        startingFilmPosGoal.getTranslation().getY() + 0.05,
                        startingFilmPosGoal.getRotation());

        // Field Positions
        public static final Pose2d allianceTrenchFirstBall =
                new Pose2d(fieldCentreX + trenchDistBetweenBalls * 2,
                        allianceTrenchBallsYPos, new Rotation2d(Math.toRadians(0)));
        public static final Pose2d allianceTrenchSecondBall =
                new Pose2d(fieldCentreX + trenchDistBetweenBalls - 1,
                        allianceTrenchBallsYPos, new Rotation2d(Math.toRadians(0)));
        public static final Pose2d allianceTrenchThirdBall =
                new Pose2d(fieldCentreX, allianceTrenchBallsYPos,
                        new Rotation2d(Math.toRadians(0)));
        public static final Pose2d allianceTrenchFifthBall =
                new Pose2d(fieldCentreX - 65.53 * constants.inchesToMetres - 1,
                        allianceTrenchBallsYPos - 0.09, new Rotation2d(Math.toRadians(0)));

        // Auto Starting Positions
        public static final Pose2d autoLineAllianceTrench =
                new Pose2d(autoLineXPos - robot.halfRobotLength,
                        allianceTrenchBallsYPos, new Rotation2d(Math.toRadians(0)));
        public static final Pose2d autoLineGoal =
                new Pose2d(autoLineXPos - robot.halfRobotLength, goalYPos,
                        new Rotation2d(Math.toRadians(0)));
        public static final Pose2d autoLineOpposingTrench =
                new Pose2d(autoLineXPos - robot.halfRobotLength, 1.4,
                        new Rotation2d(Math.toRadians(180.0)));
        public static final Pose2d barrelRacingStartPos =
                new Pose2d(1.12, 5.981, new Rotation2d(Math.toRadians(0)));
    }

    /**
     * Server log sync parameters. Allows automatic uploading to a remote webserver.
     */
    public static class logging {
        public static final String flashDrive =
                Files.exists(Paths.get("/media/sda1")) ? "/media/sda1" : "/tmp";
        public static final String basePath = flashDrive; // log files (has to be inside web server)
        public static final String dataExtension = "data";
        public static final String dateExtension = "date";
        public static final String latestExtension = "latest";
        public static final String eventExtension = "event";

        public static class webserver {
            public static final String path = flashDrive; // where web server's data lives
            public static final int port = 5800; // port for graph/log web server
        }

        /**
         * Define parameters that govern the usage of the websocket logging server.
         */
        public static class liveloggingserver {
            public static final int port = 5803;
        }

        public static class rsync {
            // Port to forward to port 22 for transfering robot logs to pc over
            // rsync. Works around limited ports available while on the FMS.
            public static final int port = 5802;
            // Hostname of the robot.
            public static final String hostname = "roborio-3132-FRC.local";
        }
    }

    /**
     * Location on the roborio of the configuration file and config server details.
     */
    public static class config {
        public static final String homeDirectory = System.getProperty("user.home");
        public static final String configFilePath =
                Paths.get(homeDirectory, "config.txt").toString();
        public static final String robotNameFilePath =
                Paths.get(homeDirectory, "robotname.txt").toString();

        /**
         * Allow editing of config file via webserver.
         */
        public static class webserver {
            public static final String root = "/home/lvuser/deploy/www";
            public static final int port = 5801;
        }
    }

    // Only implementation from here onwards.

    private final static ConfigReader reader = new ConfigReader();

    /**
     * Needs to be called after the config is loaded to write out an example config
     * file and to print out details about the config file.
     */
    public static void finishLoadingConfig() {
        reader.finishLoadingConfig();
    }

    protected static String getMotorControllerType(final String parameterName,
            final String defaultValue) {
        return reader.getMotorControllerType(parameterName, defaultValue);
    }

    protected static int getInt(final String key, final int defaultValue) {
        return reader.getInt(key, defaultValue);
    }

    protected static double getDouble(final String key, final double defaultValue) {
        return reader.getDouble(key, defaultValue);
    }

    protected static PIDF getPIDF(final String prefix, final PIDF pidf) {
        return reader.getPIDF(prefix, pidf);
    }

    protected static boolean getBoolean(final String key, final boolean defaultValue) {
        return reader.getBoolean(key, defaultValue);
    }

    protected static String getString(final String key, final String defaultValue) {
        return reader.getString(key, defaultValue);
    }

    protected static int[] getIntArray(final String key, final int[] defaultValue) {
        return reader.getIntArray(key, defaultValue);
    }
}
