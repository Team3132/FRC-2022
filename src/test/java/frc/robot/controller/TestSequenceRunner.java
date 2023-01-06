
package frc.robot.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.controller.Sequence.SequenceBuilder;
import frc.robot.controller.SequenceRunner.RunStatus;
import frc.robot.interfaces.Drivebase.DriveRoutineType;
import frc.robot.mock.*;
import frc.robot.simulator.IntakeSimulator;
import frc.robot.subsystems.Subsystems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strongback.components.ui.InputDevice;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockInputDevice;
import org.strongback.mock.MockPneumaticsModule;

public class TestSequenceRunner {
    Sequence seq, seqWithEndState;

    MockClock clock;
    InputDevice gamepad;

    Subsystems subsystems;

    /**
     * Setup the needed state before each test is run.
     */
    @BeforeEach
    public void beforeEach() {
        // Create a dummy sequence that we can check. No interrupt generated.
        SequenceBuilder b1 = new SequenceBuilder("test sequence");
        b1.then().setDrivebasePower(0.5);
        b1.then().deployIntake().setIntakeRPS(10);
        b1.then().setShooterRPS(100);
        seq = b1.build();

        // Create a sequence that had a interrupted state generated to test
        // the cleanup when the sequence is interrupted.
        SequenceBuilder b2 = new SequenceBuilder("test sequence with interrupt");
        b2.then().setShooterRPS(20).setDelayDelta(0.5);
        b2.then().setShooterRPS(40).setDelayDelta(0.5);
        b2.then().setShooterRPS(60).setDelayDelta(0.5);
        b2.then().setShooterRPS(80).setDelayDelta(0.5);
        b2.then().setShooterRPS(100).setDelayDelta(0.5);
        b2.then().deployIntake();
        // With the call to createInterruptState(), an end state will be generated to apply
        // at the very end which should set the shooter to 100 RPS and unblock the paddle, much
        // like:
        // b2.then().setShooterRPS(100).deployIntake();
        b2.createInterruptState();
        seqWithEndState = b2.build();

        // Being able to travel in time can be handy.
        clock = new MockClock();
        gamepad = new MockInputDevice(1, 1, 1);

        subsystems = new Subsystems(clock, gamepad);
        subsystems.intake = new IntakeSimulator();
        subsystems.pcm = new MockPneumaticsModule();
        subsystems.drivebase = new MockDrivebase();
        subsystems.conveyor = new MockConveyor();
        subsystems.shooter = new MockShooter();
        subsystems.location = new MockLocation();
        subsystems.climberLeft = new MockClimber();
        subsystems.climberRight = new MockClimber();
        subsystems.ledStrip = new MockLEDStrip();
    }

    /**
     * Basic sequence that is allowed to finish.
     * 
     * @throws Exception
     */
    @Test
    public void testRunSequence() throws Exception {
        SequenceRunner runner = new SequenceRunner(seq, clock, subsystems);
        assertEquals(runner.status, RunStatus.WAITING_TO_START);
        assertTrue(runner.run()); // Waiting to start, needs to be called again.
        runner.start();
        assertEquals(runner.status, RunStatus.RUNNING);
        assertTrue(runner.run()); // Will set the drivebase power, needs to be called again.
        assertEquals(subsystems.drivebase.getDriveRoutineParameters().type,
                DriveRoutineType.CONSTANT_POWER);
        assertEquals(subsystems.drivebase.getDriveRoutineParameters().value, 0.5);
        // Next step is to set the intake speed, and deploy the intake which will take time,
        // requiring multiple calls to run().
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(runner.run());
        assertFalse(subsystems.intake.isRetracted());
        assertFalse(subsystems.intake.isExtended());
        assertEquals(subsystems.intake.getTargetRPS(), 10.0);
        // Now we time travel so that the intake has had enough time to deploy and our
        // next call of run() will cause it to move to the next step in the sequence.
        clock.incrementBySeconds(1);
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(subsystems.intake.isExtended());
        // run() should now detect that the intake is deployed and move to the next step.
        assertTrue(runner.run());
        // run() should now set the shooter rps and detect that there isn't anything left to do and
        // return false.
        assertFalse(runner.run()); // Nothing left to do, return false so it can be cleaned up.
        assertEquals(subsystems.shooter.getTargetRPS(), 100.0);
    }

    /**
     * Sequence is aborted before it can start. Should only run end state when it is started.
     */
    @Test
    public void testAbortBeforeStart() throws Exception {
        SequenceRunner runner = new SequenceRunner(seqWithEndState, clock, subsystems);
        assertEquals(0.0, subsystems.shooter.getTargetRPS());
        assertEquals(RunStatus.WAITING_TO_START, runner.status);
        runner.abort();
        assertEquals(RunStatus.WAITING_TO_START_THEN_ABORT, runner.status);
        runner.start();
        // Sequence has been told to start, it should immediately apply the end state.
        assertEquals(RunStatus.ABORTED, runner.status);
        assertTrue(runner.run());
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertEquals(100.0, subsystems.shooter.getTargetRPS());
        assertFalse(subsystems.intake.isRetracted());
        assertFalse(subsystems.intake.isExtended());
        // Fast forward time to let the intake move into place.
        clock.incrementBySeconds(1);
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(subsystems.intake.isExtended());

        // The runner should now detect that the intake is deployed and finish, returning false.
        assertFalse(runner.run()); // Nothing left to do, return false so it can be cleaned up.
        assertEquals(100.0, subsystems.shooter.getTargetRPS());
        assertTrue(subsystems.intake.isExtended());
    }

