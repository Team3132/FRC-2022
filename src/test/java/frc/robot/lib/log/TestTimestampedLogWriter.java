package frc.robot.lib.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class TestTimestampedLogWriter {

    // Ensure writing works.
    @Test
    public void testWrite() throws IOException {
        Path tempDir = Files.createTempDirectory("timestamped");
        TimestampedLogWriter writer =
                new TimestampedLogWriter(tempDir.toString(), "test", 123, "txt");
        writer.write("Hello\n");
        writer.write("World!");
        writer.flush();
        Path expctedFile = Paths.get(tempDir.toString(), "data", "test_00123.txt");
        System.out.printf("Found: '" + Files.readAllLines(expctedFile));
        assertEquals(Files.readString(expctedFile), "Hello\nWorld!");
    }

    // Ensure symbolic links are created.
    @Test
    public void testSymbolicLink() throws IOException {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return; // Symbolic links aren't supported on Windows.
        }
        Path tempDir = Files.createTempDirectory("timestamped");
        TimestampedLogWriter writer =
                new TimestampedLogWriter(tempDir.toString(), "test", 123, "txt");
        writer.write("Hello\n");
        writer.write("World!");
        writer.flush();
        writer.createSymbolicLink("event", "SouthernCross_M1");
        Path expctedFile = Paths.get(tempDir.toString(), "event", "SouthernCross_M1_test.txt");
        System.out.printf("Found: '" + Files.readAllLines(expctedFile));
        assertEquals(Files.readString(expctedFile), "Hello\nWorld!");
    }

}
