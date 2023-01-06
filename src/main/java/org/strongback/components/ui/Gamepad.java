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
import java.util.function.DoubleConsumer;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;
import org.strongback.function.IntToIntFunction;

/**
 * A type of input device similar to an Xbox controller.
 */
public interface Gamepad extends InputDevice {

    public final static double kThumbstickThreshold = 0.3;
    public final static double kTriggerThreshold = 0.6;

    public abstract ContinuousRange getLeftX();

    public abstract ContinuousRange getLeftY();

    public abstract ContinuousRange getRightX();

    public abstract ContinuousRange getRightY();

    public abstract ContinuousRange getLeftTrigger();

    public abstract ContinuousRange getRightTrigger();

    public abstract Axis leftAxis();

    public abstract Axis rightAxis();

    public abstract Axis DPadAxis();

    public abstract Trigger leftTrigger();

    public abstract Trigger rightTrigger();

    public abstract Trigger leftBumper();

    public abstract Trigger rightBumper();

    public abstract Trigger aButton();

    public abstract Trigger bButton();

    public abstract Trigger xButton();

    public abstract Trigger yButton();

    public abstract Trigger startButton();

    public abstract Trigger backButton();

    public abstract Trigger leftStick();

    public abstract Trigger rightStick();

    public abstract void setRumbleLeft(double value);

    public abstract void setRumbleRight(double value);

    public static Gamepad create(String name, IntToDoubleFunction axisToValue,
            IntToBooleanFunction buttonNumberToSwitch,
            IntToIntFunction dPad, IntSupplier axisCount, IntSupplier buttonCount,
            IntSupplier POVCount,
            ContinuousRange leftX, ContinuousRange leftY, ContinuousRange rightX,
            ContinuousRange rightY,
            ContinuousRange leftTrigger, ContinuousRange rightTrigger, BooleanSupplier leftBumper,
            BooleanSupplier rightBumper, BooleanSupplier buttonA,
            BooleanSupplier buttonB, BooleanSupplier buttonX, BooleanSupplier buttonY,
            BooleanSupplier startButton, BooleanSupplier backButton,
            BooleanSupplier leftStick, BooleanSupplier rightStick,
            DoubleConsumer leftRumble, DoubleConsumer rightRumble) {
        return new Gamepad() {
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
                return () -> dPad.applyAsInt(pad);
            }

            @Override
            public Switch getDPad(int pad, int direction) {
                return () -> dPad.applyAsInt(pad) == direction;
            }

            @Override
            public Axis DPadAxis() {
                return new DPadAxis(getName(), "DPad", getDPad(0));
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
            public ContinuousRange getLeftX() {
                return leftX;
            }

            @Override
            public ContinuousRange getLeftY() {
                return leftY;
            }

            @Override
            public ContinuousRange getRightX() {
                return rightX;
            }

            @Override
            public ContinuousRange getRightY() {
                return rightY;
            }

            @Override
            public Axis leftAxis() {
                return new JoystickAxis(getName(), "left thumbstick", leftX, leftY, leftStick,
                        kThumbstickThreshold);
            }

            @Override
            public Axis rightAxis() {
                return new JoystickAxis(getName(), "left thumbstick", rightX, rightY, rightStick,
                        kThumbstickThreshold);
            }

            @Override
            public ContinuousRange getLeftTrigger() {
                return leftTrigger;
            }

            @Override
            public ContinuousRange getRightTrigger() {
                return rightTrigger;
            }

            @Override
            public Trigger leftTrigger() {
                return trigger("left trigger", () -> leftTrigger.read() > kTriggerThreshold);
            }

            @Override
            public Trigger rightTrigger() {
                return trigger("right trigger", () -> rightTrigger.read() > kTriggerThreshold);
            }

            @Override
            public Trigger leftBumper() {
                return trigger("left bumper", leftBumper);
            }

            @Override
            public Trigger rightBumper() {
                return trigger("right bumper", rightBumper);
            }

            @Override
            public Trigger aButton() {
                return trigger("A", buttonA);
            }

            @Override
            public Trigger bButton() {
                return trigger("B", buttonB);
            }

            @Override
            public Trigger xButton() {
                return trigger("X", buttonX);
            }

            @Override
            public Trigger yButton() {
                return trigger("Y", buttonY);
            }

            @Override
            public Trigger startButton() {
                return trigger("start button", startButton);
            }

            @Override
            public Trigger backButton() {
                return trigger("back button", backButton);
            }

            @Override
            public Trigger leftStick() {
                return trigger("left stick", leftStick);
            }

            @Override
            public Trigger rightStick() {
                return trigger("right stick", rightStick);
            }

            @Override
            public void setRumbleLeft(double value) {
                leftRumble.accept(value);
            }

            @Override
            public void setRumbleRight(double value) {
                rightRumble.accept(value);
            }

            private Trigger trigger(String button, BooleanSupplier supplier) {
                return new Trigger(getName(), button, supplier);
            }
        };
    }
}
