package frc.robot.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.function.DoubleSupplier;

/**
 * Helper to allow a series of states that a test has to go through.
 * Each state can be either setting a but of values or asserting values.
 * 
 * Each state attempt moves the clock forward in case it takes time for
 * something to happen (like the lift to move into position or the intake
 * to open).
 */
public class TestHelper {
    private final DoubleSupplier stepFunc; // How to move time on one unit.
    private final ArrayList<Runnable> safetyFuncs = new ArrayList<Runnable>(); // Check for safety
                                                                               // violations.
    private double time = 0; // time in the test.
    // How long to keep checking before giving up on an assert.
    private final double kMaxCycleTimeSec = 10;

    /**
     * Create a new TestHelper passing in a function to call repeatedly to
     * step the time forward. May also poke the subsystems to do processing.
     * 
     * @param stepFunc poke subsystems. Returns time in seconds.
     */
    public TestHelper(DoubleSupplier stepFunc, Runnable statusPrinter) {
        this.stepFunc = stepFunc;
    }

    /**
     * Register a function to be called each step to check that the robot is
     * still in a safe state.
     * 
     * @param func function to call.
     * @return this for chaining calls.
     */
    public TestHelper registerSafetyFunc(Runnable func) {
        safetyFuncs.add(func);
        return this;
    }

    /**
     * Allows a BusinessRules unit test to define a series of steps, where
     * it is either setting state, asserting that it's in a state or waiting.
     * eg set that hasCube = true, and the next state is to assert that
     * the outtake is closed and the intake is stowed.
     */
    private class RobotState {
        StateSetterOrAsserter[] setters;
        StateSetterOrAsserter[] asserters;
        Double waitFor = null;
    }

    private ArrayList<RobotState> states = new ArrayList<RobotState>();

    /**
     * Update the robot state with one or more setters
     * eg SetHasCube(true).
     */
    public void thenSet(StateSetterOrAsserter... setters) {
        StateSetterOrAsserter[] temp = new StateSetterOrAsserter[setters.length];
        int i = 0;
        for (StateSetterOrAsserter s : setters) {
            temp[i++] = s;
        }
        thenSetArray(temp);
    }

    /**
     * Update the robot state with one or more setters, array version.
     * eg SetHasCube(true).
     */
    public void thenSetArray(StateSetterOrAsserter[] setters) {
        RobotState state = addNewState();
        state.setters = setters;
    }

    /**
     * Update the robot state with one or more setters
     * eg SetHasCube(true).
     */
    public void thenAssert(StateSetterOrAsserter... asserters) {
        RobotState state = addNewState();
        state.asserters = new StateSetterOrAsserter[asserters.length];
        int i = 0;
        for (StateSetterOrAsserter a : asserters) {
            state.asserters[i++] = a;
        }
    }

    /**
     * Delay for x virtual seconds.
     */
    public void thenWait(double seconds) {
        RobotState state = addNewState();
        state.waitFor = Double.valueOf(seconds);
    }

    private RobotState addNewState() {
        RobotState state = new RobotState();
        states.add(state);
        return state;
    }

    public boolean run() {
        // Start the clock at time zero and step through all the states, setting or
        // asserting in each one.
        for (RobotState state : states) {
            // Run the setters once.
            if (state.setters != null) {
                // This is a setter state.
                setTime(stepFunc.getAsDouble());
                printLog("------- Running setters -------");
                for (StateSetterOrAsserter setter : state.setters) {
                    printLog("  set" + setter.name());
                    setter.setState();
                }
                // Can't set and assert in the same stage.
                assertTrue(state.asserters == null);
            }
            // Attempt each assert state up to 1000 times before failing the test.
            boolean successful = false;
            StringBuilder log = new StringBuilder();
            setTime(stepFunc.getAsDouble());
            double startTime = time;
            while (time < startTime + kMaxCycleTimeSec && !successful) {
                // System.out.printf("i=%d\n", i);
                successful = true; // Pass by default.
                log.setLength(0); // Reset the log.

                // Don't loop too quickly. Let the background threads do their bit.
                try {
                    Thread.sleep(4);
                } catch (InterruptedException e1) {
                }
                // Increment the clockrun business rules and all subsystems.
                setTime(stepFunc.getAsDouble());

                // Check that the robot is still in a safe state every time.
                try {
                    for (Runnable func : safetyFuncs) {
                        func.run(); // Throw on error.
                    }
                } catch (AssertionError e) {
                    // Robot no longer safe, abort.
                    successful = false;
                    printLog("Robot safety failure: " + e.getMessage());
                    break;
                }

                // Asserts will normally fail a few times while the subsystems
                // move into place. Repeatedly try before giving up and log
                // the first successful attempt or the last failed attempt.
                try {
                    if (state.asserters != null) {
                        log.append("------- Running asserters -------\n");
                        for (StateSetterOrAsserter asserter : state.asserters) {
                            log.append("  assert" + asserter.name() + "\n");
                            asserter.assertState();
                        }
                        // Got to here, all asserts must have been successful.
                        successful = true;
                    }
                } catch (AssertionError e) {
                    // One of the asserts failed, step forward in time and try
                    // again.
                    log.append(" FAILED: " + e.getMessage() + "\n");
                    successful = false;
                }

                // If this is a wait state, only set successful when it's
                // waited long enough.
                if (state.waitFor != null) {
                    if (time < startTime + state.waitFor.doubleValue()) {
                        // Longer to wait.
                        successful = false;
                    } else {
                        // Finished waiting.
                        successful = true;
                    }
                }
            }
            // Only print the last attempt, successful or failed. Otherwise we see 100 failed
            // attempts before it gives up.
            printLog(log.toString());
            if (!successful) {
                // statusPrinter.run();
                printTestResult("FAILED");
                return false;
            }
        }
        // All asserts must have passed to get to here.
        printTestResult("PASSED");
        return true;
    }

    private void setTime(double time) {
        this.time = time;
    }

    public String formatLine(String str) {
        return String.format("%.3f       TEST: %s", time, str);
    }

    public void printLog(String str) {
        for (String line : str.split("\n")) {
            System.out.println(formatLine(line));
        }
    }

    public void printTestResult(String result) {
        printLog("");
        printLog("*************************");
        printLog("****** TEST " + result + " ******");
        printLog("*************************");
    }
}
