package frc.robot.interfaces;



import frc.robot.lib.LEDColour;

public interface LEDStrip {
    public void setColour(LEDColour c);

    /**
     * Fills the LED strip with alternating colours
     */
    public void setAlternatingColour(LEDColour c1, LEDColour c2);

    /**
     * Fills the led strip like a progress bar.
     * 
     * @param c1 Colour for completed area
     * @param c2 Colour for unfinished area
     * @param percent Percent of leds to fill from 0-1
     */
    public void setProgressColour(LEDColour c1, LEDColour c2, double percent);

    public void setGreenAndGold();

    public void updateRainbow();

    public void setAlliance();

    public void setAlliance(LEDColour ledColour);
}
