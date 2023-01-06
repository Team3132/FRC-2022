
package frc.robot.simulator;



import frc.robot.interfaces.Shooter;
import frc.robot.lib.Subsystem;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class ShooterSimulator extends Subsystem implements Shooter {

    private double targetRPS = 0;
    private double shooterTime = 0;
    private double hoodTargetAngle = 0;

    public ShooterSimulator() {
        super("ShooterSimulator");
    }

    @Override
    public void setTargetRPS(double rps) {
        this.targetRPS = rps;
        this.shooterTime = System.currentTimeMillis();
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }

    @Override
    public boolean isAtTargetSpeed() {
        if ((System.currentTimeMillis() - this.shooterTime) < 1000) {
            return true;
        }
        return false;
    }

    @Override
    public void setHoodTargetAngle(double angle) {
        this.hoodTargetAngle = angle;
    }

    @Override
    public double getHoodTargetAngle() {
        return hoodTargetAngle;
    }

    @Override
    public boolean isHoodAtTargetAngle() {
        return true;
    }
}
