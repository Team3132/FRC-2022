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
import org.strongback.drive.TankDrive;


/**
 * A motor is a device that can be set to operate at a speed.
 *
 * @author Zach Anderson
 *
 */
@ThreadSafe
public interface Motor extends SpeedSensor, Stoppable, Requirable {

    public enum ControlMode {
        /**
         * Set the faction of time that the motor is being powered [-1,1].
         * Same as percent output.
         */
        DutyCycle(0, com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput,
                com.revrobotics.CANSparkMax.ControlType.kDutyCycle),
        /**
         * Position closed loop
         */
        Position(1, com.ctre.phoenix.motorcontrol.ControlMode.Position,
                com.revrobotics.CANSparkMax.ControlType.kPosition),
        /**
         * Speed closed loop
         * This is what both the Talon and the Spark MAX call velocity, but there is no direction
         * component
         * so we call it speed.
         */
        Speed(2, com.ctre.phoenix.motorcontrol.ControlMode.Velocity,
                com.revrobotics.CANSparkMax.ControlType.kVelocity),
        /**
         * Input current closed loop
         */
        Current(3, com.ctre.phoenix.motorcontrol.ControlMode.Current,
                com.revrobotics.CANSparkMax.ControlType.kCurrent),
        /**
         * Follow other motor controller
         * Not supported by Spark MAX.
         */
        Follower(5, com.ctre.phoenix.motorcontrol.ControlMode.Follower,
                com.revrobotics.CANSparkMax.ControlType.kDutyCycle),
        /**
         * Motion Profile
         */
        MotionProfile(6, com.ctre.phoenix.motorcontrol.ControlMode.MotionProfile,
                com.revrobotics.CANSparkMax.ControlType.kSmartMotion),
        /**
         * Motion Magic
         */
        MotionMagic(7, com.ctre.phoenix.motorcontrol.ControlMode.MotionMagic,
                com.revrobotics.CANSparkMax.ControlType.kSmartVelocity),
        /**
         * Motion Profile with auxiliary output
         */
        MotionProfileArc(10, com.ctre.phoenix.motorcontrol.ControlMode.MotionProfileArc,
                com.revrobotics.CANSparkMax.ControlType.kSmartMotion),

        /**
         * Disable Motor Controller
         * Not supported by Spark MAX
         */
        Disabled(15, com.ctre.phoenix.motorcontrol.ControlMode.Disabled,
                com.revrobotics.CANSparkMax.ControlType.kDutyCycle),

        /**
         * Voltage
         * Not natively supported by TalonSRX/Falcon, so calculated in the wrapper class.
         */
        Voltage(20, com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput,
                com.revrobotics.CANSparkMax.ControlType.kVoltage);

        /**
         * Value of control mode
         */
        public final int value;
        public final com.ctre.phoenix.motorcontrol.ControlMode talonControlMode;
        public final com.revrobotics.CANSparkMax.ControlType revControlType;

        /**
         * Create ControlMode of initValue
         * 
         * @param initValue Value of ControlMode
         */
        ControlMode(final int initValue, com.ctre.phoenix.motorcontrol.ControlMode talonControlMode,
                com.revrobotics.CANSparkMax.ControlType revControlType) {
            this.value = initValue;
            this.talonControlMode = talonControlMode;
            this.revControlType = revControlType;
        }
    };

    /**
     * Tell the motor what control mode and how fast/far.
     * Some motor controllers don't support some modes.
     * Normally for Speed mode, this is in rps, not ticks / 100ms.
     * 
     * @param mode percent output, position, velocity etc.
     * @param demand for percent [-1,1].
     */
    public void set(final ControlMode mode, double demand);

    /**
     * Ask for the last set demand.
     * Not normally useful without the control mode used.
     */
    public double get();

    /**
     * Returns the velocity after scaling.
     * Normally in rps or in metres/sec.
     * 
     * Not supported by all motor controllers.
     */
    public default double getSpeed() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Returns the position in metres after scaling.
     * 
     * Not supported by all motor controllers.
     */
    public default double getPosition() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Scale the values to/from the motors into more intuitive values.
     * 
     * getPosition() returns revolutions.
     * getVelocity() returns revolutions/second.
     * 
     * Also consider setScale(double ticksPerTurn, double gearRatio, double wheelDiameterMetres).
     * 
     * @param ticksPerTurn How many encoder ticks per turn, eg 4096 or 42.
     * @param gearRatio How many turns of the motor to turn the output shaft, eg 11
     * @param wheelDiameterMetres How many metres does the wheel move for every turn.
     * @return this.
     */
    public default Motor setScale(double ticksPerTurn, double gearRatio) {
        setScale(ticksPerTurn, gearRatio, 1);
        return this;
    }

