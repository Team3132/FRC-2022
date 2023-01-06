package org.strongback.components.ui;



import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;

/**
 * A type of input device similar to an Xbox controller.
 */
public interface Dancepad extends InputDevice {

    // public final static double kThumbstickThreshold = 0.3;
    // public final static double kTriggerThreshold = 0.6;

    public abstract JoystickAxis cardinalAxis();

    public abstract Switch getNE();

    public abstract Trigger neButton();

    public abstract Switch getSE();

    public abstract Trigger seButton();

    public abstract Switch getSW();

    public abstract Trigger swButton();

    public abstract Switch getNW();

    public abstract Trigger nwButton();

    public abstract Switch getStart();

    public abstract Trigger startButton();

    public abstract Switch getSelect();

    public abstract Trigger selectButton();

    public abstract Switch getL1();

    public abstract Trigger L1Button();

    public abstract Switch getL2();

    public abstract Trigger L2Button();

    public abstract Switch getR1();

    public abstract Trigger R1Button();

    public abstract Switch getR2();

    public abstract Trigger R2Button();

    public static Dancepad create(IntToDoubleFunction axisToValue,
            IntToBooleanFunction buttonNumberToSwitch,
            IntSupplier axisCount, IntSupplier buttonCount,
            IntSupplier POVCount, ContinuousRange cardinalX, ContinuousRange cardinalY,
            BooleanSupplier neButton, BooleanSupplier seButton, BooleanSupplier swButton,
            BooleanSupplier nwButton,
            BooleanSupplier startButton, BooleanSupplier selectButton, BooleanSupplier L1Button,
            BooleanSupplier L2Button, BooleanSupplier R1Button, BooleanSupplier R2Button) {
        return new Dancepad() {
            @Override
            public String getName() {
                return "dancepad";
            }

            @Override
            public ContinuousRange getAxis(int axis) {
                return () -> axisToValue.applyAsDouble(axis);
            }

            @Override
            public Switch getButton(int button) {
                return () -> buttonNumberToSwitch.applyAsBoolean(button);
            }

            @Override
            public Trigger button(int button) {
                return trigger("button " + button,
                        () -> buttonNumberToSwitch.applyAsBoolean(button));
            }

            @Override
            public int getAxisCount() {
                return axisCount.getAsInt();
            }

            @Override
            public int getButtonCount() {
                return buttonCount.getAsInt();
            }

            public int getPOVCount() {
                return POVCount.getAsInt();
            }

            @Override
            public JoystickAxis cardinalAxis() {
                return new JoystickAxis(getName(), "cardinal axis", cardinalX, cardinalY, null,
                        0.5);
            }

            @Override
            public Switch getStart() {
                return () -> startButton.getAsBoolean();
            }

            @Override
            public Trigger startButton() {
                return trigger("start button", startButton);
            }

            @Override
            public Switch getSelect() {
                return () -> selectButton.getAsBoolean();
            }

            @Override
            public Trigger selectButton() {
                return trigger("select button", selectButton);
            }

            @Override
            public Switch getNE() {
                return () -> neButton.getAsBoolean();
            }

            @Override
            public Trigger neButton() {
                return trigger("NE Button", neButton);
            }

            @Override
            public Switch getSE() {
                return () -> neButton.getAsBoolean();
            }

            @Override
            public Trigger seButton() {
                return trigger("SE Button", seButton);
            }

            @Override
            public Switch getSW() {
                return () -> swButton.getAsBoolean();
            }

            @Override
            public Trigger swButton() {
                return trigger("SW Button", swButton);
            }

            @Override
            public Switch getNW() {
                return () -> nwButton.getAsBoolean();
            }

            @Override
            public Trigger nwButton() {
                return trigger("NW Button", nwButton);
            }

            @Override
            public Switch getL1() {
                return () -> L1Button.getAsBoolean();
            }

            @Override
            public Trigger L1Button() {
                return trigger("L1 Button", L1Button);
            }

            @Override
            public Switch getL2() {
                return () -> L2Button.getAsBoolean();
            }

            @Override
            public Trigger L2Button() {
                return trigger("L2 Button", L2Button);
            }

            @Override
            public Switch getR1() {
                return () -> R1Button.getAsBoolean();
            }

            @Override
            public Trigger R1Button() {
                return trigger("R1 Button", R1Button);
            }

            @Override
            public Switch getR2() {
                return () -> R2Button.getAsBoolean();
            }

            @Override
            public Trigger R2Button() {
                return trigger("R2 Button", R2Button);
            }

            @Override
            public DirectionalAxis getDPad(int pad) {
                return () -> 0;
            }

            @Override
            public Switch getDPad(int pad, int direction) {
                return Switch.neverTriggered();
            }

            private Trigger trigger(String button, BooleanSupplier supplier) {
                return new Trigger(getName(), button, supplier);
            }
        };
    }

}
