package org.strongback.hardware;



import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;
import com.revrobotics.REVLibError;
import com.revrobotics.SparkMaxLimitSwitch;
import com.revrobotics.SparkMaxPIDController;
import org.strongback.components.Motor;
import org.strongback.components.PIDF;

/*
 * Package to wrap a Spark MAX controller driving a Neo.
 * 
 * This is the hardware interface class that implements the interface we use.
 * 
 * This currently only supports the built in hall effect encoder.
 * 
 * We have a scale factor. This is useful in position and velocity/speed closed loop feedback modes.
 * For reading it we divide by the scale factor, when writing values we multiply by the scale
 * factor.
 */
public class HardwareSparkMAX implements Motor {
    private final com.revrobotics.CANSparkMax spark;
    private final com.revrobotics.RelativeEncoder encoder;
    private int slotID = 0;
    private double setpoint = 0;
    // There appears to be a bug where if the pid controller is created before
    // any followers, then the followers won't follow. Hence the pid controller
    // is created on demand in getPID() in case there have been followers added.
    // Note that set(...) works no matter what, it's just pid.setReference(...,...)
    // that doesn't.
    private SparkMaxPIDController pid;
    private final SparkMaxLimitSwitch fwdLimitSwitch;
    private final SparkMaxLimitSwitch revLimitSwitch;

    public HardwareSparkMAX(com.revrobotics.CANSparkMax spark) {
        this.spark = spark;
        // Assume built in hall effect sensor.
        encoder = spark.getEncoder();
        setScale(1, 1); // Change from rpm to rps.
        fwdLimitSwitch =
                spark.getForwardLimitSwitch(SparkMaxLimitSwitch.Type.kNormallyOpen);
        revLimitSwitch =
                spark.getReverseLimitSwitch(SparkMaxLimitSwitch.Type.kNormallyOpen);
    }

    @Override
    public void set(ControlMode mode, double value) {
        // spark.set(value);
        getPID().setReference(value, mode.revControlType, slotID);
        setpoint = value;
    }

    private SparkMaxPIDController getPID() {
        if (pid == null) {
            // Work around a bug where followers don't work if they
            // are added after the pid controller is created.
            pid = spark.getPIDController();
        }
        return pid;
    }

    @Override
    public double get() {
        return setpoint;
    }

    @Override
    public Motor setInverted(boolean isInverted) {
        // Not available in brushless mode.
        // spark.setInverted(isInverted);
        return this;
    }

    @Override
    public boolean getInverted() {
        return spark.getInverted();
    }

    @Override
    public Motor disable() {
        spark.disable();
        return this;
    }

    @Override
    public void stop() {
        spark.stopMotor();
    }

    @Override
    public Motor setPIDF(int slotIdx, PIDF pidf) {
        getPID().setP(pidf.p, slotIdx);
        getPID().setI(pidf.i, slotIdx);
        getPID().setD(pidf.d, slotIdx);
        getPID().setFF(pidf.f, slotIdx);
        return this;
    }

    public Motor selectProfileSlot(int slotIdx) {
        // Only used when set() is called.
        slotID = slotIdx;
        return this;
    }

    @Override
    public double getPosition() {
        return encoder.getPosition();
    }

    @Override
    public Motor setPosition(double position) {
        encoder.setPosition(position);
        return this;
    }

    /**
     * Returns the speed as measured by the encoders and scaled by setScale()
     * 
     * @return Depending on what setScale() was called with, this will return either
     *         RPS or meters/second.
     */
    @Override
    public double getSpeed() {
        // CANEncoder returns RPM by default. This has been scaled to be RPS by dividing by 60.
        return encoder.getVelocity();
    }

    /**
     * Scale the values to/from the motors into more intuitive values.
     * 
     * getPosition() returns the number of metres.
     * getSpeed() returns metres/second.
     * 
     * Also consider setScale(double ticksPerTurn, double gearRatio).
     * 
     * @param ticksPerTurn Not used for the HardwareSparkMAX with a builtin encoder.
     * @param gearRatio How many turns of the motor to turn the output shaft, eg 11
     * @param wheelDiameterMetres How many metres does the wheel move for every turn.
     * @return this.
     */
    @Override
    public Motor setScale(double ticksPerTurn, double gearRatio, double wheelDiameter) {
        if (ticksPerTurn * gearRatio * wheelDiameter == 0) {
            throw new RuntimeException(
                    "WARNING: HardwareSparkMAX::setScale() was passed zero, this isn't what you want!");
        }
        // Encoder.getVelocity() returns RPM by default, so this needs to be converted to RPS.
        encoder.setVelocityConversionFactor(wheelDiameter / gearRatio / 60);
        // Encoder getPosition() returns rotations by default.
        encoder.setPositionConversionFactor(wheelDiameter / gearRatio);
        return this;
    }

