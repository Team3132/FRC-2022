package frc.robot.lib;



import java.util.function.DoubleSupplier;

/**
 * A simple PIDF loop.
 * Rather than call backs as per the WPIlib PID this is a push PID loop useful for
 * dropping into subsystems.
 * 
 * It has four parameters, P, I, D and F.
 * 
 * We call getOutput to return the current output value for the PID loop.
 */
public class SimplePID {
    private DoubleSupplier timeSource; // This feeds us the time
    private double setpoint; // current setpoint
    private double P, I, D, F; // Tunable constants
    private double lastTime; // last time the PID was run (for Dt calculations)
    private double lastError; // previous error.
    private double currentIntegral;
    private double lastOutput;

    public SimplePID(double P, double I, double D, double F, DoubleSupplier timeSource) {
        this.P = P;
        this.I = I;
        this.D = D;
        this.F = F;
        this.timeSource = timeSource;
    }


    /**
     * Reset the PID loop values. Useful when we have finished with an iteration of this loop.
     * 
     * @return This, for chaining
     */
    public SimplePID reset() {
        this.setpoint = 0;
        this.lastError = 0;
        this.currentIntegral = 0;
        this.lastOutput = 0;
        this.lastTime = timeSource.getAsDouble();
        return this;
    }

    /**
     * Set the loops setpoint
     * 
     * @param setpoint The point to which we are aiming. The valueSource should move towards this
     *        value if we do our work right
     * @return This, for chaining
     */
    public SimplePID setSetpoint(double setpoint) {
        this.setpoint = setpoint;
        return this;
    }

    /**
     * Calculate the PID loop's output value
     * 
     * @param currentPoint current value of the value under measure
     * @return The new output value for the PID loop
     */
    public double getOutput(double currentPoint) {
        double output, error, curTime, curD, deltaT;

        curTime = timeSource.getAsDouble();
        deltaT = curTime - lastTime;
        if (deltaT == 0) {
            // no time has passed, so same value!
            return lastOutput;
        }
        error = setpoint - currentPoint;
        currentIntegral += error * (curTime - lastTime);
        curD = (error - lastError) / (curTime - lastTime);
        output = this.F + this.P * error + this.I * currentIntegral + this.D * curD;
        lastError = error;
        lastTime = curTime;
        lastOutput = output;
        return output;
    }
}
