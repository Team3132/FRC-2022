package frc.robot.lib;



import edu.wpi.first.wpilibj.DigitalInput;
import org.strongback.Executable;

/**
 * Similar to a {@link DigitalInput}, except the get method is time based - ie the digital input has
 * to be held for a
 * certain time to register as triggered.
 */
public class TimedDigitalInput extends DigitalInput implements Executable {
    private long triggeredTime = 0;
    private boolean prevTriggered = false;
    private long triggerStartTime = 0;
    private boolean isTriggeredByTime = false;


    /**
     * Create an instance of a Digital Input class. Creates a digital input given a channel.
     *
     * @param channel the DIO channel for the digital input 0-9 are on-board, 10-25 are on the MXP
     */
    public TimedDigitalInput(int channel) { // constructor should do nothing
        super(channel);
    }

    /**
     * Sets the time it takes for this sensor to register as triggered
     * 
     * @param triggeredTime time it takes to register as triggered
     * @return this
     */
    public DigitalInput setTriggeredTime(double triggeredTime) {
        this.triggeredTime = (long) (triggeredTime * 1000);
        return this;
    }

    /**
     * Retreieves the value set by {@link #setTriggeredTime(double)}
     * 
     * @return the time it takes to register the sensor as being triggered
     */
    public double getTriggeredTime() {
        return (double) triggeredTime / 1000.0;
    }

    public boolean get() {
        return !super.get() && isTriggeredByTime;
    }

    @Override
    public void execute(long timeInMillis) {
        long now = System.currentTimeMillis();
        if (!super.get()) {
            if (!prevTriggered) {
                triggerStartTime = now;
            }
            if (now - triggerStartTime >= triggeredTime) {
                isTriggeredByTime = true;
            }
            prevTriggered = true;
            return;
        }
        prevTriggered = false;
        isTriggeredByTime = false;
    }
}
