package frc.robot.subsystems;



import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.interfaces.LEDStrip;
import frc.robot.lib.LEDColour;
import frc.robot.lib.MathUtil;

// LED Strip Subsystem 2020

public class LEDStripImpl implements LEDStrip {
    public AddressableLED ledStrip;
    public AddressableLEDBuffer ledStripBuffer;
    private final int numberOfLEDs;
    private int rainbowHue = 0;
    private LEDColour alliance = LEDColour.WHITE;

    public LEDStripImpl(int PWM_Port, int numberOfLEDs) {
        this.numberOfLEDs = numberOfLEDs;

        ledStrip = new AddressableLED(PWM_Port);
        ledStripBuffer = new AddressableLEDBuffer(numberOfLEDs);
        ledStrip.setLength(ledStripBuffer.getLength());

        // Set the data
        ledStrip.setData(ledStripBuffer);
        ledStrip.start();
    }

    @Override
    public void setColour(LEDColour c) {
        for (int i = 0; i < numberOfLEDs; i++) {
            ledStripBuffer.setRGB(i, c.r, c.g, c.b);
        }
        setData();
    }

    @Override
    public void setAlternatingColour(LEDColour c1, LEDColour c2) {
        int alternateNum = numberOfLEDs / 10; // alternate colours every 10% of the strip
        for (int i = 0; i < numberOfLEDs; i++) {
            if ((i / alternateNum) % 2 == 0) {
                setLEDColour(i, c1);
            } else {
                setLEDColour(i, c2);
            }
        }
        setData();
    }

    @Override
    /**
     * Fill the LED strip outside-in like a progress bar mirrored at the center.
     */
    public void setProgressColour(LEDColour c1, LEDColour c2, double percent) {
        percent = MathUtil.clamp(percent, 0, 1);
        int progress = (int) (percent * numberOfLEDs / 2);
        // Fill one side (completed colour, gets closer to center as percent increases)
        for (int i = 0; i < progress; i++) {
            setLEDColour(i, c1);
        }
        // Fill center (unfinished colour)
        for (int i = progress; i < numberOfLEDs - progress; i++) {
            setLEDColour(i, c2);
        }
        // Fill other side (completed colour, gets closer to center as percent increases)
        for (int i = numberOfLEDs - progress; i < numberOfLEDs; i++) {
            setLEDColour(i, c1);
        }
        setData();
    }

    /**
     * Applies a rainbow pattern across all LEDs which is offset on subsequent calls
     * Must be called periodically
     * Currently disabledPeriodic calls this which runs every 20ms
     */
    public void updateRainbow() {
        // For every pixel
        for (int i = 0; i < numberOfLEDs; i++) {
            // Calculate the hue - hue is easier for rainbows because the color shape is a circle so
            // only one value needs to precess
            final int hue = (rainbowHue + (i * 180 / numberOfLEDs)) % 180;
            // Set the value
            ledStripBuffer.setHSV(i, hue, 255, 128);

        }
        // Increase by to make the rainbow "move"
        rainbowHue += 3;
        // Check bounds
        rainbowHue %= 180;
        setData();
    }

    @Override
    public void setGreenAndGold() {
        for (int i = 0; i < numberOfLEDs; i++) {
            setLEDColour(i, i % 2 == 0 ? LEDColour.YELLOW : LEDColour.GREEN);
        }
        setData();
    }

    private void setData() {
        ledStrip.setData(ledStripBuffer);
    }

    private void setLEDColour(int index, LEDColour c) {
        ledStripBuffer.setRGB(index, c.r, c.g, c.b);
    }

    @Override
    public void setAlliance() {
        setColour(alliance);
    }

    @Override
    public void setAlliance(LEDColour alliance) {
        this.alliance = alliance;
        setAlliance();
    }
}
