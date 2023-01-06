package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.robot.interfaces.Intake;
import frc.robot.mock.MockIntake;
import org.junit.jupiter.api.Test;

public class TestOverridableSubsystem {

    @Test
    public void testNormalMode() {
        // This is when the controller should talk through to the real
        // subsystem and the button box is passed through to the mock.
        // We only want to use mock here so that they can be checked.
        Intake real = new MockIntake();
        Intake simulator = new MockIntake();
        Intake mock = new MockIntake();
        // Create the sim and pass it the three different endpoints.
        OverridableSubsystem<Intake> intakeOverride =
                new OverridableSubsystem<Intake>("intake", Intake.class, real, simulator, mock);
        // Get the endpoint that the controller would use.
        Intake normalIntake = intakeOverride.getNormalInterface();
        // Get the endpoint that the diag box uses.
        Intake overrideIntake = intakeOverride.getOverrideInterface();
        // Tell the different mocks that they should all have zero power.
        real.setTargetRPS(0);
        simulator.setTargetRPS(0);
        mock.setTargetRPS(0);
        // Put the overridable interface into automatic mode (controller talks to the real
        // subsystem)
        intakeOverride.setAutomaticMode();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(1);
        // Check that only the real interface got told about it.
        assertEquals(real.getTargetRPS(), 1.0, 0.1);
        assertEquals(simulator.getTargetRPS(), 0, 0.1);
        assertEquals(mock.getTargetRPS(), 0, 0.1);
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-1);
        // Check that only the real interface got told about it.
        assertEquals(real.getTargetRPS(), 1.0, 0.1);
        assertEquals(simulator.getTargetRPS(), 0, 0.1);
        assertEquals(mock.getTargetRPS(), -1, 0.1);

        // Now change to manual mode. Controller should talk to the simulator
        // and the diag box to the real interface.
        intakeOverride.setManualMode();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(0.5);
        // Check that only the simulator got told about it.
        assertEquals(real.getTargetRPS(), 1.0, 0.1);
        assertEquals(simulator.getTargetRPS(), 0.5, 0.1);
        assertEquals(mock.getTargetRPS(), -1, 0.1);
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-0.5);
        // Check that only the real interface got told about it.
        assertEquals(real.getTargetRPS(), -0.5, 0.1);
        assertEquals(simulator.getTargetRPS(), 0.5, 0.1);
        assertEquals(mock.getTargetRPS(), -1, 0.1);

        // And finally, turn it off. Nothing should talk to the real subsystem.
        // The controller should talk to the simulator and the diag box to
        // the mock.
        intakeOverride.turnOff();
        // Pretend to be the controller and send through a command.
        normalIntake.setTargetRPS(0.25);
        // Check that only the simulator got told about it.
        assertEquals(real.getTargetRPS(), -0.5, 0.1);
        assertEquals(simulator.getTargetRPS(), 0.25, 0.1);
        assertEquals(mock.getTargetRPS(), -1, 0.1);
        // Pretend to be the diag box and send through a command.
        overrideIntake.setTargetRPS(-0.25);
        // Check that only the mock got told about it.
        assertEquals(real.getTargetRPS(), -0.5, 0.1);
        assertEquals(simulator.getTargetRPS(), 0.25, 0.1);
        assertEquals(mock.getTargetRPS(), -0.25, 0.1);
    }

}
