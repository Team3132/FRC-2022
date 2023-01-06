package frc.robot.mock;



import frc.robot.interfaces.LEDStrip;
import frc.robot.lib.LEDColour;

// LED Strip Subsystem 2020

public class MockLEDStrip implements LEDStrip {

    @Override
    public void setColour(LEDColour c) {}

    @Override
    public void setAlternatingColour(LEDColour c1, LEDColour c2) {}

    @Override
    public void setProgressColour(LEDColour c1, LEDColour c2, double percent) {}

    @Override
    public void updateRainbow() {}

    @Override
    public void setGreenAndGold() {}

    @Override
    public void setAlliance() {}

    @Override
    public void setAlliance(LEDColour ledColour) {}
}
