package frc.robot.interfaces;

public interface Compressor {
    /**
     * Allows the compressor to run
     */
    public void turnOn();

    /**
     * Disallows the compressor to run
     */
    public void turnOff();

    /**
     * @return true if the compressor is allowed to run
     */
    public boolean isOn();
}
