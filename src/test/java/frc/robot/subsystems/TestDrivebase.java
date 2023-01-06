package frc.robot.subsystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.robot.drive.routines.DriveRoutine;
import frc.robot.interfaces.Drivebase;
import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.interfaces.Drivebase.DriveRoutineParameters;
import frc.robot.interfaces.Drivebase.DriveRoutineType;
import org.junit.jupiter.api.Test;
import org.strongback.components.Motor.ControlMode;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;

public class TestDrivebase {

    class MockDriveRoutine extends DriveRoutine {
        public String name;
        public int callCount = 0;
        public double leftPower = 0;
        public double rightPower = 0;

        public MockDriveRoutine(String name) {
            super(name, ControlMode.DutyCycle);
            this.name = name;
        }

        @Override
        public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
            callCount++;
            return new DriveMotion(leftPower, rightPower);
        }

        @Override
        public void enable() {}

        @Override
        public void disable() {}

        @Override
        public boolean hasFinished() {
            return true;
        }

        @Override
        public String getName() {
            return "mock";
        }
    }

    @Test
    public void testDriveRoutine() {
        MockMotor leftMotor = Mock.stoppedMotor();
        MockMotor rightMotor = Mock.stoppedMotor();
        MockDriveRoutine arcade = new MockDriveRoutine("MockArcade");
        Drivebase drive = new DrivebaseImpl(leftMotor, rightMotor);
        // Register this drive routine so it can be used.
        drive.registerDriveRoutine(DriveRoutineType.ARCADE_DUTY_CYCLE, arcade);
        // Tell the drive subsystem to use it.
        drive.setDriveRoutine(new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE));
        int expectedCallCount = 0;

        // Subsystems should start disabled, so shouldn't be calling the
        // DrivedriveRoutine.
        assertEquals(expectedCallCount, arcade.callCount);
        drive.execute(0);
        assertEquals(expectedCallCount, arcade.callCount);
        assertEquals(0, leftMotor.get(), 0.01);
        assertEquals(0, rightMotor.get(), 0.01);

        // Enable the drivebase
        arcade.leftPower = 0.5;
        arcade.rightPower = 0.75;
        drive.enable();
        drive.execute(0); // Should call getMotion() on driveRoutine.
        assertEquals(++expectedCallCount, arcade.callCount);
        // Check that the motors now have power.
        assertEquals(arcade.leftPower, leftMotor.get(), 0.01);
        assertEquals(arcade.rightPower, rightMotor.get(), 0.01);

        // Update the speed and see if the motors change.
        arcade.leftPower = -0.1;
        arcade.rightPower = 1;
        drive.execute(0); // Should call getMotion() on driveRoutine.
        assertEquals(++expectedCallCount, arcade.callCount);
        // Check that the motors now have power.
        assertEquals(arcade.leftPower, leftMotor.get(), 0.01);
        assertEquals(arcade.rightPower, rightMotor.get(), 0.01);

        // Change driveRoutine and see if the outputs are different
        MockDriveRoutine cheesy = new MockDriveRoutine("MockCheesy");
        cheesy.leftPower = 1;
        cheesy.rightPower = -1;
        drive.registerDriveRoutine(DriveRoutineType.CHEESY, cheesy);
        // Tell the drive subsystem to use it.
        drive.setDriveRoutine(new DriveRoutineParameters(DriveRoutineType.CHEESY));
        drive.execute(0);
        assertEquals(1, cheesy.callCount); // first time running this driveRoutine
        assertEquals(cheesy.leftPower, leftMotor.get(), 0.01);
        assertEquals(cheesy.rightPower, rightMotor.get(), 0.01);

        // Disable and confirm that the driveRoutine isn't called and the motors are
        // stopped
        drive.disable();
        drive.execute(0); // Should no call getMotion() on driveRoutine.
        assertEquals(expectedCallCount, arcade.callCount);
        assertEquals(0, leftMotor.get(), 0.01);
        assertEquals(0, rightMotor.get(), 0.01);
    }

}