    /**
     * Scale the values to/from the motors into more intuitive values.
     * 
     * getPosition() returns the number of metres.
     * getVelocity() returns metres/second.
     * 
     * Also consider setScale(double ticksPerTurn, double gearRatio).
     * 
     * @param ticksPerTurn How many encoder ticks per turn, eg 4096 or 42.
     * @param gearRatio How many turns of the motor to turn the output shaft, eg 11
     * @param wheelDiameterMetres How many metres does the wheel move for every turn.
     * @return this.
     */
    public default Motor setScale(double ticksPerTurn, double gearRatio,
            double wheelDiameterMetres) {
        // Default implementation does nothing.
        return this;
    }

    public default Motor enable() {
        return this;
    }

    public default Motor disable() {
        stop();
        return this;
    }

    /**
     * Set PID parameters for motor controllers that support it.
     */
    public default Motor setPIDF(int slotIdx, PIDF pidf) {
        // Not implemented by default.
        return this;
    }

    /**
     * Tell the motor controller which set of PID values to use.
     */
    public default Motor selectProfileSlot(int slotIdx) {
        // Not implemented by default.
        return this;
    }

    /**
     * Query the forward limit switch.
     * Not implemented on all motor controllers.
     * 
     * @return true if the forward limit switch is triggered.
     */
    public default boolean isAtForwardLimit() {
        // Not implemented by default.
        return false;
    }

    /**
     * Query the forward limit switch.
     * Not implemented on all motor controllers.
     * 
     * @return true if the forward limit switch is triggered.
     */
    public default boolean isAtReverseLimit() {
        // Not implemented by default.
        return false;
    }

    /**
     * Returns the bus voltage for motor controllers that support it.
     * 
     * @return voltage on the bus.
     */
    public default double getBusVoltage() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Returns the voltage being supplied to the motor for motor controllers that support it.
     * 
     * @return voltage to the motor.
     */
    public default double getOutputVoltage() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Returns the percentage of the power being supplied to the motor for motor controllers that
     * support it.
     * 
     * @return percentage
     */
    public default double getOutputPercent() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Returns the output current of the motor controller.
     * 
     * @return current in amps to the motor.
     */
    public default double getOutputCurrent() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Returns the current being supplied to the motor for motor controllers that support it.
     * 
     * @return current in amps to the motor.
     */
    public default double getSupplyCurrent() {
        // Not implemented by default.
        return 0;
    }

    /**
     * returns the temperature of the motor controller.
     * Not supported by all motor controllers.
     */
    public default double getTemperature() {
        // Not implemented by default.
        return 0;
    }

    /**
     * Invert just the sensor.
     */
    public default Motor setSensorPhase(boolean phase) {
        // Not implemented by default.
        return this;
    }

    /**
     * Override the position of the quadrature encoder.
     */
    public default Motor setPosition(double position) {
        // Not implemented by default.
        return this;
    }

    /**
     * Invert both the motor and the sensor.
     */
    public default Motor setInverted(boolean invert) {
        // Not implemented by default.
        return this;
    };

    public default boolean getInverted() {
        // Not implemented by default.
        return false;
    }

    /**
     * Stops this {@link Motor}.
     */
    @Override
    public default void stop() {
        set(ControlMode.DutyCycle, 0);
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of two other motors that will
     * be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a
     * {@link TankDrive}.
     * TalonSRX and Spark MAX controllers support following, so use that instead.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(final Motor motor1, final Motor motor2) {
        return new Motor() {
            @Override
            public void set(final ControlMode mode, double demand) {
                motor1.set(mode, demand);
                motor2.set(mode, demand);
            }

            @Override
            public double get() {
                return motor1.get();
            }

            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }
        };
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of three other motors that will
     * be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a
     * {@link TankDrive}.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @param motor3 the third motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(final Motor motor1, final Motor motor2, final Motor motor3) {
        return new Motor() {
            @Override
            public void set(final ControlMode mode, double demand) {
                motor1.set(mode, demand);
                motor2.set(mode, demand);
                motor3.set(mode, demand);
            }

            @Override
            public double get() {
                return motor1.get();
            }

            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }
        };
    }
}
