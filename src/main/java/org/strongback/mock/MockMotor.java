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

package org.strongback.mock;



import org.strongback.components.Motor;

/**
 * A {@link Motor} implementation useful for testing. This motor does nothing but maintain a record
 * of the current demand.
 * 
 * Because there are multiple modes (duty cycle, speed, position etc), the get{Speed|Position}()
 * will only return the
 * last set demand if set() was set with that mode last time.
 *
 * @author Randall Hauch
 *
 */
public class MockMotor implements Motor {

    private volatile double demand = 0;
    private volatile ControlMode mode = ControlMode.DutyCycle;

    MockMotor(double dutyCycle) {
        mode = ControlMode.DutyCycle;
        this.demand = dutyCycle;
    }

    @Override
    public void set(ControlMode mode, double demand) {
        this.mode = mode;
        this.demand = demand;
    }

    @Override
    public double getSpeed() {
        if (mode == ControlMode.Speed)
            return demand;
        // Wasn't told a speed last time, return 0.
        return 0;
    }

    @Override
    public double get() {
        return demand;
    }

    @Override
    public String toString() {
        return String.format("mode: %s, demand %f", mode, demand);
    }

    @Override
    public double getPosition() {
        if (mode == ControlMode.Position)
            return demand;
        // Wasn't told a position last time, return 0.
        return 0;
    }

    @Override
    public double getOutputPercent() {
        if (mode == ControlMode.DutyCycle)
            return demand;
        // Wasn't told a duty cycle last time, return 0.
        return 0;
    }
}
