package org.strongback.components;



import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;

/**
 * Wrapper for accessing values from NetworkTables.
 * If the value isn't already in the config, it will insert the supplied default.
 * Note: This will not work from within unit tests as it requires loading a
 * shared library and that will only work on the roborio :(
 */
public class NetworkTableHelperImpl implements NetworkTableHelper {
    private NetworkTable table;
    private final String tableName;

    public NetworkTableHelperImpl(String tableName) {
        this.tableName = tableName;
        table = NetworkTableInstance.getDefault().getTable(tableName);
    }

    // double
    public double get(String key, double defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
            entry.setDouble(defaultValue);
        }
        return entry.getDouble(defaultValue);
    }

    // boolean
    public boolean get(String key, boolean defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
            entry.setBoolean(defaultValue);
        }
        return entry.getBoolean(defaultValue);
    }

    // String
    public String get(String key, String defaultValue) {
        NetworkTableEntry entry = table.getEntry(key);
        if (entry.getType() == NetworkTableType.kUnassigned) {
            System.out.println("Unable to get the value for '" + key
                    + "' in table '" + tableName + "'");
            entry.setString(defaultValue);
        }
        return entry.getString(defaultValue);
    }

    /**
     * Sets a named double value from the network tables.
     * 
     * @param key the name to look up in the table.
     * @param value the value set to the network table.
     */
    @Override
    public void set(String key, double value) {
        NetworkTableEntry entry = table.getEntry(key);
        entry.setDouble(value);
    }
}
