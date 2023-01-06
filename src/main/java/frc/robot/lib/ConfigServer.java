package frc.robot.lib;



import frc.robot.interfaces.LogHelper;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import org.apache.commons.text.StringEscapeUtils;

/**
 * This class is used to host a HTTP webserver on port 5801 allowing quick access to the config
 * file.
 * The user is given a textarea to edit the current config file on the robot and there is also a
 * checkbox
 * that allows them to select whether or not to restart the robot code after saving their changes to
 * the config.
 */
public class ConfigServer extends Thread implements LogHelper {
    private String webRoot;
    private String configFilename;
    private String exampleConfigFilename;
    private String robotFilename;
    // Port to listen connection
    private int port;

    public ConfigServer(String webRoot, String configFilename, String robotFilename, int port) {
        this.webRoot = webRoot;
        this.configFilename = configFilename;
        this.exampleConfigFilename = configFilename + ".example";
        this.robotFilename = robotFilename;
        this.port = port;
        setName("ConfigServer");
        start();
    }

    @Override
    public void run() {
        ServerSocket serverConnect = null;
        try {
            serverConnect = new ServerSocket(port);
            while (true) {
                handleConnect(serverConnect.accept());
            }
        } catch (Exception e) {
            exception("Config server error", e);
        }
        if (serverConnect != null) {
            try {
                serverConnect.close();
            } catch (IOException e) {
            }
        }
    }

    private void handleConnect(Socket connect) {
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;

        try {
            // Read characters from the client via input stream on the socket.
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // Get character output stream to client. (for headers)
            out = new PrintWriter(connect.getOutputStream());
            // Get binary output stream to client. (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());
            // Get first line of the request from the client.
            String input = in.readLine();
            if (input == null)
                return; // Connection is dropped.
            info("Input: %s", input);
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // Get the HTTP method of the client.
            switch (method) {
                case "POST":
                    boolean restart = handlePost(in);
                    if (restart) {
                        handleGet(out, dataOut, "reload.html");
                        System.exit(0);
                    }
                    // Fall through to GET
                case "GET":
                case "HEAD":
                    // GET or HEAD method
                    if (!method.equals("HEAD")) { // GET method for returning content
                        handleGet(out, dataOut, "index.html");
                    }
                    break;
                default:
                    error("Invalid method: %s", method);
                    out.println("HTTP/1.1 501 Not Implemented");
                    out.println(); // Blank line between headers and content, very important !
                    out.flush();
            }

        } catch (IOException ioe) {
            error("Server error : %s", ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                exception("Error closing stream", e);
            }
        }
    }

    /**
     * Method to handle responses from a web-browser.
     * Stores input from the form and attempts to update the robot config and name.
     * There is an option on the form for whether or not to restart the robot code after saving the
     * config,
     * this is returned as a boolean.
     * 
     * @param in Characters read from the client via input stream on the socket.
     * @return Boolean for whether or not a restart was requested by the web-browser.
     * @throws IOException
     */
    private boolean handlePost(BufferedReader in) throws IOException {
        /*
         * This is the expected response from a web-browser, the important part is the last line
         * with the user input.
         * POST / HTTP/1.1
         * Host: localhost:8080
         * User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101
         * Firefox/52.0
         * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,* /*;q=0.8
         * Accept-Language: en-US,en;q=0.5
         * Accept-Encoding: gzip, deflate
         * Referer: http://localhost:8080/
         * Connection: keep-alive
         * Upgrade-Insecure-Requests: 1
         * Content-Type: application/x-www-form-urlencoded
         * Content-Length: 10
         * 
         * input=test&input2=hello+world
         */
        int length = 0;
        boolean restart = false;
        String input;
        do {
            input = in.readLine();
            if (input.startsWith("Content-Length")) {
                // Looking for a line like this:
                // Content-Length: 10
                String[] parts = input.split(" ");
                if (parts.length > 1) {
                    length = Integer.parseInt(parts[1]);
                }
            }
            info("Input: %s", input);
        } while (!input.equals(""));
        char[] params = new char[length];
        if (in.read(params, 0, length) != length) {
            error("Failed to read parameters from POST.");
            return false;
        }
        String paramsString = new String(params);
        info("Filtered Params: %s", paramsString);
        String[] inputs = paramsString.split("&");
        for (int i = 0; i < inputs.length; i++) {
            String[] param = inputs[i].split("=");
            String value = "";
            if (param.length > 1) {
                value = URLDecoder.decode(param[1], "UTF-8");
            }
            info("Seen param: %s", param[0]);
            // Add a new case to this if statement to check for more parameters.
            switch (param[0]) {
                case "config":
                    saveToFile(value, configFilename);
                    break;
                case "name":
                    saveToFile(value, robotFilename);
                    break;
                case "restart":
                    restart = true;
                    break;
                default:
                    error("Unexpected param: %s", param[0]);
            }
        }
        return restart;
    }

    /**
     * Method that takes a string, removes windows line endings and to save to a file on the robot.
     * 
     * @param contents Value to save to the file.
     */
    private void saveToFile(String contents, String filename) {
        info("Saving to file: %s", contents);
        File file = new File(filename);
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            // Remove windows line endings and write to file.
            writer.write(contents.replace("\r", ""));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGet(PrintWriter out, BufferedOutputStream dataOut, String filename)
            throws IOException {
        String html = readFile(Paths.get(webRoot, filename).toString());
        html = replaceParameter(html, "CONFIG", configFilename);
        html = replaceParameter(html, "EXAMPLE_CONFIG", exampleConfigFilename);
        html = replaceParameter(html, "ROBOT_NAME", robotFilename);
        byte[] fileData = html.getBytes();
        // Send HTTP Headers
        out.println("HTTP/1.1 200 OK");
        out.println("Content-type: text/html");
        out.println("Content-length: " + fileData.length);
        out.println(); // Blank line between headers and content, very important !
        out.flush(); // Flush character output stream buffer
        dataOut.write(fileData, 0, fileData.length);
        dataOut.flush();
    }

    private String replaceParameter(String htmlString, String param, String filename) {
        String fileString =
                StringEscapeUtils.escapeHtml4(readFile(filename));
        return htmlString.replace("${" + param + "}", fileString);
    }

    /**
     * Method to read a file and return the content as a String, removing all Windows line endings.
     * 
     * @param file Path of file to read.
     * @return Content of file as String.
     */
    private String readFile(String file) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            content = String.format("failed to read file(%s): %s", file, e);
            exception("failed to read file", e);
            e.printStackTrace();
        }
        return content.replace("\r", "");
    }
}
