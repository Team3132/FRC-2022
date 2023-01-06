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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MockPneumaticsModuleTest {

    private MockPneumaticsModule module;

    @BeforeEach
    public void beforeEach() {
        this.module = new MockPneumaticsModule();
    }

    @Test
    public void shouldNotBeAutomaticModeByDefault() {
        assertTrue(module.automaticMode().isOn());
        assertFalse(module.compressorRunningSwitch().isTriggered());
    }

    @Test
    public void shouldNotRunCompressorAutomaticallyWhenSetToAutomaticModeAndLowPressureIsNotTriggered() {
        module.lowPressureSwitch().setNotTriggered(); // start out with enough pressure
        module.automaticMode().on();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertTrue(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(),
                MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertTrue(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(),
                MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // Turn off auto mode when running ...
        module.automaticMode().off();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);
    }

    @Test
    public void shouldRunCompressorAutomaticallyWhenSetToAutomaticModeAndLowPressureIsTriggered() {
        module.lowPressureSwitch().setTriggered(); // start out with low pressure
        module.automaticMode().on();
        assertTrue(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(),
                MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertTrue(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(),
                MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        // Turn off auto mode when not running ...
        module.automaticMode().off();
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);
    }

    @Test
    public void shouldKeepStickyFaultStates() {
        assertFalse(module.compressorFaults().currentTooHigh().isTriggered());
        assertFalse(module.compressorFaults().notConnected().isTriggered());
        assertFalse(module.compressorFaults().shorted().isTriggered());
        assertFalse(module.compressorStickyFaults().currentTooHigh().isTriggered());
        assertFalse(module.compressorStickyFaults().notConnected().isTriggered());
        assertFalse(module.compressorStickyFaults().shorted().isTriggered());

        module.compressorFaults().currentTooHigh().trigger();
        assertFalse(module.compressorFaults().currentTooHigh().isTriggered());
        assertTrue(module.compressorStickyFaults().currentTooHigh().isTriggered());

        // Start the compressor ...
        module.lowPressureSwitch().setTriggered(); // start out with low pressure
        module.automaticMode().on();

        module.compressorFaults().notConnected().trigger();
        assertFalse(module.compressorFaults().notConnected().isTriggered());
        assertTrue(module.compressorStickyFaults().notConnected().isTriggered());

        // Compressor should stop after a fault
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        // But we can start it again ...
        module.lowPressureSwitch().setTriggered();
        assertTrue(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(),
                MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        module.compressorFaults().shorted().trigger();
        assertFalse(module.compressorFaults().shorted().isTriggered());
        assertTrue(module.compressorStickyFaults().shorted().isTriggered());

        // Compressor should stop after a fault
        assertFalse(module.compressorRunningSwitch().isTriggered());
        assertEquals(module.compressorCurrent().getCurrent(), 0.0);

        module.clearStickyFaults();

        assertFalse(module.compressorFaults().currentTooHigh().isTriggered());
        assertFalse(module.compressorFaults().notConnected().isTriggered());
        assertFalse(module.compressorFaults().shorted().isTriggered());
        assertFalse(module.compressorStickyFaults().currentTooHigh().isTriggered());
        assertFalse(module.compressorStickyFaults().notConnected().isTriggered());
        assertFalse(module.compressorStickyFaults().shorted().isTriggered());
    }

}