    /**
     * Sequence without an interrupt/end state that is aborted.
     * It should stop after the current state is applied.
     * 
     * @throws Exception
     */
    @Test
    public void testAbort() throws Exception {
        SequenceRunner runner = new SequenceRunner(seq, clock, subsystems);
        assertEquals(runner.status, RunStatus.WAITING_TO_START);
        assertTrue(runner.run()); // Waiting to start, needs to be called again.
        runner.start();
        assertEquals(runner.status, RunStatus.RUNNING);
        assertTrue(runner.run()); // Will set the drivebase power, needs to be called again.
        assertEquals(subsystems.drivebase.getDriveRoutineParameters().type,
                DriveRoutineType.CONSTANT_POWER);
        assertEquals(subsystems.drivebase.getDriveRoutineParameters().value, 0.5);
        // Next step is to set the intake speed, and deploy the intake which will take time,
        // requiring multiple calls to run().
        assertTrue(subsystems.intake.isRetracted());
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(runner.run());
        assertFalse(subsystems.intake.isRetracted());
        assertFalse(subsystems.intake.isExtended());
        assertEquals(subsystems.intake.getTargetRPS(), 10.0);
        // Now we time travel so that the intake has had enough time to deploy and our
        // next call of run() will cause it to move to the next step in the sequence.
        clock.incrementBySeconds(1);
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(subsystems.intake.isExtended());
        // Now abort the run, which should stop the shooter speed being set.
        runner.abort();
        // run() should now detect that the intake is deployed.
        assertTrue(runner.run()); // Run the end state
        assertFalse(runner.run()); // End state is done, return false so it can be cleaned up.
        assertEquals(subsystems.shooter.getTargetRPS(), 0.0);
    }

    /**
     * Sequence with an interrupt/end state that is aborted.
     * It should stop after the current state is applied and then apply the final state.
     * 
     * @throws Exception
     */
    @Test
    public void testAbortWithInterruptState() throws Exception {
        SequenceRunner runner = new SequenceRunner(seqWithEndState, clock, subsystems);
        assertEquals(runner.status, RunStatus.WAITING_TO_START);
        assertTrue(runner.run()); // Waiting to start, needs to be called again.
        runner.start();
        assertEquals(runner.status, RunStatus.RUNNING);
        // Will set the shooter to 20 and delay for 0.5 seconds.
        assertTrue(runner.run());
        assertEquals(subsystems.shooter.getTargetRPS(), 20.0);
        assertTrue(runner.run()); // Same step, still waiting for 0.5 seconds to pass.
        assertEquals(subsystems.shooter.getTargetRPS(), 20.0);
        // Fast forward time.
        clock.incrementByMilliseconds(600);
        assertTrue(runner.run()); // Step has completed.
        assertEquals(subsystems.shooter.getTargetRPS(), 20.0);
        assertTrue(runner.run()); // Next step, set 40 RPS and wait 0.5 seconds.
        assertEquals(subsystems.shooter.getTargetRPS(), 40.0);

        // Now waiting for 0.5 seconds.

        // Abort sequence, causing the end state to be applied when the current step finishes.
        runner.abort();

        assertTrue(runner.run()); // Still waiting for the current step to finish.
        assertEquals(subsystems.shooter.getTargetRPS(), 40.0);
        // Fast forward time to finish the current step.
        clock.incrementByMilliseconds(600);
        assertTrue(runner.run()); // Should finish applying the current state.
        assertEquals(subsystems.shooter.getTargetRPS(), 40.0);
        // Now applying the end state which includes moving the intake which will take time.
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(subsystems.intake.isRetracted());
        assertTrue(runner.run());
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertEquals(subsystems.shooter.getTargetRPS(), 100.0);
        assertFalse(subsystems.intake.isRetracted());
        assertFalse(subsystems.intake.isExtended());
        // Fast forward time to let the intake move into place.
        clock.incrementBySeconds(1);
        subsystems.intake.execute(clock.currentTimeInMillis());
        assertTrue(subsystems.intake.isExtended());

        // The runner should now detect that the intake is deployed and finish, returning false.
        assertFalse(runner.run()); // Nothing left to do, return false so it can be cleaned up.
        assertEquals(subsystems.shooter.getTargetRPS(), 100.0);
        assertTrue(subsystems.intake.isExtended());
    }
}
