package frc.robot.mock;



import java.util.function.IntSupplier;

public class MockIntSupplier implements IntSupplier {
    private int value = Integer.MAX_VALUE;

    @Override
    public int getAsInt() {
        return value;
    }

    public void set(int newValue) {
        System.out.println("setting to " + newValue);
        value = newValue;
    }
}
