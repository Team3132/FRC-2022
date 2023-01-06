package frc.robot.controller;



import frc.robot.interfaces.LogHelper;
import frc.robot.lib.LEDColour;
import frc.robot.subsystems.Subsystems;
import java.util.Iterator;
import org.strongback.components.Clock;

/**
 * Handles the logic of starting, running and aborting a single Sequence.
 * Used by the Controller to execute multiple sequences in parallel.
 */
public class SequenceRunner implements LogHelper {
    protected final Sequence sequence;
    private Iterator<State> iterator;
    protected RunStatus status = RunStatus.WAITING_TO_START;
    private State desiredState;
    private final Clock clock;
    private final Subsystems subsystems;
    private double stateStartTimeSec = 0;
    private double nextLogTimeSec = 0;
    private double timeBetweenLogsSec = 0.25;
    private String blockedBy = "";
    private boolean firstApplyState = true;

    enum RunStatus {
        WAITING_TO_START, // Waiting for to be allowed to start
        RUNNING, // Running normally
        WAITING_TO_START_THEN_ABORT, // When sequence is allowed to run, it should immediately abort
        ABORTING, // Waiting for current step to finish before applying the end state.
        ABORTED, // The end state is being applied
        FINISHED // The sequence is done.
    }

    public SequenceRunner(Sequence sequence, Clock clock, Subsystems subsystems) {
        this.sequence = sequence;
        this.clock = clock;
        this.subsystems = subsystems;
        debug("Sequence %s queued to start", sequence.getName());
    }

    /**
     * Check if this sequence conflicts with another sequence by using the
     * any of the same domains/subsystems.
     * 
     * @param other the other Runner/Sequence to check.
     * @return true if conflicts
     */
    synchronized public boolean doesConflict(SequenceRunner other) {
        if (this == other) {
            return false; // Can't conflict with itself
        }
        return sequence.doesConflict(other.sequence);
    }

    /**
     * Tells this runner that next time run() is called it can start executing the Sequence.
     */
    synchronized public void start() throws Exception {
        switch (status) {
            case WAITING_TO_START:
                status = RunStatus.RUNNING;
                debug("Sequence %s started", sequence.getName());
                break;
            case WAITING_TO_START_THEN_ABORT:
                status = RunStatus.ABORTING;
                debug("Starting aborted sequence %s", sequence.getName());
                break;
            default:
                throw new Exception("Start called on sequence " + sequence.getName()
                        + " in unexpected state " + status);
        }
        iterator = sequence.iterator();
        next();
    }

    /**
     * Move to the next step in the current sequence or, if aborted,
     * return finished.
     * 
     * @return true if there is another step to be executed.
     */
    synchronized public boolean next() {
        if (!iterator.hasNext()) {
            debug("Sequence %s is complete", sequence.getName());
            status = RunStatus.FINISHED;
            return false;
        }
        switch (status) {
            case FINISHED:
                return false;
            case WAITING_TO_START:
            case WAITING_TO_START_THEN_ABORT:
                return true;
            case ABORTING:
                // The step that was running when the sequence was aborted
                // has now finished applying. Apply the end state.
                status = RunStatus.ABORTED;
                setDesiredState(sequence.getEndState());
                return true;
            case ABORTED:
                // The end state has been fully applied. Change status to finished.
                status = RunStatus.FINISHED;
                return false;
            case RUNNING:
                // Normal running, move to the next state.
                setDesiredState(iterator.next());
                break;
        }
        return true;
    }

    /**
     * Change the desired state and set the other tracking parameters.
     * 
     * @param state the state to change to.
     */
    private void setDesiredState(State state) {
        desiredState = state;
        blockedBy = "";
        stateStartTimeSec = clock.currentTime();
        timeBetweenLogsSec = 0.25;
        nextLogTimeSec = stateStartTimeSec + timeBetweenLogsSec;
        firstApplyState = true;
    }

    /**
     * Aborts the current sequence by allowing the current state to apply and
     * then applies the sequences end state.
     */
    synchronized public void abort() {
        switch (status) {
            case FINISHED:
            case ABORTING:
            case WAITING_TO_START_THEN_ABORT:
            case ABORTED:
                return;
            case WAITING_TO_START:
                // Needs to apply end state
                debug("Aborting queued sequence %s", sequence.getName());
                status = RunStatus.WAITING_TO_START_THEN_ABORT;
                return;
            case RUNNING:
                debug("Aborting sequence %s", sequence.getName());
                status = RunStatus.ABORTING;
                break;
        }
    }

