package frc.robot.subsystems;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Config;
import frc.robot.interfaces.DriveTelemetry;
import frc.robot.interfaces.Location;
import frc.robot.lib.LocationHistory;
import frc.robot.lib.MathUtil;
import frc.robot.lib.NavXGyroscope;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import org.strongback.components.Clock;
import org.strongback.components.Gyroscope;

/**
 *	Location Subsystem.
 *
 * The location subsystem is responsible for tracking the location of the robot on the field.
 * It does this through reading the encoders and the gyro and plotting where the robot has moved.
 * This is guaranteed lossy, as we cannot update often enough to track every slight movement.
 * 
 * Coordinate system
 * -----------------
 * 
 * The field orientation has the Y axis across the driver's station and
 * the X axis between the alliance's ends.
 * Positive for Y is from left to right across the driver's station,
 * and positive for X is towards the opposite alliance's end from the driver's station.
 *  
 * X is along the "horizontal line", and Y is the "vertical" line with 
 * (0,0) at the bottom left corner of the diagram below.
 * 
 * @formatter:off
 *                              Y +ve
 *                            ^ 
 *                            |
 *                            | * * * * * * * * * * * * * * * * * * * *
 *                            |                                       *
 *                            |                                       *
 *                            |                                       *
 *    Our drivers' stations   |                                       *   Other alliance driver stations
 *                            |                                       *
 *                            |                                       *
 *                            |                                       *
 *                            |                                       *
 *                   X -ve <--0-------------------------------------------> X +ve
 *                            |
 *                            v
 *                              Y -ve
 * 
 * Heading angles:
 *                            ^  90 degrees 
 *                            |
 *                            |
 *  180/-180 degrees <--------+-------->   0 degrees
 *                            |
 *                            |
 *                            v  -90 degrees
 * 
 * @formatter:on
 * 
 * The location subsystem works in headings and bearings. When the robot starts we call the forward direction heading/bearing 0.
 * 
 * A heading is the angle that the robot has moved from the initial angle.
 * It can range from -infinity to infinity, so full circles change the heading by 360 degrees each time.
 * Increasing positive angles are the robot turning anticlockwise.
 * 
 * A bearing is the relative angle from the initial angle. In nautical terms it is the absolute bearing.
 * A bearing is constrained to the range -180 to 180 degrees.
 */
public class LocationImpl extends Subsystem implements Location {
    private DriveTelemetry telemetry;
    private Gyroscope gyro;
    private Clock clock; // Source of time.
    /**
     * current the current Position:
     * x: current left/right offset from 0 at start point (metres). +x is forward.
     * y: current forward/backward offset from 0 at start point (metres). +y is left.
     * h: current heading from facing directly down the field (degrees). +ve is anticlockwise.
     * s: current speed (metres/sec)
     * t: time of the last update. (seconds)
     */
    private Pose2d desired; // Where the auto driving hopes the robot is at.
    private LocationHistory history; // history of points we have been on the field.
    private boolean debug = false;


    // Odometry class for tracking robot pose
    private final DifferentialDriveOdometry odometry;

    /**
     * Constructor. The location subsystem obtains inputs from the drivebase and from the gyro.
     *
     * It performs inverse kinematics on these to determine where the robot is currently on the
     * field.
     * We can override this with the setCurrent method which allows some other subsystem to
     * determine
     * where we are on the field, and then to inform the location subsystem of that fact.
     * 
     * @param name The name to be used in the logs
     * @param leftDistance The distance travelled by the left wheel (in metres)
     * @param gyro The gyro to get angles
     * @param log The log to store debug and other logging messages
     */
    public LocationImpl(DriveTelemetry telemetry, Gyroscope gyro, Clock clock) {
        super("Location"); // always present!
        this.telemetry = telemetry;
        odometry = new DifferentialDriveOdometry(Rotation2d.fromDegrees(gyro.getAngle()),
                this.telemetry.getLeftDistance(), this.telemetry.getRightDistance());

        this.gyro = gyro;
        this.clock = clock;
        this.history = new LocationHistory(clock);
        this.desired = new Pose2d(0, 0, new Rotation2d(0));

        Chart.register(() -> odometry.getPoseMeters().getX(), "%s/actual/x", name);
        Chart.register(() -> odometry.getPoseMeters().getY(), "%s/actual/y", name);
        Chart.register(() -> odometry.getPoseMeters().getRotation().getDegrees(), "%s/actual/a",
                name);
        Chart.register(() -> desired.getX(), "%s/desired/x", name);
        Chart.register(() -> desired.getY(), "%s/desired/y", name);
        Chart.register(() -> desired.getRotation().getDegrees(), "%s/desired/a", name);

        // Enable this subsystem by default.
        enable();
    }

