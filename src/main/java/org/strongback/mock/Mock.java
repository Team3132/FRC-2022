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



import java.util.concurrent.atomic.AtomicLong;
import org.strongback.components.Fuse;
import org.strongback.components.Switch;
import org.strongback.control.Controller;
import org.strongback.control.PIDController;
import org.strongback.hardware.Hardware.Switches.AnalogOption;
import org.strongback.hardware.Hardware.Switches.TriggerMode;


/**
 * Factory for mock components.
 *
 * @author Randall Hauch
 */
public class Mock {

    private static final AtomicLong CAN_DEVICE_ID_GENERATOR = new AtomicLong();

    /**
     * Create a mock power panel.
     *
     * @return the mock power panel; never null
     */
    public static MockPowerPanel powerPanel() {
        return new MockPowerPanel(16);
    }

    /**
     * Create a mock pneumatics module. This method can be called more than once to represent a
     * robot with multiple pneumatics
     * modules.
     *
     * @return the mock pneumatics module; never null
     */
    public static MockPneumaticsModule pneumaticsModule() {
        return new MockPneumaticsModule();
    }

    /**
     * Create a mock pneumatics module. This method can be called more than once to represent a
     * robot with multiple pneumatics
     * modules.
     *
     * @return the mock pneumatics module; never null
     */
    public static MockPneumaticsModule pneumaticsModule(int id) {
        return new MockPneumaticsModule();
    }

    /**
     * Create a mock clock.
     *
     * @return the mock clock; never null
     */
    public static MockClock clock() {
        return new MockClock();
    }

    /**
     * Create a mock accelerometer.
     *
     * @return the mock accelerometer; never null
     */
    public static MockAccelerometer accelerometer() {
        return new MockAccelerometer();
    }

    /**
     * Create a mock 2-axis accelerometer.
     *
     * @return the mock 2-axis accelerometer; never null
     */
    public static MockTwoAxisAccelerometer accelerometer2Axis() {
        return new MockTwoAxisAccelerometer();
    }

    /**
     * Create a mock 3-axis accelerometer.
     *
     * @return the mock 3-axis accelerometer; never null
     */
    public static MockThreeAxisAccelerometer accelerometer3Axis() {
        return new MockThreeAxisAccelerometer();
    }

    /**
     * Create a mock angle sensor.
     *
     * @return the mock angle sensor; never null
     */
    public static MockAngleSensor angleSensor() {
        return new MockAngleSensor();
    }

    /**
     * Create a mock compass.
     *
     * @return the mock compass; never null
     */
    public static MockCompass compass() {
        return new MockCompass();
    }

    /**
     * Create a mock current sensor.
     *
     * @return the mock current sensor; never null
     */
    public static MockCurrentSensor currentSensor() {
        return new MockCurrentSensor();
    }

    /**
     * Create a mock distance sensor.
     *
     * @return the mock distance sensor; never null
     */
    public static MockDistanceSensor distanceSensor() {
        return new MockDistanceSensor();
    }

    /**
     * Create a mock double supplier.
     *
     * @return the mock double supplier; never null
     */
    public static MockDoubleSupplier doubleSupplier() {
        return new MockDoubleSupplier();
    }

    /**
     * Create a mock voltage sensor.
     *
     * @return the mock voltage sensor; never null
     */
    public static MockVoltageSensor voltageSensor() {
        return new MockVoltageSensor();
    }

    /**
     * Create a mock temperature sensor.
     *
     * @return the mock temperature sensor; never null
     */
    public static MockTemperatureSensor temperatureSensor() {
        return new MockTemperatureSensor();
    }

    /**
     * Create a mock switch that has already been triggered.
     *
     * @return the mock switch; never null
     */
    public static MockSwitchImplementation triggeredSwitch() {
        MockSwitchImplementation s = new MockSwitchImplementation();
        s.setTriggered();
        return s;
    }

    /**
     * Create a mock switch that has not yet been triggered.
     *
     * @return the mock switch; never null
     */
    public static MockSwitchImplementation notTriggeredSwitch() {
        MockSwitchImplementation s = new MockSwitchImplementation();
        s.setNotTriggered();
        return s;
    }

    /**
     * Create a fuse that has not yet been triggered.
     *
     * @return the mock switch; never null
     */
    public static Fuse triggeredFuse() {
        return Fuse.create().trigger();
    }

    /**
     * Create a fuse that has not yet been triggered.
     *
     * @return the mock switch; never null
     */
    public static Fuse notTriggeredFuse() {
        return Fuse.create().reset();
    }

    /**
     * Create a relay that operates instantaneously.
     *
     * @return the mock relay; never null
     */
    public static MockRelay relay() {
        return new MockRelay();
    }

    /**
     * Create a mock gyroscope.
     *
     * @return the mock gyroscope; never null
     */
    public static MockGyroscope gyroscope() {
        return new MockGyroscope();
    }

    /**
     * Create a mock motor.
     *
     * @return the mock motor; never null
     */
    public static MockMotor stoppedMotor() {
        return new MockMotor(0.0);
    }

    /**
     * Create a running mock motor.
     *
     * @param speed the initial speed
     * @return the mock motor; never null
     */
    public static MockMotor runningMotor(double speed) {
        return new MockMotor(speed);
    }

    protected static int nextDeviceId() {
        return (int) CAN_DEVICE_ID_GENERATOR.getAndIncrement();
    }

    /**
     * Create a mock {@link Controller}.
     *
     * @return the mock controller; never null
     */
    public static MockController controller() {
        return new MockController();
    }

