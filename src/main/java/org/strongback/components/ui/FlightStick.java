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



import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;
import org.strongback.function.IntToIntFunction;

/**
 * A type of input device consisting of a joystick with twist and throttle and multiple buttons.
 */
public interface FlightStick extends InputDevice {
    public ContinuousRange getPitch();

    public ContinuousRange getYaw();

    public ContinuousRange getRoll();

    public ContinuousRange getThrottle();

    public Switch getTrigger();

    public Trigger trigger();

    public Switch getThumb();

    public Trigger thumb();

    public static FlightStick create(String name, IntToDoubleFunction axisToValue,
            IntToBooleanFunction buttonNumberToSwitch,
            IntToIntFunction padToValue, IntSupplier axisCount, IntSupplier buttonCount,
            IntSupplier POVCount,
            ContinuousRange pitch, ContinuousRange yaw, ContinuousRange roll,
            ContinuousRange throttle, BooleanSupplier trigger, BooleanSupplier thumb) {
        return new FlightStick() {
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
                return trigger("button " + button,
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

            public int getPOVCount() {
                return POVCount.getAsInt();
            }

            @Override
            public ContinuousRange getPitch() {
                return pitch;
            }

            @Override
            public ContinuousRange getYaw() {
                return yaw;
            }

            @Override
            public ContinuousRange getRoll() {
                return roll;
            }

            @Override
            public ContinuousRange getThrottle() {
                return throttle;
            }

            @Override
            public Switch getTrigger() {
                return () -> trigger.getAsBoolean();
            }

            @Override
            public Trigger trigger() {
                return trigger("trigger", trigger);
            }

            @Override
            public Switch getThumb() {
                return () -> thumb.getAsBoolean();
            }

            @Override
            public Trigger thumb() {
                return trigger("thumb", thumb);
            }

            private Trigger trigger(String button, BooleanSupplier supplier) {
                return new Trigger(getName(), button, supplier);
            }
        };
    }

}
