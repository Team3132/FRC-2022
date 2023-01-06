package frc.robot.lib;



import java.util.function.DoubleSupplier;

public class LowPassFilter implements DoubleSupplier {
    private double last = 0;
    private final double alpha;
    private final DoubleSupplier source;
    private boolean initialized = false;

    public LowPassFilter(DoubleSupplier source, double alpha) {
        this.source = source;
        this.alpha = alpha;
    }

    @Override
    public double getAsDouble() {
        if (!initialized) {
            // Do this here as source may not be initialized when the constructor is called.
            last = source.getAsDouble();
            initialized = true;
        }
        return filterValues(source.getAsDouble(), last, alpha);
    }

    public static double filterValues(double newValue, double oldValue, double alpha) {
        return alpha * newValue + (1 - alpha) * oldValue;
    }

}
