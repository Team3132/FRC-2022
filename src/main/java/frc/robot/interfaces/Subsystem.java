package frc.robot.interfaces;

public interface Subsystem {
    /**
     * Return the name of the subsystem
     */
    public String getName();

    /**
     * Enable the subsystem. This allows for subsystems to be enabled and disabled on the fly.
     * A disabled subsystem will not have its update() method called whilst it is disabled
     */
    public void enable();

    /**
     * Disable the subsystem. This allows for subsystems to be enabled and disabled on the fly.
     * A disabled subsystem will not have its update() method called whilst it is disabled
     */
    public void disable();

    /**
     * The run() method is called to execute the subsystem when it should be run.
     * Run checks that the subsystem is present and enabled before calling update().
     */
    public void execute(long timeInMillis);

    /**
     * @return True is the subsystem is enabled
     */
    public boolean isEnabled();

    /**
     * If a subsystem is being stopped or disabled
     */
    public void cleanup();
}
