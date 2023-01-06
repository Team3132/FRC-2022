package frc.robot.subsystems;

import static frc.robot.lib.LowPassFilter.filterValues;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Config;
import frc.robot.interfaces.Jevois;
import frc.robot.interfaces.Location;
import frc.robot.interfaces.Vision;
import frc.robot.lib.MathUtil;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import java.io.IOException;
import org.strongback.components.Clock;

public class VisionImpl extends Subsystem implements Vision, Runnable {
    private Jevois jevois;
    private Location location;
    private Clock clock;
    private double visionHMin, visionSMin, visionVMin;
    private double visionHMax, visionSMax, visionVMax;
    private TargetDetails lastSeenTarget = new TargetDetails(); // the target we are shooting into
    private boolean connected = false;

    public VisionImpl(Jevois jevois, Location location, Clock clock,
            double visionHMin, double visionSMin, double visionVMin, double visionHMax,
            double visionSMax,
            double visionVMax) {
        super("Vision");
        this.jevois = jevois;
        this.location = location;
        this.clock = clock;
        this.visionHMin = visionHMin;
        this.visionSMin = visionSMin;
        this.visionVMin = visionVMin;
        this.visionHMax = visionHMax;
        this.visionSMax = visionSMax;
        this.visionVMax = visionVMax;

        Chart.register(() -> isConnected(), "%s/connected", name);
        Chart.register(() -> lastSeenTarget.pose.getX(), "%s/curX", name);
        Chart.register(() -> lastSeenTarget.pose.getY(), "%s/curY", name);
        Chart.register(() -> lastSeenTarget.pose.getRotation().getDegrees(), "%s/heading",
                name);
        // Chart.register(() -> clock.currentTime() - lastSeenTarget.seenAtSec, "%s/seenAt", name)
        Chart.register(() -> lastSeenTarget.imageTimestamp, "%s/seenAtSec", name);
        Chart.register(() -> lastSeenTarget.targetFound, "%s/targetFound", name);
        Chart.register(() -> lastSeenTarget.distance, "%s/distance", name);
        Chart.register(() -> lastSeenTarget.angle.getDegrees(), "%s/angle", name);

        // Start reading from the Jevois camera.
        (new Thread(this)).start();
    }

    /**
     * Return the details of the last target seen. Let the caller decide if the data
     * is too old/stale.
     */
    @Override
    public synchronized TargetDetails getTargetDetails() {
        return lastSeenTarget;
    }

    /**
     * Main loop. Runs in its own thread so it can block.
     */
    @Override
    public void run() {
        try {
            while (true) {
                debug("Waiting for the camera server to start up");
                Thread.sleep(5000);
                doProcessing();
            }
        } catch (InterruptedException e) {
            warning("InterruptedException, likely shutting down");
        }
        connected = false;
    }

    /**
     * Try to connect to and process images from a camera.
     * 
     * @throws InterruptedException
     */
    public void doProcessing() throws InterruptedException {
        debug("Starting to read from Jevois camera\n");
        try {
            // Attempt to detect if there is a camera plugged in. It will throw an exception
            // if not.
            debug(jevois.issueCommand("info"));
            connected = true;

            // Update the HSV filter ranges from the config values.

            jevois.issueCommand(String.format("setHSVMin %.0f %.0f %.0f", visionHMin,
                    visionSMin, visionVMin));
            jevois.issueCommand(String.format("setHSVMax %.0f %.0f %.0f", visionHMax,
                    visionSMax, visionVMax));
            /*
             * jevois.issueCommand(String.format("position %.0f %.0f %.0f",
             * Config.vision.cameraHeight,
             * Config.vision.cameraPitch,
             * Config.vision.cameraRoll));
             */

            while (true) {
                processLine(jevois.readLine());
            }
        } catch (IOException e) {
            exception("Failed to read from jevois, aborting vision processing\n", e);
            connected = false;
        }
    }

