/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.components;



import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;

/**
 * A solenoid is a device that can be extended and retracted.
 *
 * @author Zach Anderson
 */
@ThreadSafe
public interface Solenoid extends Requirable {

    /**
     * The direction of the solenoid.
     */
    static enum Position {
        /** The solenoid is fully extended. */
        EXTENDED,
        /** The solenoid is fully retracted. */
        RETRACTED,
        /** The solenoid is stopped. */
        STOPPED,
        /** The solenoid is extending */
        EXTENDING,
        /** The solenoid is retracting */
        RETRACTING,
    }

    /**
     * Set the position of the solenoid.
     * 
     * @param position the desired position (extended or retracted).
     */
    Solenoid setPosition(Position position);

    /**
     * Get the current position of this solenoid.
     * If it is moving, then it will return which direction it's moving in.
     * i.e. If setPosition(EXTENDED) was called, then getPosition() will return
     * EXTENDING until it's had sufficient time to move.
     *
     * @return the current position; never null
     */
    Position getPosition();

    /**
     * Indicates if the solenoid has finished moving.
     * 
     * @return true if the solenoid has finished moved to its last
     *         set position.
     */
    default boolean isInPosition() {
        return isExtended() || isRetracted() || isStopped();
    }

    /**
     * Extends this solenoid.
     * 
     * @return this object to allow chaining of methods; never null
     */
    default Solenoid extend() {
        return setPosition(Position.EXTENDED);
    }

    /**
     * Retracts this solenoid.
     * 
     * @return this object to allow chaining of methods; never null
     */
    default Solenoid retract() {
        return setPosition(Position.RETRACTED);
    }

    /**
     * Determine if this solenoid is fully extended.
     *
     * @return {@code true} if this solenoid is fully extended
     */
    default boolean isExtended() {
        return getPosition() == Position.EXTENDED;
    }

    /**
     * Determine if this solenoid is fully retracted.
     *
     * @return {@code true} if this solenoid is fully retracted
     */
    default boolean isRetracted() {
        return getPosition() == Position.RETRACTED;
    }

    /**
     * Determine if this solenoid is stopped.
     *
     * @return {@code true} if this solenoid is not moving, or false otherwise
     */
    boolean isStopped();

    /**
     * Invert the direction of the underlying solenoid.
     * 
     * @param inverted set to false to keep EXTENDED => Forward
     */
    Solenoid setInverted(boolean inverted);
}