    /**
     * Create a mock {@link PIDController}.
     *
     * @return the mock controller; never null
     */
    public static MockPIDController pidController() {
        return new MockPIDController();
    }

    /**
     * Factory method for mock servos.
     */
    public static final class Servos {
        public static MockServo servo() {
            return new MockServo();
        }

        public static MockTimedServo timedServo(int channel, double min, double max,
                double travelPerSecond, double initial) {
            return new MockTimedServo(min, max, travelPerSecond, initial);
        }
    }

    /**
     * Factory method for mock angle sensors.
     */
    public static final class AngleSensors {
        public static MockGyroscope gyroscope(int channel) {
            return new MockGyroscope();
        }

        public static MockAngleSensor encoder(int aChannel, int bChannel, double distancePerPulse) {
            return new MockEncoder()
                    .setDistancePerPulse(distancePerPulse);
        }

        public static MockAngleSensor potentiometer(int channel, double fullVoltageRangeToDegrees) {
            return new MockAngleSensor();
        }

        public static MockAngleSensor potentiometer(int channel, double fullVoltageRangeToDegrees,
                double offsetInDegrees) {
            return new MockAngleSensor();
        }
    }


    /**
     * Factory method for different kinds of switches.
     */
    public static final class Switches {

        /**
         * Create a generic normally closed digital switch sensor on the specified digital channel.
         *
         * @param channel the channel the switch is connected to
         * @return a switch on the specified channel
         */
        public static MockSwitch normallyClosed(int channel) {
            MockSwitchImplementation input = new MockSwitchImplementation();
            input.setTriggered();
            return input;
        }

        /**
         * Create a generic normally open digital switch sensor on the specified digital channel.
         *
         * @param channel the channel the switch is connected to
         * @return a switch on the specified channel
         */
        public static MockSwitch normallyOpen(int channel) {
            return new MockSwitchImplementation();
        }

        /**
         * Create an analog switch sensor that is triggered when the value exceeds the specified
         * upper voltage and that is no
         * longer triggered when the value drops below the specified lower voltage.
         *
         * @param channel the port to use for the analog trigger 0-3 are on-board, 4-7 are on the
         *        MXP port
         * @param lowerVoltage the lower voltage limit that below which will result in the switch no
         *        longer being triggered
         * @param upperVoltage the upper voltage limit that above which will result in triggering
         *        the switch
         * @param option the trigger option; may not be null
         * @param mode the trigger mode; may not be null
         * @return the analog switch; never null
         */
        public static Switch analog(int channel, double lowerVoltage, double upperVoltage,
                AnalogOption option,
                TriggerMode mode) {
            return new MockSwitchImplementation();
        }
    }

    /**
     * Factory methods for solenoids.
     */
    public static final class Solenoids {
        /**
         * Create a mock double-acting solenoid that uses the specified channels on the default
         * module.
         *
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid doubleSolenoid(int extendChannel, int retractChannel) {
            return new MockDoubleSolenoid(0, 0);
        }

        /**
         * Create a mock double-acting solenoid that uses the specified channels on the given
         * module.
         *
         * @param module the module for the channels
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid doubleSolenoid(int module, int extendChannel,
                int retractChannel) {
            return new MockDoubleSolenoid(0, 0);
        }

        /**
         * Create a mock double-acting solenoid that uses the specified channels on the default
         * module.
         *
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid doubleSolenoid(int extendChannel, int retractChannel,
                double timeIn, double timeOut) {
            return new MockDoubleSolenoid(timeIn, timeOut);
        }

        /**
         * Create a mock double-acting solenoid that uses the specified channels on the given
         * module.
         *
         * @param module the module for the channels
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid doubleSolenoid(int module, int extendChannel, int retractChannel,
                double timeIn, double timeOut) {
            return new MockDoubleSolenoid(timeIn, timeOut);
        }

        /**
         * Create a mock single-acting solenoid that uses the specified channel on the default
         * module.
         *
         * @param extendChannel the channel that extends the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid singleSolenoid(int extendChannel) {
            return new MockSingleSolenoid(0, 0);
        }

        /**
         * Create a mock single-acting solenoid that uses the specified channel on the given module.
         *
         * @param module the module for the channel
         * @param extendChannel the channel that extends the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid singleSolenoid(int module, int extendChannel) {
            return new MockSingleSolenoid(0, 0);
        }

        /**
         * Create a mock single-acting solenoid that uses the specified channel on the default
         * module.
         *
         * @param extendChannel the channel that extends the solenoid
         * @param timeIn the amount of time it takes to retract the solenoid
         * @param timeOut the amount of time it takes to extend the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid singleSolenoid(int extendChannel, double timeIn,
                double timeOut) {
            return new MockSingleSolenoid(timeIn, timeOut);
        }

        /**
         * Create a mock single-acting solenoid that uses the specified channel on the given module.
         *
         * @param module the module for the channel
         * @param extendChannel the channel that extends the solenoid
         * @param timeIn the amount of time it takes to retract the solenoid
         * @param timeOut the amount of time it takes to extend the solenoid
         * @return a solenoid on the specified channels; never null
         */
        public static MockSolenoid singleSolenoid(int module, int extendChannel, double timeIn,
                double timeOut) {
            return new MockSingleSolenoid(timeIn, timeOut);
        }

        /**
         * Create a relay on the specified channel.
         *
         * @return a mock relay
         */
        public static MockRelay relay(int channel) {
            return new MockRelay();
        }
    }
}
