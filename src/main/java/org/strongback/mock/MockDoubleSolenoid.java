package org.strongback.mock;



import org.strongback.Strongback;
import org.strongback.components.Clock;
import org.strongback.components.Solenoid;
import org.strongback.hardware.Hardware;

/**
 * Mock version of a single solenoid.
 * Everything but the solenoid itself.
 *
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.DoubleSolenoid
 */
final class MockDoubleSolenoid implements MockSolenoid {
    private Position position;
    private double endTime; // time the solenoid will finish moving
    private final Clock clock;
    private final double timeOut; // time in seconds to move the solenoid out
    private final double timeIn; // time in seconds to move the solenoid in

    MockDoubleSolenoid(double timeOut, double timeIn) {
        this.position = Position.STOPPED; // unknown
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.clock = Strongback.timeSystem();
        this.endTime = clock.currentTime();
    }

    @Override
    public String toString() {
        return "position = " + position;
    }

    @Override
    public MockDoubleSolenoid setPosition(Position position) {
        if (position == this.position) {
            return this; // No change
        }
        double now = clock.currentTime();
        switch (position) {
            case EXTENDED:
                endTime = now + timeOut;
                break;
            case RETRACTED:
                endTime = now + timeIn;
                break;
            case STOPPED:
                endTime = now;
                break;
            default:
                throw new RuntimeException(
                        "Invalid Position " + position + " for MockDoubleSolenoid");
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
    public MockDoubleSolenoid setInverted(boolean inverted) {
        return this;
    }
}
