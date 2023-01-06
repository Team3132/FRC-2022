package org.strongback.hardware;



import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;

/**
 * Wrapper for WPILib {@link DoubleSolenoid}. Adds extra states based on time to account for
 * the solenoid moving into place.
 *
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.DoubleSolenoid
 */
final class HardwareDoubleSolenoid implements Solenoid {
    private final DoubleSolenoid solenoid;
    private Position position;
    private double endTime; // time the solenoid will finish moving
    private final Clock clock;
    private final double timeOut; // time in seconds to move the solenoid out
    private final double timeIn; // time in seconds to move the solenoid in
    private boolean inverted = false;

    HardwareDoubleSolenoid(DoubleSolenoid solenoid, double timeOut, double timeIn) {
        assert solenoid != null;
        this.solenoid = solenoid;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
        switch (solenoid.get()) {
            case kForward:
                position = inverted ? Position.RETRACTED : Position.EXTENDED;
                break;
            case kReverse:
                position = inverted ? Position.EXTENDED : Position.RETRACTED;
                break;
            default:
                position = Position.STOPPED;
        }
    }

    @Override
    public String toString() {
        return position.name().toLowerCase();
    }

    @Override
    public HardwareDoubleSolenoid setPosition(Position position) {
        if (position == this.position) {
            return this; // No change
        }
        double now = clock.currentTime();
        switch (position) {
            case EXTENDED:
                endTime = now + timeOut;
                solenoid.set(inverted ? Value.kReverse : Value.kForward);
                break;
            case RETRACTED:
                endTime = now + timeIn;
                solenoid.set(inverted ? Value.kForward : Value.kReverse);
                break;
            case STOPPED:
                endTime = now;
                solenoid.set(Value.kOff);
                break;
            default:
                throw new RuntimeException(
                        "Invalid Position " + position + " for HardwareDoubleSolenoid");
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
    public HardwareDoubleSolenoid setInverted(boolean inverted) {
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
