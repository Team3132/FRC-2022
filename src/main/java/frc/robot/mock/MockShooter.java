package frc.robot.mock;



import frc.robot.interfaces.Shooter;
import frc.robot.lib.Subsystem;

public class MockShooter extends Subsystem implements Shooter {

    private double targetRPS = 0;
    private double hoodTargetAngle = 0;

    public MockShooter() {
        super("MockShooter");
    }

    @Override
    public void setTargetRPS(double rps) {
        targetRPS = rps;
    }

    @Override
    public boolean isAtTargetSpeed() {
        return true;
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }

    @Override
    public void setHoodTargetAngle(double degrees) {
        hoodTargetAngle = degrees;
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
