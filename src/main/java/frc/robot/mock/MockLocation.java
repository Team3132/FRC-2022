package frc.robot.mock;



import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Config;
import frc.robot.interfaces.Location;
import frc.robot.lib.MathUtil;

public class MockLocation implements Location {

    Pose2d here = new Pose2d(0, 0, new Rotation2d(0));
    double heading = 0;

    @Override
    public void execute(long timeInMillis) {}

    @Override
    public void setCurrentPose(Pose2d pose) {
        here = pose;
    }

    @Override
    public Pose2d getCurrentPose() {
        return here;
    }

    @Override
    public void setDesiredPose(Pose2d pose) {}

    @Override
    public Pose2d getHistoricalPose(double timeSec) {
        // Build the position based on the time.
        return new Pose2d(10 * timeSec, 100 * timeSec, new Rotation2d(timeSec % 360));
    }

    @Override
    public void update() {}

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public double getBearing() {
        return MathUtil.normalise(heading, Config.constants.fullCircle);
    }

    @Override
    public void resetHeading() {
        heading = 0;
    }

    @Override
    public void enable() {}

    @Override
    public void disable() {}

    public void setHeading(double heading) {
        this.heading = heading;
    }


}
