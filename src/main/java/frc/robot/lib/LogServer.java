package frc.robot.lib;



import frc.robot.interfaces.LogHelper;
import frc.robot.lib.log.Log;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A websocket server to publish new latest log messages to all websocket
 * clients. This is used to provide a live feed of the latest log messages for live debugging
 * purposes. Otherwise, the log messages are only available in the log file.
 */
public class LogServer extends WebSocketServer implements LogHelper {
    /**
     * Create a map that stores the Java location of the client connection as a
     * string stores and also the Tailer thread so that it can be referenced if it needs to be
     * closed.
     */
    Map<String, Tailer> tailers = new HashMap<String, Tailer>();

    /**
     * Class is used to read and broadcast new lines in log files for every new
     * client that connects.
     */
    class ConnectionListener extends TailerListenerAdapter {
        WebSocket conn;

        public ConnectionListener(WebSocket conn) {
            this.conn = conn;
        }

        /**
         * Gets called on every new line detected by the Tailer instance
         */
        public void handle(String line) {
            conn.send(line);
        }
    }

    public LogServer(InetSocketAddress address) {
        super(address);
        info("Starting websocket server");
        start();
        info("Started websocket server");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        File logFile = Log.writer.getLinkPath("latest", "Latest").toFile();
        if (logFile.exists()) {
            TailerListener connListener = new ConnectionListener(conn);
            Tailer tailer = Tailer.create(logFile, connListener);
            tailers.put(conn.toString(), tailer);
        } else {
            conn.send(
                    "Log file does not yet exist. Please wait for the log file to be created and then reconnect.");
            info("Log file does not yet exist");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (!tailers.containsKey(conn.toString())) {
            error("Tried to remove a non-existent socket");
            return;
        }
        tailers.get(conn.toString()).stop();
        tailers.remove(conn.toString());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {}

    /**
     * When the server closes unexpectedly onError is called in addition to onClose.
     */
    @Override
    public void onError(WebSocket conn, Exception e) {
        exception("Error on Websocket", e);
    }

    @Override
    public void onStart() {}

    @Override
    public String getName() {
        return "LogServer";
    }

}
