package frc.robot.lib.log;



import frc.robot.interfaces.LogWriter;
import frc.robot.lib.FileUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates the files and the symbolic links for a single stream of data
 * normally on the USB flash drive.
 *
 * 'Latest' symlink created to point to this file.
 */
public class TimestampedLogWriter implements LogWriter {

    private final String name; // eg chart
    private final String extn; // eg "html" or "csv"
    private final String baseDir; // All logs are below this directory
    private Path filePath = null;
    private BufferedWriter writer = null;

    /**
     * Write free form data to a file and create multiple symbolic links to it.
     * Used for csv and graphing files.
     * 
     * Creates
     * 
     * <pre>
     *    baseDir/
     *            data/name_000filenum.extn
     *            latest/Latest_name.extn -> data/name_filenum.extn
     *            date/timestamp_name.extn -> ../data/name_filenum.extn 
     *			  event/event_match_name.extn -> ../data/name_filenum.extn
     * </pre>
     * 
     * @param baseDir Where on the file system to put the logging directories.
     * @param name the type of data, eg "data", "chart"
     * @param filenum the number of the file. Incremented every start of the code.
     * @param extn the file extension
     * @throws IOException
     */
    public TimestampedLogWriter(String baseDir, String name, long filenum, String extn)
            throws IOException {
        this.baseDir = baseDir;
        this.name = name;
        this.extn = extn;
        // The absolute path to the data file so we can write to the file.
        filePath = Paths.get(baseDir, "data", String.format("%s_%05d.%s", name, filenum, extn));
        // Ensure the parent directory exists.
        Files.createDirectories(filePath.getParent());
        // Create the file writer.
        writer = Files.newBufferedWriter(filePath);
        createSymbolicLink("latest", "Latest");
    }

    /**
     * Create symbolic links to the file. Used to create Latest_x and dated
     * symlinks. This version also takes a directory.
     * 
     * @param dir sub directory relative to logging base dir to put link in.
     * @param prefix as in <prefix>_chart.html
     */
    @Override
    public void createSymbolicLink(String dir, String prefix) {
        Path symlinkPath = getLinkPath(dir, prefix);
        FileUtil.createSymbolicLink(symlinkPath, filePath);
    }

    @Override
    public void write(String contents) {
        if (writer == null)
            return; // File logging not enabled.
        try {
            writer.write(contents);
            writer.flush();
        } catch (Exception e) {
            // nothing to do. If we can't write to the log file it's not a disaster.
        }
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            // nothing to do. If we can't write to the log file it's not a disaster.
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Path getLinkPath(String dir, String prefix) {
        return Paths.get(baseDir, dir, String.format("%s_%s.%s", prefix, name, extn));
    }
}
