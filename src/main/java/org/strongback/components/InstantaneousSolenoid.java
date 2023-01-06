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

/**
 * A solenoid is a device that can be extended and retracted.
 *
 * @author Zach Anderson
 * @author Rex di Bona
 */
@ThreadSafe
public interface InstantaneousSolenoid extends Actuator {

    /**
     * The direction of the solenoid.
     */
    static enum Direction {
        STOPPED, // the solenoid is not moving (and is unknown)
        EXTENDED, // The solenoid is extended
        RETRACTED, // the solenoid is retracted
        EXTENDING, // The solenoid is currently extending but has not yet completed
        RETRACTING // The solenoid is currently retracting but has not yet completed
    }

    /**
     * Get the current direction of this solenoid.
     *
     * @return the current direction; never null
     */
    Direction getDirection();

    /**
     * Extends this solenoid.
     * 
     * @return this object to allow chaining of methods; never null
     */
    InstantaneousSolenoid extend();

    /**
     * Retracts this solenoid.
     * 
     * @return this object to allow chaining of methods; never null
     */
    InstantaneousSolenoid retract();

    /**
     * Determine if this solenoid is currently extending.
     *
     * @return {@code true} if this solenoid is in the process of extending but not yet fully
     *         extended, or {@code false}
     *         otherwise
     */
    default boolean isExtending() {
        return getDirection() == Direction.EXTENDING;
    }

    /**
     * Determine if this solenoid is currently retracting.
     *
     * @return {@code true} if this solenoid is in the process of retracting but not yet fully
     *         retracted, or {@code false}
     *         otherwise
     */
    default boolean isRetracting() {
        return getDirection() == Direction.RETRACTING;
    }

    /**
     * Determine if this solenoid has finished extending.
     *
     * @return {@code true} if this solenoid is fully extended, or {@code false}
     *         otherwise
     */
    default boolean isExtended() {
        return getDirection() == Direction.EXTENDED;
    }

    /**
     * Determine if this solenoid has finished retracting.
     *
     * @return {@code true} if this solenoid is fully retracted, or {@code false}
     *         otherwise
     */
    default boolean isRetracted() {
        return getDirection() == Direction.RETRACTED;
    }

    /**
     * Determine if this solenoid is stopped.
     *
     * @return {@code true} if this solenoid is not retracting or extending, or false otherwise
     */
    default boolean isStopped() {
        return getDirection() == Direction.STOPPED;
    }
}
