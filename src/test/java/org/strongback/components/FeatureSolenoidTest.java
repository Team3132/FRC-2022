package org.strongback.components;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strongback.components.FeatureSolenoid.Mode;
import org.strongback.components.Solenoid.Position;
import org.strongback.mock.Mock;
import org.strongback.mock.MockSolenoid;

public class FeatureSolenoidTest {
    private MockSolenoid solenoid;

    @BeforeEach
    public void beforeEach() {
        solenoid = Mock.Solenoids.doubleSolenoid(0, 1);
    }

    @Test
    public void basic() {
        FeatureSolenoid feature = new FeatureSolenoid(solenoid);
        // Start with a retracted solenoid, which should be the disabled mode.
        solenoid.setPosition(Position.RETRACTED);
        assertFalse(solenoid.isExtended());
        assertTrue(solenoid.isRetracted());
        assertFalse(feature.isEnabled());
        assertTrue(feature.isDisabled());
        assertEquals(feature.getMode(), Mode.DISABLED);

        // Enable the feature.
        feature.setMode(Mode.ENABLED);
        assertEquals(feature.getMode(), Mode.ENABLED);
        assertTrue(solenoid.isExtended());
        assertFalse(solenoid.isRetracted());
        assertTrue(feature.isEnabled());
        assertFalse(feature.isDisabled());

        // Disable the feature
        feature.setMode(Mode.DISABLED);
        assertEquals(feature.getMode(), Mode.DISABLED);
        assertFalse(solenoid.isExtended());
        assertTrue(solenoid.isRetracted());
        assertFalse(feature.isEnabled());
        assertTrue(feature.isDisabled());
    }
}
