package frc.robot.interfaces;



import java.nio.file.Path;

/**
 * Simple log writer.
 */

public interface LogWriter {

    /**
     * Writes a single line to a log file.
     * 
     * @param message Message to write to the log.
     */
    public void write(String message);

    /**
     * Flush any writes to disk.
     */
    public void flush();

    /**
     * Close the file and clean up.
     */
    public void close();

    /**
     * Create symbolic links to the file. Used to create Latest_x and dated
     * symlinks. This version also takes a directory.
     * 
     * @param dir sub directory relative to logging base dir to put link in.
     * @param prefix as in <prefix>_chart.html
     */
    public void createSymbolicLink(String dir, String prefix);

    /**
     * Get the path of the latest log file
     */
    public Path getLinkPath(String dir, String prefix);
}
