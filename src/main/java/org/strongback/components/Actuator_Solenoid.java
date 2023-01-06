package org.strongback.components;

/**
 * This interface extends an actuator to be a solenoid.
 * A solenoid is an actuator that moves to only two positions (extended and retracted)
 * and takes time to do the movement.
 * 
 * The constructor for a timed solenoid needs to have feedback to know if it has finished moving.
 * 
 * @author Rex di Bona
 *
 */
public interface Actuator_Solenoid extends Actuator {

    public default Actuator_Solenoid extend() {
        setTargetPosition(1.0);
        return this;
    }

    public default Actuator_Solenoid retract() {
        setTargetPosition(0.0);
        return this;
    }

    public default boolean isExtended() {
        return !this.isMoving() && (this.getCurrentPosition() == this.getTargetPosition())
                && (this.getTargetPosition() == 1.0);
    }

    public default boolean isRetracted() {
        return !this.isMoving() && (this.getCurrentPosition() == this.getTargetPosition())
                && (this.getTargetPosition() == 0.0);
    }

    public default boolean isStopped() {
        return !this.isMoving();
    }
}
