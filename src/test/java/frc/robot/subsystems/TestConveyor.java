package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSolenoid;

public class TestConveyor {
    MockMotor motor;
    MockSolenoid blocker;
    ConveyorImpl conveyor;
    MockClock clock;

    @BeforeEach
    public void setup() {
        motor = Mock.stoppedMotor();
        clock = Mock.clock();
        conveyor = new ConveyorImpl(motor);
    }

    @Test
    public void testMotor() {
        conveyor.setDutyCycle(0.75);
        assertEquals(0.75, conveyor.getDutyCycle(), 0.01);
    }
}
