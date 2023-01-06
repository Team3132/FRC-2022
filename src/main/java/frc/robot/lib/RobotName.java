package frc.robot.lib;



import frc.robot.Config;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Read a speciall file on the filesystem to get the robot name.
 * 
 * The logging and charting files will put their files in a subdirectory of
 * this name so that multiple robots worth of log files can be overlaid with
 * rsync and not overwrite each other.
 */
public class RobotName {
    private static String name = init(Config.config.robotNameFilePath);

    /**
     * Attempts to read the first line of a file to get the robot name. If the file
     * doesn't exist it will create a new random name, write the file and return
     * that.
     * 
     * @param path location on the filesystem to look. Normally on a usb flash drive
     * @return name of the robot as a String.
     */
    public static String init(String path) {
        String robotName;
        var filename = Paths.get(path);
        try {
            robotName = new String(Files.readAllBytes(filename));
            // Take the first line.
            robotName = robotName.split("\n")[0];
            System.out.println("Robot name: " + robotName);
            return robotName;
        } catch (Exception e) {
            // File doesn't exist, let's make a random name so it doesn't
            // conflict with any other existing robot.
            Random rand = new Random();
            robotName = String.format("noname%06d", rand.nextInt(10000));
            FileWriter f;
            try {
                f = new FileWriter(filename.toString());
                f.write(robotName);
                f.close();
            } catch (IOException e1) {
                System.err.printf("Failed to get/create a robot name, giving up");
                return "badrobot";
            }
        }
        return robotName;
    }

    /**
     * Attempts to read the first line of a file to get the robot name. If the file
     * doesn't exist it will create a new random name, write the file and return
     * that.
     * 
     * @return name of the robot as a String.
     */
    public static String get() {
        return name;
    }
}
