package frc.robot.lib;



import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    /**
     * Create symbolic links to the file. Used to create Latest_x and dated
     * symlinks.
     * 
     * @param from the full path to the symlink.
     * @param to the full path to the file to link to.
     */
    public static void createSymbolicLink(Path from, Path to) {
        Path relPath = createRelativePath(from, to);
        try {
            // Ensure the parent directory exists.
            Files.createDirectories(from.getParent());
            Files.deleteIfExists(from);
            Files.createSymbolicLink(from, relPath);
        } catch (Exception e) {
            // e.printStackTrace();
            System.err.printf("Failed to create symbolic link: Are we on windows? %s", e);
        }
    }

    /**
     * Does some relative path magic to ensure that the target is
     * relative to the file so that it will work no matter where the file system
     * is mounted.
     * 
     * Examples:
     * prefix="Latest" creates a symlink "Latest_chart.html" -> "data/chart_00007.html"
     * prefix="date/20180303" creates a symlink "date/20180303_chart.html" ->
     * "../data/chart_00007.html"
     * 
     * @param from the full path to the symlink.
     * @param to the full path to the file to link to.
     * @return the relative path to the file to link to from <code>from</code>'s perspective
     */
    private static Path createRelativePath(Path from, Path to) {
        // Make a relative path out of this path so the symbolic link will continue to
        // work no matter where the USB flash drive is mounted.
        return from.getParent().relativize(to);
    }
}
