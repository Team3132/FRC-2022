package frc.robot.lib.log;

/*
 * This class provides easy access to logging functions, allowing debugging
 * after the program has run and persisting through robot code restarts.
 * 
 * What it does depends on what user starts it.
 * - If running as lvuser, it assumes it's running on a RoboRio and will log to
 * the flash drive for easy post match analysis.
 * - If started by any other user it will just print logging and not write
 * anything to disk (eg in unit tests)
 * 
 * For text logs we have a series of calls:
 * log.{debug,info,warning,error}(String message)
 * 
 * Each message takes a varargs argument list, and prepends the message with a
 * timestamp and type of message.
 * 
 * Static implementation so that it doesn't need to be passed around everywhere.
 */



import frc.robot.Config;
import frc.robot.interfaces.LogWriter;
import frc.robot.lib.FileUtil;
import frc.robot.lib.RobotName;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.strongback.Strongback;

/**
 * This is the log file data logger. We split the log into two separate log
 * systems. For the text file logging we use this method.
 * 
 * We create a single text log file <basePath>/<instance>.txt and provide a
 * variety of methods to append to that log file.
 */

public class Log {
    /**
     * Logs to disk only. For high volume debug messages
     * 
     * @param system Name of the subsystem (used for logging identification)
     * @param message Message to log
     * @param args arguments to the message format string
     */
    public static void debug(String system, String message, final Object... args) {
        message = String.format(timeString() + " (Debug) " + "[" + system + "] " + message + "\n",
                args);
        // Don't print it to the console.
        writer.write(message);
    }

    /**
     * Logs to disk and console. For low volume informational messages (<1/sec).
     * 
     * @param system Name of the subsystem (used for logging identification)
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public static void info(String system, String message, final Object... args) {
        message = String.format(timeString() + " (Info) " + "[" + system + "] " + message + "\n",
                args);
        // Print to the console.
        System.out.print(message);
        writer.write(message);
    }

    /**
     * Logs to disk and console. For warning Messages.
     * 
     * @param system Name of the subsystem (used for logging identification)
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public static void warning(String system, String message, final Object... args) {
        message = String.format(timeString() + " (Warning) " + "[" + system + "] " + message + "\n",
                args);
        // Print to the console.
        System.err.print(message);
        writer.write(message);
    }

    /**
     * Logs to disk and console. For important errors Messages.
     * 
     * @param system Name of the subsystem (used for logging identification)
     * @param message Message to Log
     * @param args arguments to the message format string
     */
    public static void error(String system, String message, final Object... args) {
        message = String.format(timeString() + " (Error) " + "[" + system + "] " + message + "\n",
                args);
        // Print to the console.
        System.err.print(message);
        writer.write(message);
    }

    /**
     * Logs Exceptions to disk and console.
     * 
     * @param system Name of the subsystem (used for logging identification)
     * @param message Message to Log
     * @param e Exception to log after message
     */
    public static void exception(String system, String message, Exception e) {
        error(system, " " + message + ": %s", e);
        for (final StackTraceElement frame : e.getStackTrace()) {
            error(system, "     at %s", frame.toString());
        }
    }

    /**
     * Implements the classic println interface. Single string to the console!
     * 
     * @param systemName Name of the subsystem (used for logging identification)
     * @param message Message to Log
     */
    public static void println(String system, String message, final Object... args) {
        message = String.format(timeString() + " (Output) " + "[" + system + "] " + message + "\n",
                args);
        // Print to the console.
        System.out.print(message);
        writer.write(message);
    }

    /**
     * Restart logging. Called each time robot is enabled or initialised.
     */
    public static void restartLogs() {
        System.out.println("########## Restarting robot log ##########");
        LogFileNumber.increment();
        writer.flush();
        // Make the time start at zero within the log file.
        timeOffset = Strongback.timeSystem().currentTime();
        // Create a new logger to get new files.
        writer = createWriter();
    }

