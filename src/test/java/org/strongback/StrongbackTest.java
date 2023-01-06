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

package org.strongback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strongback.Logger.Level;
import org.strongback.components.Clock;

/**
 * Tests that check the functionality of the {@link Strongback.Engine}.
 */
public class StrongbackTest {

    private static final SystemLogger LOGGER = new SystemLogger();

    private Clock clock;
    private Strongback.Engine engine;

    @BeforeEach
    public void beforeEach() {
        LOGGER.enable(Level.INFO);
        clock = Clock.system();
        engine = new Strongback.Engine(clock, LOGGER);
    }

    @AfterEach
    public void afterEach() {
        try {
            if (engine != null && engine.isRunning()) {
                engine.stop();
            }
        } finally {
            engine = null;
        }
    }

    @Test
    public void shouldNotBeRunningWhenCreated() {
        assertFalse(engine.isRunning());
    }

    @Test
    public void shouldStartWithDefaultConfiguration() {
        engine.logConfiguration();
        assertFalse(engine.isRunning());
        assertTrue(engine.start());
        assertTrue(engine.isRunning());
    }

    @Test
    public void shouldAllowChangingExecutionPeriodWhenNotRunning() {
        assertFalse(engine.isRunning());
        assertEquals(engine.getExecutionPeriod(), 20);
        engine.setExecutionPeriod(5);
        assertEquals(engine.getExecutionPeriod(), 5);
        assertTrue(engine.start());
        engine.logConfiguration();
    }

    @Test
    public void shouldNotAllowChangingExecutionPeriodWhenRunning() {
        assertFalse(engine.isRunning());
        assertTrue(engine.start());
        assertEquals(engine.getExecutionPeriod(), 20);
        LOGGER.enable(Level.OFF);
        assertFalse(engine.setExecutionPeriod(5));
        LOGGER.enable(Level.INFO);
        assertEquals(engine.getExecutionPeriod(), 20);
    }

}
