package org.strongback.components;



import com.ctre.phoenix.ErrorCode;

public interface TalonSensorCollection {

    /**
     * Get the position of whatever is in the analog pin of the Talon,
     * regardless of whether it is actually being used for feedback.
     * 
     * @return
     */
    int getAnalogIn();

    /**
     * Get the position of whatever is in the analog pin of the Talon,
     * regardless of whether it is actually being used for feedback.
     * 
     * @return
     */
    int getAnalogInRaw();

    /**
     * Get the velocity of whatever is in the analog pin of the Talon,
     * regardless of whether it is actually being used for feedback.
     * 
     * @return
     */
    int getAnalogInVel();

    /**
     * Gets pin state quad a.
     * 
     * @return
     */
    boolean getPinStateQuadA();

    /**
     * Gets pin state quad b.
     * 
     * @return
     */
    boolean getPinStateQuadB();

    /**
     * Gets pin state quad index.
     * 
     * @return
     */
    boolean getPinStateQuadIdx();

    /**
     * Gets pulse width position, regardless of whether it is actually being
     * used for feedback.
     * 
     * @return
     */
    int getPulseWidthPosition();

    /**
     * Gets pulse width rise to fall time.
     * 
     * @return
     */
    int getPulseWidthRiseToFallUs();

    /**
     * Gets pulse width rise to rise time.
     * 
     * @return
     */
    int getPulseWidthRiseToRiseUs();

    /**
     * Gets pulse width velocity, regardless of whether it is actually being
     * used for feedback.
     * 
     * @return
     */
    int getPulseWidthVelocity();

    /**
     * Get the quadrature position of the Talon, regardless of whether it is
     * actually being used for feedback.
     * 
     * @return
     */
    double getQuadraturePosition();

    /**
     * Get the quadrature velocity, regardless of whether it is actually being
     * used for feedback.
     * 
     * @return
     */
    int getQuadratureVelocity();

    /**
     * Is forward limit switch closed.
     * 
     * @return
     */
    boolean isFwdLimitSwitchClosed();

    /**
     * Is reverse limit switch closed.
     * 
     * @return
     */
    boolean isRevLimitSwitchClosed();

    /**
     * Sets analog position.
     * 
     * @param newPosition
     * @param timeoutMs
     * @return
     */
    ErrorCode setAnalogPosition(int newPosition, int timeoutMs);

    /**
     * Sets pulse width position.
     * 
     * @param newPosition
     * @param timeoutMs
     * @return
     */
    ErrorCode setPulseWidthPosition(int newPosition, int timeoutMs);

    /**
     * Change the quadrature reported position.
     * 
     * @param newPosition
     * @param timeoutMs
     * @return
     */
    ErrorCode setQuadraturePosition(double newPosition, int timeoutMs);
}
