package frc.robot.lib.chart;



import frc.robot.Config;
import frc.robot.interfaces.LogWriter;
import frc.robot.lib.RobotName;
import frc.robot.lib.log.Log;
import frc.robot.lib.log.LogFileNumber;
import frc.robot.lib.log.NullLogWriter;
import frc.robot.lib.log.TimestampedLogWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.strongback.Executable;
import org.strongback.Strongback;
import org.strongback.components.Switch;
import org.strongback.components.ui.DirectionalAxis;

/**
 * Writes data out in a CSV file for charting. Creates html files for
 * viewing the data via the web server.
 */
public class Chart implements Executable {

    /**
     * Register a data stream for collection to the log.
     * The sample needs to have a method that returns a double which is the data element.
     * The format (and args) are used to construct the name of the data stream.
     * 
     * @param sample An object that returns a double value when called
     * @param format a VarArgs string which evaluates to the name of the data stream
     * @param args Any arguments required for the format varargs
     */
    public static synchronized void register(DoubleSupplier sample, String format, Object... args) {
        if (state == State.ERRORED) {
            // The log system is very broken, possibly because of a missing or faulty USB
            // flash drive.
            return;
        }
        if (state == State.CREATED) {
            columns.add(new Column(sample, String.format(format, args)));
            return;
        }
        // Invalid state. Create a stack trace to help with debugging.
        // It's likely that registrationComplete() has already been called.
        try {
            throw new Exception("Tried to add sample in an invalid state: " + state);
        } catch (Exception e) {
            Log.exception("Chart", "Failure to register: ", e);
        }
    }

    /**
     * Register a data stream for collection to the log.
     * The sample needs to have a method that returns an integer which is the data element.
     * The format (and args) are used to construct the name of the data stream.
     * 
     * @param sample An object that returns an integer value when called
     * @param format a VarArgs string which evaluates to the name of the data stream
     * @param args Any arguments required for the format varargs
     */
    public static void register(IntSupplier sample, String format, Object... args) {
        register(() -> (double) sample.getAsInt(), format, args);
    }

    /**
     * Register a data stream for collection to the log.
     * The sample needs to have a method that returns a DirectionalAxis which is the data element.
     * The format (and args) are used to construct the name of the data stream.
     * 
     * @param sample An object that returns a DirectionalAxis value when called
     * @param format a VarArgs string which evaluates to the name of the data stream
     * @param args Any arguments required for the format varargs
     */
    public static void register(DirectionalAxis sample, String format, Object... args) {
        register(() -> (double) sample.getDirection(), format, args);
    }

    /**
     * Register a data stream for collection to the log.
     * The sample needs to have a method that returns a Switch which is the data element.
     * The format (and args) are used to construct the name of the data stream.
     * 
     * @param sample An object that returns a Switch value when called
     * @param format a VarArgs string which evaluates to the name of the data stream
     * @param args Any arguments required for the format varargs
     */
    public static void register(Switch sample, String format, Object... args) {
        register(() -> (double) (sample.isTriggered() ? 1 : 0), format, args);
    }

    /**
     * Call when all registrations have been done and recording should start.
     */
    public static synchronized void registrationComplete(String matchDescription) {
        Chart.matchDescription = matchDescription;
        state = State.CONFIGURED;
        createdDateFiles = false;
        Log.debug("Chart", "Column additions completed");
    }

    /**
     * Restarts logging, called each time robot is enabled or initialised.
     */
    public synchronized static void restartCharts() {
        csvWriter.flush();
        init();
        // Jump straight to configured as all the columns have already been registered.
        state = State.CONFIGURED;
    }

    // Implementation

    private enum State {
        INVALID, // File has not yet been created
        CREATED, // File has been created, but we are waiting for all logging classes to be created
        CONFIGURED, // Logging classes are all created and have registered with the logging
                    // subsystem
        ACTIVE, // .html files have been populated and we are ready to write records into the .csv
                // file.
        ERRORED // a problem has occurred. Abandon trying to write.
    }

    /**
     * Holds a name and a way to get a value for it.
     * For example "battery voltage" and a callback to query it.
     */
    private static class Column {
        public String name;
        public DoubleSupplier sample;

        public Column(DoubleSupplier sample, String name, Object... args) {
            this.name = String.format(name, args);
            this.sample = sample;
        }
    }

    // Log file.
    private static LogWriter csvWriter = new NullLogWriter();
    private static LogWriter chartHTMLWriter = new NullLogWriter();
    private static LogWriter locationHTMLWriter = new NullLogWriter();
    // Internal state.
    private static State state = State.INVALID;
    private static String matchDescription = "Invalid";
    // Registered samples/columns.
    private static ArrayList<Column> columns = new ArrayList<Column>();
    private static Double timeOffset = 0.0; // Make the graphs always start at zero.
    private static boolean createdDateFiles = false;

    static {
        // Setup the files.
        init();
    }

    /**
     * Creates new log files on request.
     */
    private static synchronized void init() {
        // Set the graphLogState to INVALID as the new files have not yet been created.
        state = State.INVALID;
        createdDateFiles = false;

        // Get time since robot boot, so chart starts at time = 0.
        timeOffset = Strongback.timeSystem().currentTime();
        try {
            if (System.getProperty("user.name").equals("lvuser")) {
                // Running on the robot. Write for real.
                String baseDir = Paths.get(Config.logging.basePath, RobotName.get()).toString();
                long logNum = LogFileNumber.get();
                // Open all files. Also creates Latest symlink.
                csvWriter = new TimestampedLogWriter(baseDir, "data", logNum, "csv");
                chartHTMLWriter = new TimestampedLogWriter(baseDir, "chart", logNum, "html");
                locationHTMLWriter = new TimestampedLogWriter(baseDir, "location", logNum, "html");
            }

            // Everything was successfully created, we're good to go.
            state = State.CREATED;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.printf("Failed to create log files in %s: %s\n", Config.logging.basePath,
                    e.getMessage());
            state = State.ERRORED;
        }
    }

