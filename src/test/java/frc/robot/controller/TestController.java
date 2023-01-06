package frc.robot.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.robot.controller.Sequence.SequenceBuilder;
import frc.robot.mock.MockClimber;
import frc.robot.mock.MockConveyor;
import frc.robot.mock.MockDrivebase;
import frc.robot.mock.MockFeeder;
import frc.robot.mock.MockLEDStrip;
import frc.robot.mock.MockLocation;
import frc.robot.mock.MockShooter;
import frc.robot.simulator.IntakeSimulator;
import frc.robot.subsystems.Subsystems;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.strongback.components.ui.InputDevice;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockInputDevice;
import org.strongback.mock.MockPneumaticsModule;

/**
 * Test cases for the Controller and the Sequences
 * 
 * Mocks out almost everything so that no hardware is needed.
 */
public class TestController {
    // Use 10 ms steps between executions.
    private final long kTestStepMs = 10;
    private final long kRandomSeed = 123456;
    private final double kMaxWaitTimeSeconds = 4;
    private MockClock clock;
    private InputDevice gamepad;
    private Subsystems subsystems;
    // Store direct access to the simulators so the simulator-only
    // methods can be called.
    @SuppressWarnings("unused")
    private IntakeSimulator intake;
    private TestHelper test;
    // The bit that is being tested under test.
    private Controller exec;

    /**
     * Setup fields used by this test.
     */
    @BeforeEach
    public void setUp() {
        System.out.println("\n******************************");
        clock = new MockClock();
        gamepad = new MockInputDevice(1, 1, 1);
        subsystems = new Subsystems(clock, gamepad);

        subsystems.intake = intake = new IntakeSimulator();
        subsystems.velcroIntake = new MockFeeder("velcro");
        subsystems.pcm = new MockPneumaticsModule();
        subsystems.drivebase = new MockDrivebase();
        subsystems.conveyor = new MockConveyor();
        subsystems.shooter = new MockShooter();
        subsystems.location = new MockLocation();
        subsystems.climberLeft = new MockClimber();
        subsystems.climberRight = new MockClimber();
        subsystems.ledStrip = new MockLEDStrip();
        subsystems.feederLeft = new MockFeeder("left");
        subsystems.feederRight = new MockFeeder("right");

        exec = new Controller(subsystems);
        exec.enable();

        test = new TestHelper(() -> {
            clock.incrementByMilliseconds(kTestStepMs);
            // long now = clock.currentTimeInMillis();
            // System.out.printf("==== Cycle starting at time %.03fms ====\n", now/1000.);
            subsystems.intake.execute(clock.currentTimeInMillis());
            return clock.currentTime();
        }, () -> {
            System.out.println(subsystems.intake.toString());
        });

        // If the controller dies we should fail the test.
        test.registerSafetyFunc(
                () -> assertTrue(exec.isAlive(),
                        "The controller has died failing the test. A stack trace should be above"));
    }

