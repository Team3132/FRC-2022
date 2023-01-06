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

package org.strongback.components.ui;



import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;
import org.strongback.function.IntToIntFunction;

/**
 * A simple collection of axes and buttons.
 */
public interface InputDevice {
    /**
     * Get the name of this joystick.
     * 
     * @return the name.
     */
    public String getName();

    /**
     * Get the analog axis for the given number.
     * 
     * @param axis the axis number
     * @return the analog axis, or null if there is no such axis
     */
    public ContinuousRange getAxis(int axis);

    /**
     * Get the button for the given axis number.
     * 
     * @param axis the axis number
     * @return a button which is triggered when the analog axis is beyond the threshold, or null if
     *         there is no such axis
     */
    public default Switch isTriggered(int axis, double threshold) {
        if (threshold > 0) {
            // Positive threshold.
            return () -> getAxis(axis).read() >= threshold;
        } else {
            // Negative threshold.
            return () -> getAxis(axis).read() < threshold;
        }
    }

    /**
     * Get the button for the given number.
     * 
     * @param button the button number
     * @return the button, or null if there is no such button
     */
    public Switch getButton(int button);

    /**
     * Get the button for the given number.
     * 
     * @param button the button number
     * @return a trigger for the button, or null if there is no such button
     */
    public Trigger button(int button);

    /**
     * Get the directional axis for the given D-pad number.
     * 
     * @param pad the pad number
     * @return the directional axis, or null if there is no such axis for the given D-pad number
     */
    public DirectionalAxis getDPad(int pad);

    /**
     * Get the button for the given D-pad number.
     * 
     * @param pad the pad number
     * @param direction the desired direction
     * @return a button which is triggered when the directional axis faces direction, or null if
     *         there is no such axis for the given D-pad number
     */
    public Switch getDPad(int pad, int direction);

    /**
     * Get the number of analog axis present on the input device
     * 
     * @return the number of analog axis present on the input device
     */
    public int getAxisCount();

    /**
     * Get the number of buttons on the input device
     * 
     * @return the number of buttons present on the input device
     */
    public int getButtonCount();

    /**
     * Get the number of POVs on the input device
     * 
     * @return the number of POVs present on the input device
     */
    public int getPOVCount();

    /**
     * Sets the intensity of the input device's left vibration motor.
     * Only supported by gamepads with vibration motors and requires DS4Windows installed if using a
     * dualshock controller.
     * 
     * @param value Rumble amount between 0 and 1.
     */
    public default void setRumbleLeft(double value) {}

    /**
     * Sets the intensity of the input device's right vibration motor.
     * Only supported by gamepads with vibration motors (i.e. Logitech F510 or Sony Dualshock 4, NOT
     * THE F310) and requires DS4Windows installed if using a
     * dualshock controller.
     * 
     * @param value Rumble amount between 0 and 1.
     */
    public default void setRumbleRight(double value) {}

    /**
     * Create an input device from the supplied mapping functions.
     * 
     * @param axisToValue the function that maps an integer to a double value for the axis
     * @param buttonNumberToSwitch the function that maps an integer to whether the button is
     *        pressed
     * @param padToValue the function that maps an integer to the directional axis output
     * @return the resulting input device; never null
     */
    public static InputDevice create(String name, IntToDoubleFunction axisToValue,
            IntToBooleanFunction buttonNumberToSwitch, IntToIntFunction padToValue,
            IntSupplier axisCount, IntSupplier buttonCount, IntSupplier POVCount) {
        return new InputDevice() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public ContinuousRange getAxis(int axis) {
                return () -> axisToValue.applyAsDouble(axis);
            }

            @Override
            public Switch getButton(int button) {
                return () -> buttonNumberToSwitch.applyAsBoolean(button);
            }

            @Override
            public Trigger button(int button) {
                return new Trigger(getName(), "button " + button,
                        () -> buttonNumberToSwitch.applyAsBoolean(button));
            }

            @Override
            public DirectionalAxis getDPad(int pad) {
                return () -> padToValue.applyAsInt(pad);
            }

            @Override
            public Switch getDPad(int pad, int direction) {
                return () -> padToValue.applyAsInt(pad) == direction;
            }

            @Override
            public int getAxisCount() {
                return axisCount.getAsInt();
            }

            @Override
            public int getButtonCount() {
                return buttonCount.getAsInt();
            }

            @Override
            public int getPOVCount() {
                return POVCount.getAsInt();
            }
        };
    }
}