    /**
     * Create the date based symbolic links. These create symbolic links from date
     * stamped version of the file to the actual file. This is a separate method as
     * we delay creating these until we are reasonably sure the date is correct.
     * 
     * @param timestamp The date to use.
     */
    public static void createDateFiles(Calendar timestamp, String matchDescription) {
        String timestampStr = new SimpleDateFormat("yyyyMMdd't'hhmmss").format(timestamp.getTime());
        Log.info("Logging", "Creating timestamp files %s", timestampStr);
        try {
            // Create links based on the timestamp.
            writer.createSymbolicLink(Config.logging.dateExtension, timestampStr);
            writer.createSymbolicLink(Config.logging.eventExtension, matchDescription);
        } catch (final Exception e) {
            System.out.printf("Error creating logging symlinks\n");
            e.printStackTrace();
        }
    }

    // Implementation only from here.

    private static double timeOffset = 0;

    // FRC log doesn't get reset when our log does so we only want to tail -2000 on the first
    // creation of the log.
    private static boolean firstRun = true;

    public static LogWriter writer = createWriter();

    private static LogWriter createWriter() {
        if (System.getProperty("user.name").equals("lvuser")) {
            // Running on the robot. Log for real.
            final long logNum = LogFileNumber.get();
            final String robotName = RobotName.get();
            final String baseDir = Paths.get(Config.logging.basePath, robotName).toString();
            try {
                // Combination of tail flags to make it unique and harder to accidentally kill other
                // tail processes.
                createFrcLog(robotName, logNum);
                return new TimestampedLogWriter(baseDir, "log", logNum, "txt");
            } catch (final IOException e) {
                System.err.println(
                        "Failed to create logger, maybe usb flash drive isn't plugged in?");
                e.printStackTrace();
                // Fall through to create a NullLogWriter();
            }
        }
        // Likely a unit test, only write to the console.
        return new NullLogWriter();
    }

    private static void createFrcLog(String robotName, long logNum) {
        String timestampStr =
                new SimpleDateFormat("yyyyMMdd't'hhmmss").format(Calendar.getInstance().getTime());
        final Path frcLogPath = Paths.get("/home/lvuser/FRC_UserProgram.log");
        final Path frcLogCopy =
                Paths.get(Config.logging.flashDrive, robotName, Config.logging.dataExtension,
                        String.format("FRC_UserProgram_%05d.log", logNum));
        final Path frcLogLatest = Paths.get(Config.logging.flashDrive, robotName,
                Config.logging.latestExtension, "Latest_FRC_UserProgram.log");
        final Path frcLogTimestamped =
                Paths.get(Config.logging.flashDrive, robotName, Config.logging.dateExtension,
                        String.format("%s_FRC_UserProgram.log", timestampStr));

        // Combination of tail flags to make it unique and harder to accidentally kill other
        // tail processes.
        runCommand("kill -9 $(ps | egrep 'tail -20+ -fq' | awk '{print $1}')");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runCommand(String.format(
                "tail -%s -fq %s > %s",
                (firstRun ? "2000" : "20"), frcLogPath.toString(), frcLogCopy.toString()));
        firstRun = false;
        FileUtil.createSymbolicLink(frcLogLatest, frcLogCopy);
        FileUtil.createSymbolicLink(frcLogTimestamped, frcLogCopy);
    }

    /*
     * Create the timestamp for this message. We use the robot time, so each log
     * entry is time stamped for when it happened during the match.
     */
    private static String timeString() {
        return String.format("%.3f", Strongback.timeSystem().currentTime() - timeOffset);
    }

    private static void runCommand(String command) {
        try {
            System.out.println("Running command: " + command);
            Runtime.getRuntime()
                    .exec(new String[] {"bash", "-c", command});
        } catch (IOException e) {
            System.err.println("Error running command: " + command);
            e.printStackTrace();
        }
    }
}
