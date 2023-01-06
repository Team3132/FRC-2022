package frc.robot.simulator;



import frc.robot.interfaces.Intake;
import frc.robot.lib.MovementSimulator;
import frc.robot.lib.Subsystem;
import org.strongback.components.Solenoid.Position;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class IntakeSimulator extends Subsystem implements Intake {
    private final double kMaxSpeed = 180; // degrees/sec
    private final double kMaxAccel = 200; // degrees/sec/sec
    private final double kMinAngle = 0;
    private final double kMaxAngle = 45;
    private final double kMovementTolerance = 1; // How close before it's classed as being in
                                                 // position.
    private MovementSimulator arm = new MovementSimulator("arm intake", kMaxSpeed, kMaxAccel,
            kMinAngle, kMaxAngle, kMovementTolerance);
    private long lastTimeMs = 0;

    private double rps;

    public IntakeSimulator() {
        super("IntakeSimulator");
    }

    @Override
    public void setPosition(Position position) {
        arm.setTargetPos(position == Position.EXTENDED ? kMaxAngle : kMinAngle);
    }

    @Override
    public boolean isInPosition() {
        return arm.isInPosition();
    }

    @Override
    public boolean isExtended() {
        return arm.getTargetPos() == kMaxAngle && arm.isInPosition();
    }

    @Override
    public boolean isRetracted() {
        return arm.getTargetPos() == kMinAngle && arm.isInPosition();
    }

    @Override
    public void setTargetRPS(double rps) {
        this.rps = rps;
    }

    @Override
    public double getTargetRPS() {
        return rps;
    }

    @Override
    public void execute(long timeInMillis) {
        if (lastTimeMs == 0) {
            lastTimeMs = timeInMillis;
            return;
        }
        // Update the lift position.
        arm.step((timeInMillis - lastTimeMs) / 1000.);
        lastTimeMs = timeInMillis;
    }

    public String toString() {
        return arm.toString();
    }
}