    /**
     * Sequences can be aborted before they even start. In this case, they should
     * still start so they can apply their final state (if they have one) before
     * they finish.
     * 
     * We had a bug where the aborted sequence would never start because it would
     * conflict with another sequence (the one that aborted it) that also was
     * waiting to start.
     * 
     * This is very similar to TestSequenceRunner::testAbort() but tests the
     * interaction between sequences instead of the SequenceRunner behaviour.
     */
    @Test
    public void testCanStartWhenConflictingWithAbortedSequence() throws Exception {
        // Create three dummy sequences that conflict with each other.
        // The conflict is on the conveyor.
        // Once should block the controller, one that is queued to start
        // and another that should cause the queued one to abort.
        // The "start intaking" sequence should be aborted by "stop intaking"
        // being run, but before it can clean up, start shooting should be
        // queued, which should abort "stop intaking". "stop intaking" has
        // an end state defined, which should be run before "start shooting"
        // is started.
        // The original bug was that "stop intaking" would not start because
        // it would conflict with "start shooting" which was waiting on it
        // to start.
        SequenceBuilder b1 = new SequenceBuilder("start intaking");
        b1.then().setIntakeRPS(10).setConveyorDutyCycle(0.5);
        b1.then().setDelayDelta(1);
        Sequence startIntaking = b1.build();
        b1 = new SequenceBuilder("stop intaking");
        b1.then().setIntakeRPS(0).setConveyorDutyCycle(0);
        b1.createInterruptState();
        Sequence stopIntaking = b1.build();
        b1 = new SequenceBuilder("start shooting");
        b1.then().setShooterRPS(100).setConveyorDutyCycle(1);
        Sequence startShooting = b1.build();


        // Assert the expected subsystem state: intake, conveyor and shooter off.
        test.thenAssert(intakeMotorRPS(0), conveyorMotorDutyCycle(0),
                shooterMotorRPS(0));

        // Run the start intaking sequence.
        test.thenSet(sequence(startIntaking));

        // Then assert that the robot subsystems have eventually been put into the expected states.
        // Having them in one .thenAssert() means that they will all have to be true at once.
        test.thenAssert(intakeMotorRPS(10), conveyorMotorDutyCycle(0.5),
                shooterMotorRPS(0));

        // Wait for half a second so that this test hasn't yet completed.
        test.thenWait(0.5);

        // Run the stop intaking sequence which should abort the start intaking sequence
        // and it should be queued waiting for it to finish the current step.
        test.thenSet(sequence(stopIntaking));

        // Still while waiting for the start intaking sequence to finish, also try to
        // start the start shooting sequence. This should abort the stop intaking sequence
        // but as it has an end state, that should still be run.
        test.thenSet(sequence(startShooting));

        // Now move time along and check that the intake has been turned off as done
        // in the end state of the aborted stop intaking sequence and also the shooter
        // has been set to 100 RPS.
        test.thenAssert(intakeMotorRPS(0), conveyorMotorDutyCycle(1), shooterMotorRPS(100));

        // At this point we know that the stop intaking end state was run as well as the
        // full start shooting sequence.
        assertTrue(test.run());
    }

    /**
     * Example test.
     * 
     * Use this as a template when designing new tests.
     */
    /*
     * @Test
     * public void testExampleForCopying() {
     * // Update the println statement with your test name.
     * System.out.println("testExampleForCopying");
     * 
     * // Setup initial subsystem state. Lift starts at the bottom and the intake stowed.
     * // Set what you need for the test here. Once the subsystems have been set up,
     * // then the test will move on to the next thenSet(), thenAssert() or thenWait()
     * // statement.
     * test.thenAssert(outtakeOpen(true), intakeMotorPower(0),
     * liftHeight(LiftPosition.INTAKE_POSITION));
     * 
     * // Run the intaking sequence.
     * test.thenSet(sequence(Sequences.getStartIntakingSequence()));
     * 
     * // Then assert that the robot subsystems have eventually been put into the expected states.
     * // Having them in one .thenAssert() means that they will all have to be true at once.
     * // In this case the intake should be in the narrow configuration, the outtake should be open,
     * // the intake motor should have full power forward and the lift should be in the intake
     * position.
     * test.thenAssert(outtakeOpen(true), intakeMotorPower(1),
     * liftHeight(LiftPosition.INTAKE_POSITION));
     * 
     * // The test can then be told to do nothing for some number of seconds.
     * test.thenWait(0.5);
     * 
     * // Then you can tell the robot that a cube was detected in the outtake, which
     * // may make the sequence move on to the next state.
     * // NB, this sensor has been removed, so hasCube() is no longer an option.
     * test.thenSet(hasCube(true));
     * 
     * // And then make the test check that the subsystems are updated to be in the correct state.
     * test.thenAssert(outtakeOpen(false), intakeMotorPower(0),
     * liftHeight(LiftPosition.INTAKE_POSITION));
     * 
     * // Walk through setting the states and asserting that the robot eventually
     * // moves through the required state.
     * // This line executes the steps set up above. Note adding println statements
     * // will print out the statements when the test is setup, not as it moves through
     * // the states.
     * assertTrue(test.run());
     * }
     */