    /**
     * Set the current location. This allows a subsystem to override the location and force the
     * location to a particular point.
     * In particular the start location should be set as accurately as possible, so the robot knows
     * where it starts on the field
     * 
     * @param pose The current location.
     */
    @Override
    public void setCurrentPose(Pose2d pose) {
        Log.debug("Location", "%s: resetting to: %s", name, pose.toString());
        ((NavXGyroscope) gyro).setAngle(pose.getRotation().getDegrees());
        history.setInitial(getCurrentPose(), clock.currentTime());
        telemetry.setLeftDistance(0);
        telemetry.setRightDistance(0);
        odometry.resetPosition(Rotation2d.fromDegrees(gyro.getAngle()), 0, 0, pose);
    }

    /**
     * Resets the odometry to the specified pose.
     *
     * @param pose The pose to which to set the odometry.
     */
    /*
     * public void resetOdometry(Pose2d pose) {
     * resetEncoders();
     * odometry.resetPosition(pose, Rotation2d.fromDegrees(getHeading()));
     * }
     */

    /**
     * Return the location on the field at the current time.
     * 
     * @return the current location
     */
    @Override
    public Pose2d getCurrentPose() {
        return odometry.getPoseMeters();
    }

    /**
     * Set the desired location.
     * Usually used for the automatic driving to log where the robot should be.
     * 
     * @param position The current location.
     */
    @Override
    public void setDesiredPose(Pose2d pose) {
        desired = pose;
    }

    /**
     * Return the location on the field at the specified time
     * 
     * @param timeSec The time (in seconds) for which we wish to obtain the location
     * @return The location at the specified time
     */
    public Pose2d getHistoricalPose(double timeSec) {
        return history.getLocation(timeSec);
    }

    @Override
    public void execute(long timeInMillis) {
        update();
    }

    /**
     * Update the robots location on the field.
     *
     * Gets the movement of the robot since the last update and the heading of
     * that movement and calculate the new position of the robot.
     * 
     * It also adds this new value to the LocationHistory class which holds
     * the history of where the robot has been on the field.
     *
     * y += distance * sin(heading)
     * x += distance * cos(heading)
     * 
     * If the encoder deltas are different then we have been moving in an arc.
     * Assume that the arc is smooth and has straight entry and exit segments to calculate the new X
     * and Y locations.
     * The gyro is assumed correct, and is used to update the Heading.
     * 
     * First cut. assume we have moved the average distance of both left and right movements at
     * the average Heading between the start and the end Heading.
     * This is not exact, but since we sample fast enough it is close enough for tracking how the
     * robot is moving.
     * 
     * One problem is that very fast sampling means we will have zero movements for some samples.
     * This causes problems with the current speed value.
     */
    @Override
    public void update() {
        if (!enabled)
            return; // The location subsystem should never be disabled.

        // odometry expects degrees and metres
        odometry.update(Rotation2d.fromDegrees(gyro.getAngle()),
                telemetry.getLeftDistance(),
                telemetry.getRightDistance());

        double newTime = clock.currentTime(); // Time of last update
        history.addLocation(getCurrentPose(), newTime);

        if (debug) {
            Log.debug("%s: %s", name, getCurrentPose().toString());
        }
    }

    /**
     * Return the heading of the robot
     * 
     * @return Returns the cumulative heading of the robot. This is a continuous heading,
     *         so it moves from 360 to 361 degrees.
     */
    @Override
    public double getHeading() {
        return gyro.getAngle();
    }

    /**
     * Return the robot heading restricted to -180 to 180 degrees. This is discontinuous,
     * so 180 leads to -179.9999 and -180 leads to 179.9999
     * 
     * @return the current bearing of the robot
     */
    @Override
    public double getBearing() {
        return (MathUtil.normalise(getHeading(), Config.constants.fullCircle));
    }

    /**
     * Return the robot heading restricted from 0 to 1 in fractions of a circle.
     * 
     * @return the Heading from 0 straight ahead to .5 backwards to 1 straight ahead.
     */
    public double getUnitHeading() {
        double bearing = getBearing();
        if (bearing < 0.0) {
            bearing += Config.constants.fullCircle;
        }
        return bearing / Config.constants.fullCircle;
    }

    @Override
    public void resetHeading() {
        gyro.zero();
        // Update the saved state.
        odometry.update(new Rotation2d(Math.toRadians(gyro.getAngle())),
                telemetry.getLeftDistance(), telemetry.getRightDistance());
    }

    @Override
    public void updateDashboard() {
        Pose2d pose = getCurrentPose();
        SmartDashboard.putNumber("Location X", pose.getX());
        SmartDashboard.putNumber("Location Y", pose.getY());
        SmartDashboard.putNumber("Location A", pose.getRotation().getDegrees());
    }

}