    /**
     * run() attempts to apply the current state. If that succeeds it will move
     * to the next state so that can be applied next time.
     * 
     * @return true if the sequence is still running.
     */
    synchronized public boolean run() {
        switch (status) {
            case WAITING_TO_START:
            case WAITING_TO_START_THEN_ABORT:
                return true;
            case FINISHED:
                return false;
            case RUNNING:
            case ABORTING:
            case ABORTED:
                try {
                    // Try to apply the current state to the subsystems and see if they all
                    // managed to apply it. Returns true if it was successful.
                    if (tryApplyState()) {
                        // The current state was successfully applied, go to the next state.
                        subsystems.ledStrip.setAlliance();
                        // next() returns false if the sequence is finished.
                        return next();
                    }
                } catch (Exception e) {
                    exception("Exception thrown trying to apply state", e);
                    return false; // Stop the processing of this sequence.
                }
                break;
        }
        // A subsystem needs more time.
        return true;
    }

    /**
     * Does the simple, dumb and most importantly, safe thing.
     * Note: This years robot doesn't have any subsystems that can impact others,
     * so no logic is needed here to protect from dangerous states.
     * 
     * Note if the step asks for something which will cause harm to the robot, the
     * request will be ignored. For example if the lift was moved into a position
     * the intake could hit it and then the intake was moved into the lift, the
     * intake move would be ignored.
     * 
     * This returns UNBLOCKED if the state was completely applied, so it will need to
     * be repeatedly called until it returns null.
     * 
     * This relies on conflicting subsystems being part of the same domain so
     * that sequences that use either of them will conflict and abort the previous sequence.
     * 
     * @throws Exception Some unhandled case (null state etc) was experienced.
     * @return true if the state was successfully applied, false if more time is needed.
     */
    synchronized private boolean tryApplyState() throws Exception {
        boolean isFirstApplyState = firstApplyState;
        firstApplyState = false;

        if (status != RunStatus.RUNNING && status != RunStatus.ABORTING
                && status != RunStatus.ABORTED) {
            throw new Exception("SequenceRunner(" + sequence.getName() + ") is in unexpected state "
                    + status.name());
        }

        if (desiredState == null) {
            throw new Exception("desiredState is null");
        }

        if (isFirstApplyState) {
            debug("Applying requested state: %s", desiredState);
            if (desiredState.logString != null) {
                info("State:");
                info("State: %s ", desiredState.logString);
                info("State:");
            }
        }

        // First we tell the subsystems what they should do so they can do it in parallel before
        // checking that they have finished.

        // Start driving if necessary.
        if (desiredState.drive != null) {
            subsystems.drivebase.setDriveRoutine(desiredState.drive);
        }
        if (desiredState.currentPose != null) {
            subsystems.location.setCurrentPose(desiredState.currentPose);
        }

        // Intake
        if (desiredState.intakePosition != null) {
            subsystems.intake.setPosition(desiredState.intakePosition);
        }
        if (desiredState.intakeRPS != null) {
            subsystems.intake.setTargetRPS(desiredState.intakeRPS);
        }
        if (desiredState.velcroDutyCycle != null) {
            subsystems.velcroIntake.setDutyCycle(desiredState.velcroDutyCycle);
        }

        // Conveyor
        if (desiredState.conveyorDutyCycle != null) {
            subsystems.conveyor.setDutyCycle(desiredState.conveyorDutyCycle);
        }

        // Shooter
        if (desiredState.shooterRPS != null) {
            subsystems.shooter.setTargetRPS(desiredState.shooterRPS);
        }
        if (desiredState.feederLeftDutyCycle != null) {
            subsystems.feederLeft.setDutyCycle(desiredState.feederLeftDutyCycle);
        }
        if (desiredState.feederRightDutyCycle != null) {
            subsystems.feederRight.setDutyCycle(desiredState.feederRightDutyCycle);
        }
        if (desiredState.hoodAngle != null) {
            subsystems.shooter.setHoodTargetAngle(desiredState.hoodAngle);
        }

        // LED
        if (desiredState.ledColour != null) {
            subsystems.ledStrip.setColour(desiredState.ledColour);
        }

        // Climber
        if (desiredState.climberLeftDutyCycle != null) {
            subsystems.climberLeft.setDutyCycle(desiredState.climberLeftDutyCycle);
        }
        if (desiredState.climberRightDutyCycle != null) {
            subsystems.climberRight.setDutyCycle(desiredState.climberRightDutyCycle);
        }

        // Gamepad Rumble
        if (desiredState.gamepadRumbleIntensity != null) {
            this.subsystems.gamepad.setRumbleLeft(desiredState.gamepadRumbleIntensity);
            this.subsystems.gamepad.setRumbleRight(desiredState.gamepadRumbleIntensity);
        }

        // subsystems.jevois.setCameraMode(desiredState.cameraMode);


        // Check which subsystems have yet to finish applying the requested state.

        // Intake
        if (notFinished(desiredState.intakePosition, subsystems.intake.isInPosition(),
                LEDColour.YELLOW, "intake")) {
            return false; // It's not yet in position
        }

        // Shooter speed
        if (notFinished(desiredState.shooterUpToSpeed, subsystems.shooter.isAtTargetSpeed(),
                LEDColour.PURPLE, "shooter wheel")) {
            return false;
        }

        // Shooter hood
        if (notFinished(desiredState.hoodAtTarget,
                subsystems.shooter.isHoodAtTargetAngle(),
                LEDColour.MAGENTA, "shooter hood")) {
            return false;
        }

        // Drivebase
        if (notFinished(desiredState.drive, subsystems.drivebase.hasFinished(),
                LEDColour.CYAN, "driving")) {
            return false; // Still driving
        }

        // Last thing: wait for the delay time if it's set.
        // The time beyond which we are allowed to move onto the next state
        if (desiredState.timeAction != null) {
            double endTime = desiredState.timeAction.calculateEndTime(stateStartTimeSec);
            if (notFinished(desiredState.timeAction, clock.currentTime() >= endTime,
                    LEDColour.ORANGE, "time")) {
                return false; // Waiting for the required amount of time.
            }
        }

        blockedBy = "";
        return true; // Not waiting on anything, can move to the next state.
    }

