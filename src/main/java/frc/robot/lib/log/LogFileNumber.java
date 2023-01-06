package frc.robot.lib.log;



import frc.robot.Config;
import frc.robot.lib.RobotName;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Returns the next unique log file number to write to.
 * 
 * To make the log files easier to find they should to be timestamped so that
 * the run at a specific time can be found.
 * 
 * The roborio doesn't have a battery powered clock so when it first starts it
 * doesn't have the time and it will start from the same timestamp every time.
 * The time is supplied by the drivers station when it connects.
 * 
 * Unfortunately the code (and the logging of its output) will start well before
 * this.
 * 
 * To work around this, the logs are witten to unique incrementing file names
 * and when the real time is set on the roborio when the drivers station
 * connects a symbolic link (think shortcut) is created named with the current
 * time. We also know the match number at that time, so another link is created
 * based on the match number.
 * 
 * This class supplies the number that makes the incrementing number.
 * 
 * The different log files (chart, etc) all share the same number and they are
 * all recreated with a new number when the robot is enabled so that output of
 * every enable is in a new file.
 */
public class LogFileNumber {
    static long value = 0;

    static {
        // Increment and get the number from disk.
        increment();
    }

    /**
     * Returns the current unique log file number.
     * 
     * @return the log file number to use.
     */
    public static long get() {
        return value;
    }

    /**
     * Reads the file and increments (or uses 1 if no such file exists) and writes
     * the log file number back out.
     */
    public static void increment() {
        var path = Paths.get(Config.logging.basePath, RobotName.get(), "lognumber.txt");
        try {
            BufferedReader br = Files.newBufferedReader(path);
            String s = br.readLine(); // read the line into the buffer.
            value = Integer.parseInt(s);
            br.close();
            value++;
        } catch (IOException | NumberFormatException e) {
            System.err.printf("Cannot read %s. Resetting log number to 1.\n", path.toString());
            value = 1;
        }
        try {
            // Ensure the parent directory exists.
            Files.createDirectories(path.getParent());
            BufferedWriter bw = Files.newBufferedWriter(path);
            bw.write(value + "\n");
            bw.flush();
            bw.close();
            System.out.printf("Wrote %d to %s\n", value, path);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot write log number file. Possible old file overwrite.");
        }
    }
}