    /**
     * Parses a line from the vision and calculates the target position on the field
     * based on where the robot was at that time. No line is read when a target
     * isn't seen.
     * 
     * Example line: D3 1.0 true 12.23 20.2 9.1 FIRST
     * 
     * Line format: D3 <imageAge> <found> <distance> <angle> <skew> FIRST
     * 
     * Where: D3: static string to indicate that this is a found vision target.
     * imageAge: time in seconds since image taken
     * found: boolean for if goal was detected
     * distance: horizontal distance from goal in metres
     * angle: degrees
     * skew: degrees
     * FIRST: static string.
     */
    private void processLine(String line) {
        // Split the line on whitespace.
        // "D3 timestamp found distance angle FIRST"
        String[] parts = line.split("\\s+");

        if (!parts[0].equals("D3")) {
            info("Ignoring non-vision target line: %s", line);
            return;
        }
        // Logger.debug("Vision::processLine(%s)\n", line);
        TargetDetails newTarget = new TargetDetails();
        newTarget.targetFound = Boolean.parseBoolean(parts[2]);

        if (!Boolean.parseBoolean(parts[2]))
            return; // If target is not detected, ignore the line

        // A target was seen, update the last seen target details so that it can be
        // used in vision routines.

        newTarget.imageTimestamp = clock.currentTime() - Double.parseDouble(parts[1]);
        newTarget.distance = Double.parseDouble(parts[3]); // Metres
        newTarget.angle = Rotation2d.fromDegrees(-Double.parseDouble(parts[4]));
        newTarget.skew = Rotation2d.fromDegrees(Double.parseDouble(parts[5]));
        // Find out where the robot was when this image was taken.
        Pose2d robotPose = location.getHistoricalPose(newTarget.imageTimestamp);
        // Take the coordinates as relative to the camera.
        Pose2d cameraPose =
                robotPose.plus(new Transform2d(Config.vision.cameraPosition.getTranslation(),
                        Config.vision.cameraPosition.getRotation()));
        // Calculate where the target must have been and what angle it was facing.
        newTarget.pose = MathUtil.relativeToAbsolute(cameraPose, newTarget.angle,
                newTarget.distance, newTarget.skew);

        if (lastSeenTarget.isValid(clock.currentTime())) {
            // Apply a low pass filter so that the target doesn't jump around.
            newTarget.pose = new Pose2d(
                    filterValues(newTarget.pose.getX(), lastSeenTarget.pose.getX(),
                            Config.vision.goalLowPassAlpha),
                    filterValues(newTarget.pose.getY(), lastSeenTarget.pose.getY(),
                            Config.vision.goalLowPassAlpha),
                    new Rotation2d(Math.toRadians(
                            filterValues(newTarget.pose.getRotation().getDegrees(),
                                    lastSeenTarget.pose.getRotation().getDegrees(),
                                    Config.vision.goalLowPassAlpha))));
        }

        synchronized (this) {
            lastSeenTarget = newTarget;
        }
        // log.sub("Vision: Updated target %s", lastSeenTarget);

    }

    @Override
    public void updateDashboard() {
        double lockAgeSec = (clock.currentTime() - lastSeenTarget.imageTimestamp);
        double angle = 0, distance = 0, skew = 0;
        if (lastSeenTarget.isValid(clock.currentTime())) {
            Pose2d robotPose = location.getCurrentPose();
            Pose2d targetPose = lastSeenTarget.pose;
            angle = MathUtil.absoluteToRelativeAngle(robotPose, targetPose).getDegrees();
            distance = MathUtil.distanceBetween(robotPose, targetPose);
            // With zero skew, as the target faces the robot, it would be 180 degrees out from the
            // robot, so 180 degrees needs to be subtracted off.
            skew = robotPose.getRotation().minus(targetPose.getRotation())
                    .minus(Rotation2d.fromDegrees(180)).getDegrees();
        }

        SmartDashboard.putBoolean("Vision camera connected", connected);
        SmartDashboard.putNumber("Vision distance to target", distance);
        SmartDashboard.putNumber("Vision lockAgeSec", lockAgeSec);
        SmartDashboard.putNumber("Vision angle", angle);
        SmartDashboard.putBoolean("Vision targetFound", lastSeenTarget.targetFound);
        SmartDashboard.putBoolean("Vision is Valid", lastSeenTarget.isValid(clock.currentTime()));
        SmartDashboard.putNumber("Vision Skew", skew);

    }

    /**
     * @return hasConnection returns the current status of the connection to the
     *         external vision processor
     */
    @Override
    public boolean isConnected() {
        return connected;
    }
}