    @Override
    public boolean isAtForwardLimit() {
        return fwdLimitSwitch.isPressed();
    }

    @Override
    public boolean isAtReverseLimit() {
        return revLimitSwitch.isPressed();
    }

    public boolean setSmartCurrentLimit(int limit) {
        return spark.setSmartCurrentLimit(limit) == REVLibError.kOk;
    }

    public boolean setSmartCurrentLimit(int stallLimit, int freeLimit) {
        return spark.setSmartCurrentLimit(stallLimit, freeLimit) == REVLibError.kOk;
    }

    public boolean setSmartCurrentLimit(int stallLimit, int freeLimit, int limitRPM) {
        return spark.setSmartCurrentLimit(stallLimit, freeLimit, limitRPM) == REVLibError.kOk;
    }

    public boolean setSecondaryCurrentLimit(double limit) {
        return spark.setSecondaryCurrentLimit(limit) == REVLibError.kOk;
    }

    public boolean setSecondaryCurrentLimit(double limit, int chopCycles) {
        return spark.setSecondaryCurrentLimit(limit, chopCycles) == REVLibError.kOk;
    }

    public boolean setIdleMode(IdleMode mode) {
        return spark.setIdleMode(mode) == REVLibError.kOk;
    }

    public IdleMode getIdleMode() {
        return spark.getIdleMode();
    }

    public boolean enableVoltageCompensation(double nominalVoltage) {
        return spark.enableVoltageCompensation(nominalVoltage) == REVLibError.kOk;
    }

    public boolean disableVoltageCompensation() {
        return spark.disableVoltageCompensation() == REVLibError.kOk;
    }

    public double getVoltageCompensationNominalVoltage() {
        return spark.getVoltageCompensationNominalVoltage();
    }

    public boolean setOpenLoopRampRate(double rate) {
        return spark.setOpenLoopRampRate(rate) == REVLibError.kOk;
    }

    public boolean setClosedLoopRampRate(double rate) {
        return spark.setClosedLoopRampRate(rate) == REVLibError.kOk;
    }

    public double getOpenLoopRampRate() {
        return spark.getOpenLoopRampRate();
    }

    public double getClosedLoopRampRate() {
        return spark.getClosedLoopRampRate();
    }

    public boolean follow(HardwareSparkMAX leader) {
        return spark.follow(leader.getHWSpark()) == REVLibError.kOk;
    }

    public boolean follow(HardwareSparkMAX leader, boolean invert) {
        return spark.follow(leader.getHWSpark(), invert) == REVLibError.kOk;
    }

    public boolean isFollower() {
        return spark.isFollower();
    }

    public short getFaults() {
        return spark.getFaults();
    }

    public short getStickyFaults() {
        return spark.getStickyFaults();
    }

    public boolean getFault(FaultID faultID) {
        return spark.getFault(faultID);
    }

    public boolean getStickyFault(FaultID faultID) {
        return spark.getStickyFault(faultID);
    }

    @Override
    public double getBusVoltage() {
        return spark.getBusVoltage();
    }

    @Override
    public double getOutputPercent() {
        return spark.getAppliedOutput();
    }

    @Override
    public double getOutputCurrent() {
        return spark.getOutputCurrent();
    }

    @Override
    public double getTemperature() {
        return spark.getMotorTemperature();
    }

    @Override
    public Motor setSensorPhase(boolean phase) {
        // In brushless mode, this doesn't make sense.
        // Invert the motor instead.
        return this;
    }


    public boolean clearFaults() {
        return spark.clearFaults() == REVLibError.kOk;
    }

    public boolean burnFlash() {
        return spark.burnFlash() == REVLibError.kOk;
    }

    public boolean setCANTimeout(int milliseconds) {
        return spark.setCANTimeout(milliseconds) == REVLibError.kOk;
    }

    public boolean enableSoftLimit(SoftLimitDirection direction, boolean enable) {
        return spark.enableSoftLimit(direction, enable) == REVLibError.kOk;
    }

    public boolean setSoftLimit(SoftLimitDirection direction, float limit) {
        return spark.setSoftLimit(direction, limit) == REVLibError.kOk;
    }

    public double getSoftLimit(SoftLimitDirection direction) {
        return spark.getSoftLimit(direction);
    }

    public boolean isSoftLimitEnabled(SoftLimitDirection direction) {
        return spark.isSoftLimitEnabled(direction);
    }

    public CANSparkMax getHWSpark() {
        return spark;
    }
}
