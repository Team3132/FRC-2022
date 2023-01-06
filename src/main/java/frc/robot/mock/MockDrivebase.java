package frc.robot.mock;



import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.Drivebase;

public class MockDrivebase implements Drivebase {
    private DriveRoutineParameters parameters =
            new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE);
    String name = "MockDrivebase";;

    public MockDrivebase() {}

    @Override
    public void execute(long timeInMillis) {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enable() {}

    @Override
    public void disable() {}

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void cleanup() {}

    @Override
    public void setDriveRoutine(DriveRoutineParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public DriveRoutineParameters getDriveRoutineParameters() {
        return parameters;
    }

    @Override
    public boolean hasFinished() {
        return true;
    }

    @Override
    public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine) {}

    @Override
    public void setLeftDistance(double pos) {}

    @Override
    public void setRightDistance(double pos) {}

    @Override
    public double getLeftDistance() {
        return 0;
    }

    @Override
    public double getRightDistance() {
        return 0;
    }

    @Override
    public double getLeftSpeed() {
        return 0;
    }

    @Override
    public double getRightSpeed() {
        return 0;
    }
}