    /**
     * execute is called periodically.
     */
    @Override
    public void execute(long timeInMillis) {
        update();
    }

    /**
     * Called periodically to record the latest values and write them to file.
     */
    private static synchronized void update() {
        if (state == State.CONFIGURED) {
            String csvColumns = getGraphHeaders();
            initCSVFile(csvColumns);
            initChartFile(csvColumns);
            initLocationFile();
            state = State.ACTIVE;
        }
        if (state == State.ACTIVE) {
            csvWriter.write(getGraphValues());
            if (createdDateFiles)
                return;
            // If there is a valid date and time from the drivers station,
            // create the dated links to the files.
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.YEAR) >= 2017) {
                createDateFiles(now);
                createdDateFiles = true;
            }
        }
    }

    /**
     * Insert the header row into the CSV file now that all the columns are known.
     */
    private static void initCSVFile(String csvColumns) {
        csvWriter.write(csvColumns + "\n");
    }

    /**
     * Create the html file for viewing the data as a plot.ly chart.
     */
    private static void initChartFile(String csvColumns) {
        String title = "Run " + LogFileNumber.get();
        String file = String.format("data_%05d", LogFileNumber.get());
        chartHTMLWriter.write(String.format("<html>\n" + "<head><title>%1$s plot.ly chart</title>\n"
                + "</head>\n"
                + "<body>\n" + "<script>\n" + "var fn = '../data/%2$s.csv';\n"
                + "var baseLabelsStr = '%3$s';\n"
                + "</script>\n"
                + "<script src='../../scripts/plotly.js'></script><script src='../../scripts/plotly-ext.js'></script>\n"
                + "<div id='chart1' style='width:100%%;height:90%%;'><!-- Plotly chart will be drawn inside this DIV --></div>\n"
                + "<script> loadPlotlyFromCSV('%1$s', fn, 0);\n" + "</script>\n\n" + "<p>\n"
                + "Click on the series in the legend to swap y-axis and then to turn off.\n"
                + "<p>\n"
                + "Trying to run this locally? Run the following in the directory containing this file:\n"
                + "<p>\n"
                + "<pre>\n" + " python3 -m http.server\n" + "</pre>\n" + "<p>\n"
                + "Then go to <a href='http://localhost:8000/%4$s/latest/Latest_chart.html'>http://localhost:8000/%4$s/latest/Latest_chart.html</a>\n"
                + "</body>\n", title, file, csvColumns, RobotName.get()));
        chartHTMLWriter.close();
    }

    /**
     * Create the html file for the location plot.
     */
    private static void initLocationFile() {
        String title = "Instance " + LogFileNumber.get();
        String file = String.format("data_%05d", LogFileNumber.get());
        locationHTMLWriter.write(String.format("<html><title>%1$s</title><head>\n"
                + "<script src='../../scripts/plotly.js'></script><script src='../../scripts/plotLocation.js'></script>\n"
                + "<body><div id='myDiv' style='width: 480px; height: 400px;'>\n"
                + "<!-- Plotly chart will be drawn inside this DIV --></div>\n"
                + "<script> makeplot('../data/%2$s.csv');\n" + "</script>" + "<p>\n"
                + "Trying to run this locally? Run the following in the directory containing this file:\n"
                + "<p>\n"
                + "<pre>\n" + " python3 -m http.server\n" + "</pre>\n" + "<p>\n"
                + "Then go to <a href='http://localhost:8000/%3$s/latest/Latest_chart.html'>http://localhost:8000/%3$s/latest/Latest_chart.html</a>\n"
                + "</body>\n", title, file, RobotName.get()));
        locationHTMLWriter.close();
    }

    private static String getGraphHeaders() {
        String headers = "date";
        for (Column e : columns) {
            if (e.name != null) {
                headers = headers + "," + e.name;
            }
        }
        return headers;
    }

    public static String getGraphValues() {
        StringBuffer s = new StringBuffer();
        // Subtracts time offset from current time so graph starts at time = 0
        s.append(String.format("%.3f", Strongback.timeSystem().currentTime() - timeOffset));
        for (Column e : columns) {
            if (e.name != null) {
                s.append(",");
                s.append(e.sample.getAsDouble());
            }
        }
        s.append("\n");
        return s.toString();
    }

    /**
     * Create the date based symbolic links. These create symbolic links from date
     * stamped version of the file to the actual file. This is a separate method as
     * we delay creating these until we are reasonably sure the date is correct.
     * 
     * @param timestamp The date to use.
     */
    public static void createDateFiles(Calendar timestamp) {
        String timestampStr = new SimpleDateFormat("yyyyMMdd't'hhmmss").format(timestamp.getTime());
        try {
            // Create links based on the timestamp.
            csvWriter.createSymbolicLink(Config.logging.dateExtension, timestampStr);
            chartHTMLWriter.createSymbolicLink(Config.logging.dateExtension, timestampStr);
            locationHTMLWriter.createSymbolicLink(Config.logging.dateExtension, timestampStr);
            // And on event name, match type, match number, replay number, alliance and
            // position. These details should be available at the same time now that the
            // drivers station is able to talk to the robot.
            csvWriter.createSymbolicLink(Config.logging.eventExtension, matchDescription);
            chartHTMLWriter.createSymbolicLink(Config.logging.eventExtension, matchDescription);
            locationHTMLWriter.createSymbolicLink(Config.logging.eventExtension, matchDescription);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Error creating symlinks in %s: %s\n", Config.logging.basePath,
                    e.getMessage());
        }
    }
}
