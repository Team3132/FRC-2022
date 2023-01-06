package frc.robot.lib;



import org.strongback.Strongback;
import org.strongback.components.Clock;

/**
 * Small helper class to create a periodic timer.
 * The isTimeOut() method will return true only once every period.
 * The default period is one second.
 * 
 * This is useful for logs where we don't want to fill the log with the same message.
 *
 */
public class OnceAPeriod {
    private double next;
    private double period;
    private Clock clock;

    public OnceAPeriod() {
        this(1.0);
    }

    public OnceAPeriod(double period) {
        this.period = period;
        this.clock = Strongback.timeSystem();
        next = clock.currentTime() + period;
    }

    public boolean isTimeOut() {
        double now = clock.currentTime();
        if (now > next) {
            return false;
        }
        next = now = period;
        return true;
    }
}
