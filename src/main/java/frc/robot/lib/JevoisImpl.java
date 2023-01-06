package frc.robot.lib;

/**
 * Class to talk to the JeVois smart machine video camera.
 * More information can be found at jevois.org
 * 
 * This allows control over the camera via the serial port and to read
 * the position of detected vision targets, also over the serial port.
 * 
 * This class has been setup to allow different modes - where depending
 * on what the camera should do - either detect vision targets or allow
 * the driver to drive. These are the two Modes.
 * 
 * Notes:
 * 1) If operating in headed mode, the video codec and mode selects which
 * video processor to use. Something like a CameraServer needs to be
 * configured to select the the video code and mode so that the correct
 * video processor is used.
 * 2) It won't give any results back in headed mode unless something is pulling video frames (ugh).
 * 3) If something stops reading the serial port (like this script), then the video frames stop
 * (ugh).
 */



import com.fazecast.jSerialComm.*;
import frc.robot.interfaces.Jevois;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JevoisImpl implements Jevois {

    private static class Parameter<T> {
        String name;
        T value;

        public Parameter(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            if (value instanceof Boolean) {
                return "setcam " + name + " " + ((Boolean) value ? 1 : 0) + "\n";
            } else {
                return "setcam " + name + " " + value + "\n";
            }
        }
    }

    private static int limit(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class Mode {
        public String name;
        public String modeString = "";
        Parameter<Integer> brightness = new Parameter<Integer>("brightness", 0);
        Parameter<Integer> contrast = new Parameter<Integer>("contrast", 3);
        Parameter<Integer> saturation = new Parameter<Integer>("saturation", 2);
        Parameter<Boolean> autowb = new Parameter<Boolean>("autowb", true);
        Parameter<Integer> redbal = new Parameter<Integer>("redbal", 128);
        Parameter<Integer> bluebal = new Parameter<Integer>("bluebal", 128);
        Parameter<Boolean> autogain = new Parameter<Boolean>("autogain", true);
        Parameter<Integer> gain = new Parameter<Integer>("gain", 128);
        Parameter<Boolean> hflip = new Parameter<Boolean>("hflip", false);
        Parameter<Boolean> vflip = new Parameter<Boolean>("vflip", false);
        Parameter<Integer> sharpness = new Parameter<Integer>("sharpness", 6);
        Parameter<Integer> absexp = new Parameter<Integer>("absexp", 1000);

        public Mode(String name) {
            this.name = name;
        }

        /**
         * A setmapping or setmapping2 command to tell the camera what module to load.
         * Can't be used when something is streaming the video camera.
         * 
         * Example syntax: setmapping <num> - select video mapping <num>, only possible
         * while not streaming setmapping2 <CAMmode> <CAMwidth> <CAMheight> <CAMfps>
         * <Vendor> <Module> - set no-USB-out video mapping defined on the fly, while
         * not streaming.
         * 
         * @param str Full command to run to set the mapping.
         */
        public void setModeString(String str) {
            modeString = str;
        }

        public void setBrightness(int brightness) {
            this.brightness.value = limit(brightness, -3, 3);
        }

        public void setContrast(int contrast) {
            this.contrast.value = limit(contrast, 0, 6);
        }

        public void setSaturation(int saturation) {
            this.saturation.value = limit(saturation, 0, 4);
        }

        public void setAutowb(boolean autowb) {
            this.autowb.value = autowb;
        }

        @SuppressWarnings("unused")
        public void setRedbal(int redbal) {
            this.redbal.value = limit(redbal, 0, 255);
        }

        @SuppressWarnings("unused")
        public void setBluebal(int bluebal) {
            this.bluebal.value = limit(bluebal, 0, 255);
        }

        public void setAutogain(boolean autogain) {
            this.autogain.value = autogain;
        }

        public void setGain(int gain) {
            this.gain.value = limit(gain, 16, 1023);
        }

        @SuppressWarnings("unused")
        public void setHflip(boolean hflip) {
            this.hflip.value = hflip;
        }

        @SuppressWarnings("unused")
        public void setVflip(boolean vflip) {
            this.vflip.value = vflip;
        }

        public void setSharpness(int sharpness) {
            this.sharpness.value = limit(sharpness, 0, 32);
        }

        @SuppressWarnings("unused")
        public void setAbsexp(int absexp) {
            this.absexp.value = limit(absexp, 1, 1000);
        }

        @Override
        public String toString() {
            String toReturn = "";
            toReturn += brightness;
            toReturn += contrast;
            toReturn += saturation;
            toReturn += autowb;
            toReturn += redbal;
            toReturn += bluebal;
            toReturn += hflip;
            toReturn += vflip;
            toReturn += sharpness;
            toReturn += absexp;
            return toReturn;
        }
    }

    private static final Mode webcamMode;
    private static final Mode visionMode;

    static {
        /*
         * HSV ranges that seemed to work (Mark: October 2019)
         * Min 70, 10, 100
         * Max 90, 255, 255
         * YUYV 640 252 60.0 YUYV 320 240 60.0 JeVois FirstPython
         * guvcview -ao none -f YUYV -x 640x252
         * Needs to be manually enabled.
         * BAYER 640 480 30.0 BAYER 640 480 30.0 JeVois PassThrough
         */
        webcamMode = new Mode("webcam");
        webcamMode.setAutowb(false);
        webcamMode.setAutogain(false);
        // Causes an error.
        // webcamMode.setModeString("setmapping2 MJPG 320 240 30.0 RGB565 320 240 30.0
        // JeVois Convert");
        visionMode = new Mode("vision");
        visionMode.setBrightness(-3);
        visionMode.setContrast(3);
        visionMode.setSaturation(2);
        visionMode.setAutowb(false);
        visionMode.setGain(16);
        visionMode.setSharpness(6);
        webcamMode.setAutogain(false);
        // The first mapping line in the file should be the FirstPython one.
        visionMode.setModeString("setmapping 0");
        // 0 - OUT: YUYV 640x252 @ 60fps CAM: YUYV 320x240 @ 60fps MOD: JeVois:FirstPython Python
        // visionMode.setModeString("setmapping2 YUYV 320 240 60.0 JeVois FirstPython");
    }

    InputStream istream;
    OutputStream ostream;
    private boolean connected = false;

    /**
     * Try to connect to a JeVois camera and set it up for vision processing. If
     * there isn't a camera connected, don't fail, just complain and give up. Throws
     * if there are any issues setting up the camera.
     * 
     * @throws IOException
     */
    public JevoisImpl() throws IOException {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (int i = 0; i < ports.length; i++) {
            SerialPort port = ports[i];
            info("Found port %s\n", port.getDescriptivePortName());
            if (port.getDescriptivePortName().startsWith("JeVois")) {
                port.openPort();
                info("Found camera %s on %s\n", port.getDescriptivePortName(),
                        port.getSystemPortName());
                // Don't block too long in case a command needs to be sent.
                port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10, 0);
                istream = port.getInputStream();
                ostream = port.getOutputStream();
                connected = true;
                info(issueCommand("listmappings"));
                setCameraMode(CameraMode.VISION); // Restore any mode if one has been set.
                info(issueCommand("info"));
                // Turn on the serial output over USB
                info(issueCommand("setpar serout USB"));
                return;
            }
        }
        error("Failed to find JeVois camera, is it plugged in to a USB port and has an orange light?");
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setCameraMode(CameraMode mode) throws IOException {
        if (!isConnected())
            return; // Camera isn't connected.
        switch (mode) {
            case WEBCAM:
                setParameters(webcamMode);
                break;
            case VISION:
                setParameters(visionMode);
                break;
            default:
                error("Jevois: Unsupported vision mode %s\n", mode.toString());
        }
    }

    private void setParameters(Mode mode) throws IOException {
        if (!isConnected())
            return; // No connection, give up.
        info("Setting %s camera mode.\n", mode.name);
        if (!mode.modeString.isEmpty()) {
            info(issueCommand(mode.modeString));
        }
        for (String command : mode.toString().split("\n")) {
            info(issueCommand(command));
        }
    }

    /**
     * Read a full line. Will block until text is available.
     * Example from a visible vision target:
     * D3 0.0041708097717292 -0.08790574004652713 0.6225565372368229 0.28 0.175 1.0
     * 0.9757402399136117 -0.1237847793771889 0.17325008881164186 0.050918752288585487 FIRST
     * D3 0.2703311590479972 0.34137431252665157 1.340374448197372 0.28 0.175 1.0 0.9416448768770811
     * -0.07495457893269117 0.32795451275561716 -0.011514100546526834 FIRST
     * D3 -0.34479472541636735 0.34221938559564063 1.4353810619853402 0.28 0.175 1.0
     * 0.9835028223168648 -0.1689770348306745 0.06316048382443286 0.013405725555073253 FIRST
     * D3 -0.3190665833627917 -0.16974943059054468 1.4929203902754815 0.28 0.175 1.0
     * 0.994180288811222 0.0003884439751087179 0.09825181108672097 0.04418126377428119 FIRST
     * 
     * D3 <x pos, left is -ve> <y pos, up is -ve> <dist (meters)> <target width> <target height>
     * <1.0>
     * jevois.sendSerial("D3 {} {} {} {} {} {} {} {} {} {} FIRST".
     * format(np.asscalar(tv[0]), np.asscalar(tv[1]), np.asscalar(tv[2]), # position
     * self.owm, self.ohm, 1.0, # size
     * r, np.asscalar(i[0]), np.asscalar(i[1]), np.asscalar(i[2]))) # pose
     */
    @Override
    public String readLine() throws IOException {
        if (!isConnected()) {
            debug("CAMERA NOT CONNECTED");
            throw new IOException("No camera connected"); // No connection, give up.

        }
        StringBuffer line = new StringBuffer(200);
        while (true) {
            synchronized (this) {
                // Logger.debug(line.toString());
                try {
                    int b = istream.read();
                    if (b < 0) {
                        connected = false;
                        throw new IOException(
                                "End of file reached on serial port - was the camera disconnected / incorrect permissions?");
                    }
                    // Handle both \r\n and \n line endings.
                    if (b == '\r') {
                        continue;
                    }

                    if (b == '\n') {
                        // Have a newline, return the current line.
                        String result = line.toString();
                        line.setLength(0);
                        return result;
                    }
                    line.append((char) b);
                } catch (SerialPortTimeoutException e) {
                    debug("Jevois: There is serial port timeout!!!!!!!!!");
                    // Give up reading in case a command needs to be sent.
                }
            }
        }
    }

    /**
     * Send a command and keep reading until a line with "ERR" or "OK".
     * 
     * @param command the full command text to send, eg "info".
     * @return The output text including the OK or ERR terminator.
     * @throws IOException
     */
    @Override
    public synchronized String issueCommand(String command) throws IOException {
        if (!isConnected())
            return "ERR: JeVois not connected";

        info(command);
        ostream.write(command.getBytes());
        String newline = "\n";
        ostream.write(newline.getBytes());
        StringBuffer response = new StringBuffer();
        while (true) {
            String l = readLine();
            response.append(l);
            if (l.equals("OK")) {
                // Command was successful, return the text.
                return response.toString();
            }
            if (l.startsWith("ERR")) {
                // Some problem with the command, give up.
                throw new IOException("Problem issuing command '" + command + "': " + l);
            }
            response.append("\n");
        }

    }

    @Override
    public String getName() {
        return "Jevois";
    }
}
