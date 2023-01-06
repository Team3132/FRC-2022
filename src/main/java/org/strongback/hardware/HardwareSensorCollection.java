package org.strongback.hardware;



import com.ctre.phoenix.ErrorCode;
import org.strongback.components.TalonSensorCollection;

public class HardwareSensorCollection implements TalonSensorCollection {
    private com.ctre.phoenix.motorcontrol.can.TalonSRX talon;
    private double scale = 1;

    HardwareSensorCollection(com.ctre.phoenix.motorcontrol.can.TalonSRX talon) {
        this.talon = talon;
    }

    @Override
    public int getAnalogIn() {
        return talon.getSensorCollection().getAnalogIn();
    }

    @Override
    public int getAnalogInRaw() {
        return talon.getSensorCollection().getAnalogInRaw();
    }

    @Override
    public int getAnalogInVel() {
        return talon.getSensorCollection().getAnalogInVel();
    }

    @Override
    public boolean getPinStateQuadA() {
        return talon.getSensorCollection().getPinStateQuadA();
    }

    @Override
    public boolean getPinStateQuadB() {
        return talon.getSensorCollection().getPinStateQuadB();
    }

    @Override
    public boolean getPinStateQuadIdx() {
        return talon.getSensorCollection().getPinStateQuadIdx();
    }

    @Override
    public int getPulseWidthPosition() {
        return talon.getSensorCollection().getPulseWidthPosition();
    }

    @Override
    public int getPulseWidthRiseToFallUs() {
        return talon.getSensorCollection().getPulseWidthRiseToFallUs();
    }

    @Override
    public int getPulseWidthRiseToRiseUs() {
        return talon.getSensorCollection().getPulseWidthRiseToRiseUs();
    }

    @Override
    public int getPulseWidthVelocity() {
        return talon.getSensorCollection().getPulseWidthVelocity();
    }

    @Override
    public double getQuadraturePosition() {
        return talon.getSensorCollection().getQuadraturePosition() / scale;
    }

    @Override
    public int getQuadratureVelocity() {
        return talon.getSensorCollection().getQuadratureVelocity();
    }

    @Override
    public boolean isFwdLimitSwitchClosed() {
        return talon.getSensorCollection().isFwdLimitSwitchClosed();
    }

    @Override
    public boolean isRevLimitSwitchClosed() {
        return talon.getSensorCollection().isRevLimitSwitchClosed();
    }

    @Override
    public ErrorCode setAnalogPosition(int newPosition, int timeoutMs) {
        return talon.getSensorCollection().setAnalogPosition(newPosition, timeoutMs);
    }

    @Override
    public ErrorCode setPulseWidthPosition(int newPosition, int timeoutMs) {
        return talon.getSensorCollection().setPulseWidthPosition(newPosition, timeoutMs);
    }

    @Override
    public ErrorCode setQuadraturePosition(double newPosition, int timeoutMs) {
        return talon.getSensorCollection().setQuadraturePosition((int) (newPosition * scale),
                timeoutMs);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
