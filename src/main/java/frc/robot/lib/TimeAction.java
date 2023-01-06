package frc.robot.lib;

/**
 * Encapsulates the two ways we can set a delay in the controller
 * Either DELAY_UNTIL which is an absolute time
 * Or DELAY_DELTA which is relative to when we start executing the state
 */
public class TimeAction {
    public final Type type;
    public final double seconds;

    public TimeAction(Type type, double seconds) {
        this.type = type;
        this.seconds = seconds;
    }

    public enum Type {
        DELAY_DELTA, DELAY_UNTIL
    }

    @Override
    public String toString() {
        return String.format("%s value: %f", type, seconds);
    }

    /**
     * Calculates the time at which the controller can move onto the next state
     * 
     * @param currentTime
     * @return the time at which the controller can move onto the next state
     */
    public double calculateEndTime(double currentTime) {
        switch (type) {
            case DELAY_DELTA:
                return currentTime + seconds;
            case DELAY_UNTIL:
                return seconds;
            default:
                throw new RuntimeException("Unsupported time action " + type.toString());
        }
    }
}
