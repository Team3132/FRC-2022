package frc.robot.interfaces;



import java.io.IOException;

/**
 * Interface for the JeVois machine vision camera.
 */
public interface Jevois extends LogHelper {

    public enum CameraMode {
        WEBCAM, // For the driver to see the output of the camera with no vision processing.
        VISION // Vision processing enabled. May not be suitable for driving with.
    }

    /**
     * Change the mode of the camera to support either vision or act just as a
     * webcam.
     * 
     * @param mode Which mode to use.
     */
    public void setCameraMode(CameraMode mode) throws IOException;

    /**
     * Read a full line. Will block until text is available.
     * 
     * Example from a visible vision target:
     * 
     * D3 0.0041708097717292 -0.08790574004652713 0.6225565372368229 0.28 0.175 1.0
     * 0.9757402399136117 -0.1237847793771889 0.17325008881164186 0.050918752288585487 FIRST
     * 
     * D3 <x pos, left is -ve> <y pos, up is -ve> <dist (meters)> <target width> <target height>
     * <1.0>
     */
    public String readLine() throws IOException;

    /**
     * Send a command and keep reading until a line with "ERR" or "OK".
     * 
     * @param command the full command text to send, eg "info".
     * @return The output text including the OK or ERR terminator.
     * @throws IOException
     */
    public String issueCommand(String command) throws IOException;
}
