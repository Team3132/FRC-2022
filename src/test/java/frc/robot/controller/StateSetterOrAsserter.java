package frc.robot.controller;

/**
 * A setter or asserter to either set a state or to assert that a state is
 * as expected.
 * Only one of the above will be used, but they end up in the same class
 * so that the name can be reused.
 * Example:
 * test.thenSet(HasCube(true)); // Updates outtake to say that there is a cube.
 * test.thenAssert(HasCube(true)); // Now asserts (checks) that the outtake claims there is a cube.
 * Where HasCube() is a StateSetterOrAsserter.
 */
public interface StateSetterOrAsserter {
    // Describe what this does, eg "HasCube(true)".
    // Depending on which method is used, it will be prefixed with "set" or "assert"
    // to become "setHasCube(true)" or "assertHasCube(true)".
    public abstract String name();

    // Sets the desired configuration/state, eg calls outtake.setHasCube(true)
    public abstract void setState();

    // Asserts that the the state has been set correctly. get outtake.hasCube() == true
    public abstract void assertState() throws AssertionError;
}
