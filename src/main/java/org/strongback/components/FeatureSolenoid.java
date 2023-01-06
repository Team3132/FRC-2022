
package org.strongback.components;



import java.util.HashMap;
import java.util.Map;
import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;
import org.strongback.components.Solenoid.Position;

/**
 * A wrapped solenoid that controls a feature. Allows more natural language
 * to be used (enabled + disabled) instead of a solenoid which only knows
 * retracted and extended.
 * 
 * To change what enabled is mapped to, invert the underlying solenoid.
 *
 * @author Mark Waldron (mark.r.waldron@gmail.com)
 */
@ThreadSafe
public class FeatureSolenoid implements Requirable {
    private final Solenoid solenoid;
    static private final Map<Position, Mode> positionToMode = createReverseMapping();

    /**
     * The status of the feature.
     */
    public static enum Mode {
        /** The feature has been fully enabled */
        ENABLED(Position.EXTENDED),
        /** The feature has been fully disabled */
        DISABLED(Position.RETRACTED),
        /** The feature is being enabled */
        ENABLING(Position.EXTENDING),
        /** The feature is being disabled */
        DISABLING(Position.RETRACTING);

        Position position;

        Mode(Position position) {
            this.position = position;
        }
    }

    /**
     * Wrap a solenoid to provide enable() and disable() convenience methods.
     * 
     * @param solenoid the solenoid that has the extend() and retract() methods to change into
     *        enable() and disable()
     */
    public FeatureSolenoid(Solenoid solenoid) {
        this.solenoid = solenoid;
    }

    public FeatureSolenoid setMode(Mode mode) {
        solenoid.setPosition(mode.position);
        return this;
    }

    /**
     * Converts the solenoids position into enabled/disabled.
     * 
     * @return the current mode.
     */
    public Mode getMode() {
        return positionToMode.getOrDefault(solenoid.getPosition(), Mode.DISABLED);
    }

    /**
     * Indicates if the solenoid has finished moving.
     * 
     * @return true if the solenoid has finished moved to its last
     *         set position.
     */
    public boolean isInPosition() {
        return solenoid.isInPosition();
    }

    /**
     * Is this feature enabled?
     * A feature can be neither enabled or disabled if the solenoid is still moving.
     * 
     * @return true if enabled.
     */
    public boolean isEnabled() {
        return getMode().equals(Mode.ENABLED);
    }

    /**
     * Is this feature disabled?
     * A feature can be neither enabled or disabled if the solenoid is still moving.
     * 
     * @return true if disabled.
     */
    public boolean isDisabled() {
        return getMode().equals(Mode.DISABLED);
    }

    /**
     * String version of the mode.
     * 
     * @return string of "enabled", "disabled", etc
     */
    public String getAsString() {
        return getMode().toString().toLowerCase();
    }

    /**
     * Creates a lookup to convert between position to its equivalent mode.
     * 
     * @return a map of positions to modes.
     */
    static private Map<Position, Mode> createReverseMapping() {
        Map<Position, Mode> positionToMode = new HashMap<>();
        // Create the reverse mapping so we can quickly look up how to go
        // from Positon to Mode.
        positionToMode.put(Position.STOPPED, Mode.DISABLED); // No equivalent.
        for (Mode mode : Mode.values()) {
            positionToMode.put(mode.position, mode);
        }
        return positionToMode;
    }
}
