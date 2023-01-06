package org.strongback.hardware;



import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;

/**
 * Wrapper for WPILib {@link Solenoid}. Adds extra states based on time to account for
 * the solenoid moving into place.
 *
 * @author Rex di Bona
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.Solenoid
 */
final class HardwareSingleSolenoid implements Solenoid {
    private final edu.wpi.first.wpilibj.Solenoid solenoid;
    private Position position;
    private double endTime; // time the solenoid will finish moving
    private final Clock clock;
    private final double timeOut; // time in seconds to move the solenoid out
    private final double timeIn; // time in seconds to move the solenoid in
    private boolean inverted = false;

    HardwareSingleSolenoid(edu.wpi.first.wpilibj.Solenoid solenoid, double timeIn, double timeOut) {
        assert solenoid != null;
        this.solenoid = solenoid;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
        position = ((solenoid.get() ^ inverted) ? Position.EXTENDED : Position.RETRACTED);
    }

    @Override
    public String toString() {
        return position.name().toLowerCase();
    }

    @Override
    public HardwareSingleSolenoid setPosition(Position position) {
        if (position == this.position) {
            return this; // No change
        }
        double now = clock.currentTime();
        switch (position) {
            case EXTENDED:
                endTime = now + timeOut;
                solenoid.set(inverted ? false : true);
                break;
            case RETRACTED:
                endTime = now + timeIn;
                solenoid.set(inverted ? true : false);
                break;
            default:
                throw new RuntimeException(
                        "Invalid Position " + position + " for HardwareSingleSolenoid");
        }
        this.position = position;
        return this;
    }

    @Override
    public Position getPosition() {
        if (isStopped()) {
            // Finished moving, return the set position.
            return position;
        }
        switch (position) {
            case EXTENDED:
                return Position.EXTENDING;
            case RETRACTED:
                return Position.RETRACTING;
            default:
                return Position.STOPPED;
        }
    }

    @Override
    public boolean isStopped() {
        return (clock.currentTime() >= endTime);
    }

    @Override
    public HardwareSingleSolenoid setInverted(boolean inverted) {
        if (this.inverted == inverted) {
            return this; // No change
        }
        // Invert the internal state
        this.inverted = inverted;
        if (position == Position.EXTENDED) {
            position = Position.RETRACTED;
        } else {
            position = Position.EXTENDED;
        }
        return this;
    }
}
