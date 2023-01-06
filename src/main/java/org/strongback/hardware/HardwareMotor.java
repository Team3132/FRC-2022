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

package org.strongback.hardware;



import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import org.strongback.components.Motor;
import org.strongback.function.DoubleToDoubleFunction;

/**
 * Wrapper for WPILib {@link MotorController}.
 *
 * @author Zach Anderson
 * @see Motor
 * @see Hardware
 * @see edu.wpi.first.wpilibj.motorcontrol.MotorController
 */
class HardwareMotor implements Motor {

    private final MotorController controller;
    private final DoubleToDoubleFunction speedValidator;

    HardwareMotor(MotorController controller, DoubleToDoubleFunction speedValidator) {
        this.controller = controller;
        this.speedValidator = speedValidator;
    }

    @Override
    public void set(ControlMode mode, double demand) {
        if (mode != ControlMode.DutyCycle) {
            // These types of motors only support duty cycle, so only
            // pass through either a duty cycle demand or zero demand.
            demand = 0;
        }
        controller.set(speedValidator.applyAsDouble(demand));
    }

    /**
     * Speed detection is not available for these motor controllers.
     */
    @Override
    public double getSpeed() {
        return 0;
    }

    /**
     * Returns the last set duty cycle.
     */
    @Override
    public double get() {
        return controller.get();
    }
}
