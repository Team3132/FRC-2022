package org.strongback.mock;



import java.util.function.DoubleSupplier;

public class MockDoubleSupplier extends MockZeroable implements DoubleSupplier {

    private volatile double value;

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

}
