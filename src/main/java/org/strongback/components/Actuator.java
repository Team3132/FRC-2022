package org.strongback.components;



import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;

/**
 * An actuator is a device that moves a physical object. How it moves we don't define.
 * 
 * @author Rex di Bona
 *         An actuator has a target (either binary for a solenoid, analog for a servo).
 *         An actuator takes some time to move to its final position. How much depends on the
 *         implementation.
 *         An actuator may, or may not, be able to update its target whilst it is moving.
 *         To check if a target has been updated check with if
 *         (actuator.setTarget(target).getTarget() == target)
 *         Booleans are for isMoving and isAtTarget to determine the state of the actuator.
 */
@ThreadSafe
public interface Actuator extends Requirable {
    public Actuator setTargetPosition(double target);

    public double getTargetPosition();

    public default double getCurrentPosition() {
        return getTargetPosition(); // if there is no difference
    }

    public boolean isAtTarget();

    public boolean isMoving();
}
