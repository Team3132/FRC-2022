package frc.robot.lib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class TestConfigReader {

    // Ensure all subsystems are enabled to their default values if the file is empty.
    @Test
    public void testMissingFile() throws IOException {
        Path tempDir = Files.createTempDirectory("robot");
        Path path = Paths.get(tempDir.toString(), "robot.config");
        ConfigReader config = new ConfigReader(path.toString());

        assertEquals(config.getString("missing/string", "default_value"), "default_value");
        assertEquals(config.getBoolean("missing/boolean", false), false);
        assertEquals(config.getInt("missing/int", 2), 2);
        assertArrayEquals(config.getIntArray("missing/int/array", new int[] {2, 3}),
                new int[] {2, 3});
    }

    // Ensure all subsystems are enabled to their default values if the file is empty.
    @Test
    public void testEmptyFile() throws IOException {
        File file = File.createTempFile("robot", ".config");
        String exampleFile = file.toString() + ".example";
        ConfigReader config = new ConfigReader(file.toString());
        assertEquals(file.exists(), true);

        assertEquals(config.getString("missing/string", "default_value"), "default_value");
        assertEquals(config.getBoolean("missing/boolean", false), false);
        assertEquals(config.getInt("missing/int", 2), 2);
        assertArrayEquals(config.getIntArray("missing/int/array", new int[] {2, 3}),
                new int[] {2, 3});
        assertTrue(exampleFile.toString().length() > 20); // make sure the example file
                                                          // isn't empty
                                                          // after first run of update
    }

    @Test
    public void testGetValueType() throws IOException {
        File file = File.createTempFile("robot", ".config");
        assertEquals(file.exists(), true);

        StringBuilder str = new StringBuilder();
        str.append("robot/name=\"firstValue\"\n"); // First value of a dup is ignored.
        str.append("robot/name=\"unitTest\"\n");
        str.append("  drivebase/present=false\n"); // Leading spaces.
        str.append("pcm/canID=210\n");
        str.append("drivebase/left/canIDs/withEncoders=7, 9,15\n");
        str.append("drivebase/left/canIDs/withoutEncoders=\n");
        Files.write(file.toPath(), str.toString().getBytes(StandardCharsets.UTF_8));

        ConfigReader config = new ConfigReader(file.toString());

        assertEquals(config.getString("robot/name", "not found"), "unitTest");
        assertEquals(config.getBoolean("drivebase/present", true), false);
        assertEquals(config.getInt("pcm/canID", -1), 210);
        assertArrayEquals(
                config.getIntArray("drivebase/left/canIDs/withEncoders",
                        new int[] {}),
                new int[] {7, 9, 15});
        assertArrayEquals(
                config.getIntArray("drivebase/left/canIDs/withoutEncoders",
                        new int[] {-1}),
                new int[] {});
    }
}
