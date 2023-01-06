package frc.robot.controller;



import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * A list of State instances that the robot should go through and an end state
 * for when the sequence is interrupted.
 * The final state is where the robot will remain once it has done all states.
 * Can be aborted at any time.
 */
public class Sequence implements Iterable<State> {
    private final String name;
    private final ArrayList<State> states;
    private final State endState;
    private final EnumSet<Domain> domains;

    private Sequence(SequenceBuilder builder) {
        this.name = builder.name;
        this.endState = builder.endState;
        this.states = builder.states;
        this.domains = builder.domains;
    }

    public String getName() {
        return name;
    }

    public Iterator<State> iterator() {
        return states.iterator();
    }

    public State getEndState() {
        return endState;
    }

    /**
     * A sequence conflicts with another sequence if they share any
     * domains/subsystems that are updated.
     * 
     * @param other the other sequence to compare it agains.
     * @return true if they share domains
     */
    public boolean doesConflict(Sequence other) {
        EnumSet<Domain> copy = domains.clone();
        copy.retainAll(other.domains);
        return copy.size() > 0;
    }

    /**
     * A list of State instances that the robot should go through.
     * Can be aborted at any time.
     * 
     * Every add() call adds a new state that the robot should achieve
     * before moving to the next state. The final state is where the robot
     * will remain once it has done all states.
     * 
     * Any value not set will cause it to not be changed. eg if we don't set the
     * lift height, the height will be unchanged.
     * 
     * An end state can be created to be applied if the sequence is interrupted,
     * either autocreated by passing createInterrupt = true
     * or manually set by calling onInterrupt();
     * 
     * Example usage for intaking a cube
     * SequenceBuilder builder = new SequenceBuilder("Intake cube", createInterrupt);
     * builder.add().setLiftHeight(0).setIntakeConfig(NARROW).setOuttakeOpen(true);
     * builder.add().setIntakeMotorOutput(1).setOuttakeHasCube(true);
     * builder.add().setOuttakeOpen(false);
     * builder.add().setIntakeMotorOutput(0).setIntakeConfig(STOWED);
     * return builder.build();
     */
    public static class SequenceBuilder {
        private final String name;
        private boolean createInterrupt = false;
        private ArrayList<State> states = new ArrayList<State>();
        private State endState = new State();
        private EnumSet<Domain> domains = EnumSet.noneOf(Domain.class);

        /**
         * SequenceBuilder create a new sequence.
         * 
         * @param name The name of this sequence.
         */
        public SequenceBuilder(String name) {
            this.name = name;
        }

        /**
         * Creates a special State to be run when the sequence is interrupted that
         * is applied to leave the robot in the state it would have been if the sequence
         * had been allowed to continue.
         */
        public SequenceBuilder createInterruptState() {
            createInterrupt = true;
            return this;
        }

        /**
         * Adds a new state at the end of the sequence.
         * 
         * @return new state
         */
        public State then() {
            states.add(new State());
            return states.get(states.size() - 1);
        }

        /**
         * Adds a new state at the end of the sequence and log a message.
         * 
         * @param debug Message to log.
         * @return new state
         */
        public State then(String debug) {
            return then().setLog(debug);
        }

        /**
         * Add another sequence to the end of this one, only copying the states.
         * 
         * @param other the sequence to add.
         */
        public void appendSequence(Sequence other) {
            states.addAll(other.states);
        }

        /**
         * Applies a state when the sequence gets interrupted
         */
        public State onInterrupt() {
            return endState;
        }

        public Sequence build() {
            for (State s : states) {
                domains.addAll(s.getDomains());
            }
            if (createInterrupt) {
                for (State s : states) {
                    endState.fillInterrupt(s);
                }
            }
            return new Sequence(this);
        }


    }
}
