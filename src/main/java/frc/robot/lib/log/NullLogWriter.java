package frc.robot.lib.log;



import frc.robot.interfaces.LogWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Throws away log messages. Useful for unit tests that logging
 * doesn't need to be persisted.
 */
public class NullLogWriter implements LogWriter {

    public NullLogWriter() {}

    @Override
    public void write(String contents) {
        // Only write to the console.
        System.out.print(contents);
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}

    @Override
    public void createSymbolicLink(String dir, String prefix) {}

    @Override
    public Path getLinkPath(String dir, String prefix) {
        return Paths.get("/dev/null");

    }
}
