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



import edu.wpi.first.wpilibj.CompressorConfigType;
import edu.wpi.first.wpilibj.PneumaticsControlModule;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import org.strongback.annotation.ThreadSafe;
import org.strongback.components.CurrentSensor;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Relay;
import org.strongback.components.Solenoid;
import org.strongback.components.Switch;

/**
 * A {@link PneumaticsModule} implementation based upon the WPILib's {@link Compressor} class, which
 * represents the Pneumatics
 * Control Module.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class HardwarePneumaticsModule implements PneumaticsModule {

    public final PneumaticsControlModule pcm;
    public final PneumaticsModuleType type;
    private final Relay closedLoop;
    private final Faults instantaneousFaults;
    private final Faults stickyFaults;

    HardwarePneumaticsModule(PneumaticsControlModule pcm, PneumaticsModuleType type) {
        this.pcm = pcm;
        this.type = type;
        this.closedLoop =
                Relay.instantaneous(this::setClosedLoopControl, this::getClosedLoopControl);
        this.instantaneousFaults = new Faults() {
            @Override
            public Switch currentTooHigh() {
                return pcm::getCompressorCurrentTooHighFault;
            }

            @Override
            public Switch notConnected() {
                return pcm::getCompressorNotConnectedFault;
            }

            @Override
            public Switch shorted() {
                return pcm::getCompressorShortedFault;
            }
        };
        this.stickyFaults = new Faults() {
            @Override
            public Switch currentTooHigh() {
                return pcm::getCompressorCurrentTooHighStickyFault;
            }

            @Override
            public Switch notConnected() {
                return pcm::getCompressorNotConnectedStickyFault;
            }

            @Override
            public Switch shorted() {
                return pcm::getCompressorShortedStickyFault;
            }
        };
    }

    private boolean getClosedLoopControl() {
        return this.pcm.getCompressorConfigType() == CompressorConfigType.Digital;
    }

    private void setClosedLoopControl(boolean enabled) {
        if (enabled) {
            this.pcm.enableCompressorDigital();
        } else {
            this.pcm.disableCompressor();
        }
    }

    @Override
    public CurrentSensor compressorCurrent() {
        return pcm::getCompressorCurrent;
    }

    @Override
    public Switch compressorRunningSwitch() {
        return this::getClosedLoopControl;
    }

    @Override
    public Relay automaticMode() {
        return closedLoop;
    }

    @Override
    public Switch lowPressureSwitch() {
        return pcm::getPressureSwitch;
    }

    @Override
    public Faults compressorFaults() {
        return instantaneousFaults;
    }

    @Override
    public Faults compressorStickyFaults() {
        return stickyFaults;
    }

    @Override
    public PneumaticsModule clearStickyFaults() {
        pcm.clearAllStickyFaults();
        return this;
    }

    /**
     * Create a single-acting solenoid that uses the specified channel on the given module.
     *
     * @param extendChannel the channel that extends the solenoid
     * @param timeIn the amount of time it takes to retract the solenoid
     * @param timeOut the amount of time it takes to extend the solenoid
     * @return a solenoid on the specified channels; never null
     */
    @Override
    public Solenoid singleSolenoid(int extendChannel,
            double timeIn,
            double timeOut) {
        return new HardwareSingleSolenoid(
                new edu.wpi.first.wpilibj.Solenoid(pcm.getModuleNumber(), type, extendChannel),
                timeIn, timeOut);
    }

    /**
     * Create a double-acting solenoid that uses the specified channels on the given module.
     *
     * @param extendChannel the channel that extends the solenoid
     * @param retractChannel the channel that retracts the solenoid
     * @param timeIn the amount of time it takes to retract the solenoid
     * @param timeOut the amount of time it takes to extend the solenoid
     * @return a solenoid on the specified channels; never null
     */
    @Override
    public Solenoid doubleSolenoid(int extendChannel,
            int retractChannel,
            double timeIn, double timeOut) {
        return new HardwareDoubleSolenoid(
                new edu.wpi.first.wpilibj.DoubleSolenoid(pcm.getModuleNumber(), type, extendChannel,
                        retractChannel),
                timeIn, timeOut);
    }
}
