package frc.robot.lib;



import frc.robot.Config;
import frc.robot.interfaces.LogHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.strongback.components.PIDF;

/**
 * Class responsible for reading parameters to control behaviour of robot hardware.
 * (e.g. if subsystems are present or not) It reads from a text file
 * Currently the supported types are String, int, double, boolean and int array.
 * 
 * Example lines:
 * drivebase/present = true
 * drivebase/rampRate = 0.13125
 * drivebase/right/canIDs/withEncoders = 7,6
 * drivebase/right/canIDs/withoutEncoders = 5
 */
public class ConfigReader implements LogHelper {

    private String name = "RobotConfig";
    private String filePath;
    private Map<String, String> lines;
    private Map<String, String> ignoredEntries; // Lines/entries not used in the config file.
    private Map<String, String> nonDefaultParameters; // Non-default values used from the config
                                                      // file.
    private ArrayList<String> exampleText = new ArrayList<String>();

    public ConfigReader() {
        this(Config.config.configFilePath);
    }

    public ConfigReader(String filePath) {
        this.filePath = filePath;

        readLines(Paths.get(filePath));
    }

    /**
     * Needs to be called after the config is loaded to write out an example config
     * file and to print out details about the config file.
     */
    public void finishLoadingConfig() {
        Collections.sort(exampleText);
        writeExampleFile(filePath, String.join("\n", exampleText));

        if (!ignoredEntries.isEmpty()) {
            warning("These config file lines weren't used:");
            for (String entry : ignoredEntries.values()) {
                warning("  %s", entry);
            }
        }
        if (!nonDefaultParameters.isEmpty()) {
            warning("These parameters have non-default values:");
            for (Map.Entry<String, String> entry : nonDefaultParameters.entrySet()) {
                warning(entry.getKey(), entry.getValue());
            }
        }
        info("RobotConfig finished loading parameters");
    }

    private void readLines(Path path) {
        info("Reading config file " + path);
        lines = new HashMap<String, String>();
        ignoredEntries = new TreeMap<String, String>();
        nonDefaultParameters = new TreeMap<String, String>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s*=\\s*", -1); // Keep empty values
                if (parts.length < 2) {
                    error("Bad config line " + line);
                    continue;
                }
                String tag = parts[0].trim();
                String value = parts[1].trim();
                if (lines.containsKey(tag)) {
                    error(" Duplicate tag %s in configuration file, last value will be used.", tag);
                }
                lines.put(tag, value);
                ignoredEntries.put(parts[0].trim(), line);
            }
        } catch (NoSuchFileException e) {
            error("Config file %s not found, attempting to create it", path);
            // Touch the file so at least it's there next time.
            try {
                Files.createFile(path);
            } catch (IOException e1) {
            }
        } catch (IOException e) {
            exception("Error loading configuration file " + path + ", using defaults", e);
        }
    }

    private void writeExampleFile(String filePath, String contents) {
        Path exampleFile = Paths.get(filePath + ".example");
        try {
            BufferedWriter writer;
            writer = Files.newBufferedWriter(exampleFile, StandardOpenOption.CREATE);
            writer.write(contents + "\n");
            writer.close();
            info("Wrote example config file " + exampleFile.toString());
        } catch (IOException e) {
            exception("Unable to write example config file " + exampleFile.toString(), e);
        }
    }

    public String getMotorControllerType(String parameterName, String defaultValue) {
        String type = getString(parameterName, defaultValue);
        switch (type) {
            default:
                error("Invalid value '%s' for parameter '%s'.  Using %s.", type, parameterName,
                        Config.motorController.defaultType);
                return Config.motorController.defaultType;

            case Config.motorController.talonSRX:
                return Config.motorController.talonSRX;

            case Config.motorController.sparkMAX:
                return Config.motorController.sparkMAX;
        }
    }

    private <T> void appendExample(String key, T defaultValue) {
        exampleText.add(key + " = " + defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        appendExample(key, defaultValue);
        try {
            if (lines.containsKey(key)) {
                int value = Integer.valueOf(lines.get(key));
                ignoredEntries.remove(key); // Used this line.
                debug("%s: %s -> %d", name, key, value);
                if (value != defaultValue) {
                    nonDefaultParameters.put(key, lines.get(key));
                }
                return value;
            }
        } catch (Exception e) {
            exception("Error reading key: " + key + " using default", e);
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        appendExample(key, defaultValue);
        try {
            if (lines.containsKey(key)) {
                double value = Double.valueOf(lines.get(key));
                ignoredEntries.remove(key); // Used this line.
                debug("%s: %s -> %f", name, key, value);
                if (value != defaultValue) {
                    nonDefaultParameters.put(key, lines.get(key));
                }
                return value;
            }
        } catch (Exception e) {
            exception("Error reading key: " + key + " using default", e);
        }
        return defaultValue;
    }

    public PIDF getPIDF(String prefix, PIDF pidf) {
        pidf.p = getDouble(prefix + "/p", pidf.p);
        pidf.i = getDouble(prefix + "/i", pidf.i);
        pidf.d = getDouble(prefix + "/d", pidf.d);
        pidf.f = getDouble(prefix + "/f", pidf.f);
        return pidf;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        appendExample(key, defaultValue);
        try {
            if (lines.containsKey(key)) {
                boolean value = Boolean.valueOf(lines.get(key));
                ignoredEntries.remove(key); // Used this line.
                debug("Config %s: %s -> %s", name, key, value);
                if (value != defaultValue) {
                    nonDefaultParameters.put(key, lines.get(key));
                }
                return value;
            }
        } catch (Exception e) {
            exception("Error reading key: " + key + " using default", e);
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        appendExample(key, "\"" + defaultValue + "\"");
        try {
            if (lines.containsKey(key)) {
                // Get the value between the quotes.
                String[] parts = lines.get(key).split("\"", -1);
                if (parts.length < 3) {
                    error("Bad string value for %s, needs to be in double quotes, not: %s", key,
                            lines.get(key));
                    return defaultValue;
                }
                String value = parts[1];
                ignoredEntries.remove(key); // Used this line.
                debug("%s: %s -> %s", name, key, value);
                if (!value.equals(defaultValue)) {
                    nonDefaultParameters.put(key, lines.get(key));
                }
                return value;
            }
        } catch (Exception e) {
            exception("Error reading key: " + key + " using default", e);
        }
        return defaultValue;
    }

    public int[] getIntArray(String key, int[] defaultValue) {
        // Joining primitive arrays seems to be painful under Java.
        appendExample(key, joinIntArray(defaultValue));
        try {
            if (lines.containsKey(key)) {
                String value = lines.get(key);
                int[] values;
                if (value.equals("")) {
                    // No values.
                    values = new int[0];
                } else {
                    // One or more values.
                    String[] parts = value.split("\\s*,\\s*");
                    values = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        values[i] = Integer.valueOf(parts[i]);
                    }
                }
                ignoredEntries.remove(key); // Used this line.
                debug("%s: %s -> %s", name, key, joinIntArray(values));
                if (!java.util.Arrays.equals(values, defaultValue)) {
                    nonDefaultParameters.put(key, lines.get(key));
                }
                return values;
            }
        } catch (Exception e) {
            exception("Error reading key: " + key + " using default", e);
        }
        return defaultValue;
    }

    private static String joinIntArray(int[] values) {
        return Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
    }

    @Override
    public String getName() {
        return "Config";
    }
}
