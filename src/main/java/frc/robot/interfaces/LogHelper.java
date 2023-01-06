package frc.robot.interfaces;



import frc.robot.lib.log.Log;

public interface LogHelper {
    public String getName();

    /**
     * Logs to disk and console. For low volume informational messages (<1/sec).
     * 
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public default void info(String message, final Object... args) {
        Log.info(getName(), message, args);
    }

    /**
     * Logs to disk only. For high volume debug messages
     *
     * @param message Message to log
     * @param args arguments to the message format string
     */
    public default void debug(String message, final Object... args) {
        Log.debug(getName(), message, args);
    }

    /**
     * Logs to disk and console. For important errors Messages.
     * 
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public default void error(String message, final Object... args) {
        Log.error(getName(), message, args);
    }

    /**
     * Logs Exceptions to disk and console.
     * 
     * @param systemName Name of the subsystem (used for logging identification)
     * @param message Message to Log
     * @param e Exception to log after message
     */
    public default void exception(String message, Exception e) {
        Log.exception(getName(), message, e);
    }

    /**
     * Logs to disk and console. For warning Messages.
     * 
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public default void warning(String message, final Object... args) {
        Log.warning(getName(), message, args);
    }
}
