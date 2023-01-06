package frc.robot.lib;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Config;
import org.strongback.components.Clock;

/**
 * This class provides a history of where the robot was. It is used by the vision system to
 * accurately determine where the robot was,
 * so that the exact field position of a target can be determined. We keep the history for a fixed
 * number of seconds, being the maximum
 * time that we may be asked for the previous location.
 * 
 * The history is stored in a circular buffer, so older values are overwritten with newer values.
 */
public class LocationHistory {
    private final Pose2d[] historyArray = new Pose2d[HISTORY_LENGTH];
    private int nextWriteIndex;
    private double latestLocationTime = 0;
    private static final int HISTORY_LENGTH =
            Config.location.history.memorySecs * Config.location.history.cycleSpeedHz;

    public LocationHistory(Clock clock) {
        setInitial(new Pose2d(0, 0, new Rotation2d(0)), clock.currentTime());
    }

    /**
     * Get location as a position at the specified time
     * 
     * @param timeSec time that we wish to know position
     * @return position at specified time accurate to the nearest 5ms or a position with 0 values if
     *         there is no existing position yet
     */
    public Pose2d getLocation(double timeSec) {
        if (timeSec < latestLocationTime - Config.location.history.memorySecs) {
            // too long ago.
            return historyArray[nextWriteIndex]; // oldest value we have!
        }
        timeSec = Math.min(timeSec, latestLocationTime); // Make sure the time isn't in the future.
        return historyArray[secondsToIndex(timeSec)];
    }

    /**
     * Set the initial values for the history array. We do this to ensure that all previous times
     * are set to the initial value.
     * 
     * @param filler the location to fill the array with
     */
    public void setInitial(Pose2d filler, double timeSec) {
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            historyArray[i] = filler;
        }
        nextWriteIndex = secondsToIndex(timeSec);
        latestLocationTime = timeSec;
    }

    /**
     * Add a location to the history. We should include the averaging of the two values as we move
     * between them.
     * That's better than just the new value for all points. I.e. a movement rather than a jump.
     * 
     * @param position position of the robot, including the time that the robot was at that
     *        position.
     */
    public void addLocation(Pose2d pose, double timeSec) {
        // Make a deep copy in case the original one is overwritten.
        Pose2d p = new Pose2d(pose.getX(), pose.getY(), pose.getRotation());
        int index = secondsToIndex(timeSec);
        while (nextWriteIndex != (index + 1) % HISTORY_LENGTH) {
            historyArray[nextWriteIndex] = p;
            nextWriteIndex++;
            nextWriteIndex %= HISTORY_LENGTH;
        }
        latestLocationTime = timeSec;
    }

    /**
     * Converts a time in milliseconds into an index
     * 
     * @param timeSec desired time in milliseconds
     * @return index to be called in historyArray
     */
    private int secondsToIndex(double timeSec) {
        return ((int) (Math.round(timeSec * Config.location.history.cycleSpeedHz)))
                % HISTORY_LENGTH;
    }
}