    /**
     * Pretends to be a crazy operator that keeps changing their mind.
     * 
     * This allows it to check that the robot is always in a safe configuration
     * as the safety checker is checking the state of the robot every time.
     * It sleeps for a random amount of time between desired state changes to
     * allow the robot to get either fully into the new state, or part way.
     * 
     * There is no checking that the robot is actually doing anything useful
     * here, only that it doesn't hurt itself.
     */
    @Test
    public void testCrazyOperatorFuzzTest() {
        System.out.println("testCrazyOperatorFuzzTest");
        // Seed the random number generator so that the same
        // random numbers are generated every time.
        Random generator = new Random(kRandomSeed);

        // Build a large number random steps.
        for (int i = 0; i < 10 /* 0 */; i++) {
            // Ask for a random desired state.
            test.thenSet(sequence(getRandomDesiredSequence(generator)));
            test.thenWait(generator.nextDouble() * kMaxWaitTimeSeconds);
        }

        // Walk through setting the states and asserting that the robot eventually
        // moves through the required state.
        assertTrue(test.run());
    }


    // Helpers only from this point onwards.

    private Sequence getRandomDesiredSequence(Random generator) {
        return Sequences.allSequences[generator.nextInt(Sequences.allSequences.length)];
    }

    /**
     * Tells the Controller to run the desired sequence. Only makes sense in a
     * thenSet(), not a thenAssert().
     * 
     * @param sequence the sequence to execute.
     * @return a setter or asserter object to pass to the TestHelper.
     */
    private StateSetterOrAsserter sequence(Sequence sequence) {
        return new StateSetterOrAsserter() {
            @Override
            public String name() {
                return String.format("Sequence(%s)", sequence.getName());
            }

            @Override
            public void setState() {
                exec.run(sequence);
            }

            @Override
            public void assertState() throws AssertionError {
                throw new AssertionError("Invalid usage of sequence() in thenAssert()");
            }
        };
    }

    /**
     * Either sets the shooter motor rps, OR asserts the rps the motor has
     * been set to, depending if it's in a thenSet() or a thenAssert().
     * 
     * @param rps to set/expect to/from the motor.
     * @return a setter or asserter object to pass to the TestHelper.
     */
    private StateSetterOrAsserter shooterMotorRPS(double rps) {
        return new StateSetterOrAsserter() {
            @Override
            public String name() {
                return String.format("ShooterMotorRPS(%.1f)", rps);
            }

            @Override
            public void setState() {
                subsystems.shooter.setTargetRPS(rps);
            }

            @Override
            public void assertState() throws AssertionError {
                if (Math.abs(subsystems.shooter.getTargetRPS() - rps) > 0.1) {
                    throw new AssertionError(
                            "Expected shooter motor to have rps " + rps + " but it is "
                                    + subsystems.shooter.getTargetRPS());
                }
            }
        };
    }

    /**
     * Either sets the intake motor rps, OR asserts the rps the motor has
     * been set to, depending if it's in a thenSet() or a thenAssert().
     * 
     * @param rps to set/expect to/from the motor.
     * @return a setter or asserter object to pass to the TestHelper.
     */
    private StateSetterOrAsserter intakeMotorRPS(double rps) {
        return new StateSetterOrAsserter() {
            @Override
            public String name() {
                return String.format("IntakeMotorRPS(%.1f)", rps);
            }

            @Override
            public void setState() {
                subsystems.intake.setTargetRPS(rps);
            }

            @Override
            public void assertState() throws AssertionError {
                if (Math.abs(subsystems.intake.getTargetRPS() - rps) > 0.1) {
                    throw new AssertionError(
                            "Expected intake motor to have rps " + rps + " but it is "
                                    + subsystems.intake.getTargetRPS());
                }
            }
        };
    }

    /**
     * Either sets the conveyor motor duty cycle, OR asserts the duty the motor has
     * been set to, depending if it's in a thenSet() or a thenAssert().
     * 
     * @param dutyCycle to set/expect to/from the motor.
     * @return a setter or asserter object to pass to the TestHelper.
     */
    private StateSetterOrAsserter conveyorMotorDutyCycle(double dutyCycle) {
        return new StateSetterOrAsserter() {
            @Override
            public String name() {
                return String.format("ConveyorMotorDutyCycle(%.1f)", dutyCycle);
            }

            @Override
            public void setState() {
                subsystems.conveyor.setDutyCycle(dutyCycle);
            }

            @Override
            public void assertState() throws AssertionError {
                if (Math.abs(subsystems.conveyor.getDutyCycle() - dutyCycle) > 0.1) {
                    throw new AssertionError(
                            "Expected conveyor motor to have duty cycle " + dutyCycle
                                    + " but it is "
                                    + subsystems.conveyor.getDutyCycle());
                }
            }
        };
    }
}