    /**
     * Helper to check if a subsystem has finished applying a change.
     * 
     * @param expected if null, then don't check this subsystem.
     * @param finished if the subsystem has finished applying any change
     * @param colour what colour to set the LED strip to if the subsystem hasn't finished
     * @param subsystem the name of the subsystem that is being checked
     * @return true if this subsystem is still applying the change and more time is needed
     */
    private <T> boolean notFinished(T expected, boolean finished, LEDColour colour,
            String subsystem) {
        if (expected == null || finished) {
            return false; // No change expected or finished applying change
        }
        // We're waiting for the subsystem to finish the requested update.

        // Set the LED colour to reflect what the robot is blocked by.
        // This won't work well if there are multiple sequences running, but
        // if something is blocked for a long time, then the other sequences
        // will finish and leave the LED strip alone.
        subsystems.ledStrip.setColour(colour);

        // More time is needed to apply this state.
        // Check to see if it should log a message of what it is blocked on.
        double now = clock.currentTime();
        if (now > nextLogTimeSec) {
            debug("Waiting on %s, has waited %.1f secs so far", subsystem,
                    now - stateStartTimeSec);
            timeBetweenLogsSec *= 2; // Wait longer and longer between updates.
            nextLogTimeSec = now + timeBetweenLogsSec;
        }
        // A subsystem needs more time, record it for the smart dashboard.
        blockedBy = subsystem;
        return true;
    }

    // This Sequence has yet to be started, possibly because it's waiting for other
    // sequences to finish running.
    synchronized public boolean isWaitingToStart() {
        return status == RunStatus.WAITING_TO_START
                || status == RunStatus.WAITING_TO_START_THEN_ABORT;
    }

    synchronized public boolean isRunning() {
        return status == RunStatus.RUNNING
                || status == RunStatus.ABORTING
                || status == RunStatus.ABORTED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SequenceRunner) {
            SequenceRunner runner = (SequenceRunner) other;
            return runner.sequence == sequence;
        }
        return false;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    @Override
    public String getName() {
        return sequence.getName();
    }
}
