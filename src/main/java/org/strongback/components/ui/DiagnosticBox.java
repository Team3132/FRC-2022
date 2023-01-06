package org.strongback.components.ui;



import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;
import org.strongback.function.IntToIntFunction;

/**
 * A button box that pretends to be a joystick. Many buttons with switches to
 * put it into automatic/manual/disable modes. Very useful for diagnostics.
 */
public interface DiagnosticBox extends InputDevice {
    public enum Colour {
        WHITE("white", 0, 0, 2, 23, 24), RED("red", 1, 2, 5, 25, 26), YELLOW("yellow", 2, 7, 5, 27,
                28), GREEN("green", 3, 12, 5, 29, 30), BLUE("blue", 4, 17, 5, 31, 32);

        public final String name;
        private final int potId;
        private final int buttonBase;
        private final int buttonCount;
        private final int manualButtonId;
        private final int disableButtonId;

        Colour(final String name, final int potId, final int buttonBase, final int numButtons,
                final int manualButtonId,
                final int disableButtonId) {
            this.name = name;
            this.potId = potId;
            this.buttonBase = buttonBase;
            this.buttonCount = numButtons;
            this.manualButtonId = manualButtonId;
            this.disableButtonId = disableButtonId;
        }
    }

    // What is this button?
    public static final int CLEAR_BUTTON1 = 22;

    public abstract ContinuousRange getWhitePot();

    public abstract ContinuousRange getRedPot();

    public abstract ContinuousRange getYellowPot();

    public abstract ContinuousRange getGreenPot();

    public abstract ContinuousRange getBluePot();

    public abstract Trigger whiteButton(int button);

    public abstract Trigger redButton(int button);

    public abstract Trigger yellowButton(int button);

    public abstract Trigger greenButton(int button);

    public abstract Trigger blueButton(int button);

    /**
     * Three position switch showing up as two buttons. Allows switching between
     * automatic, manual and disabled modes.
     */
    public abstract void overrideSwitch(Colour colour, Runnable automatic, Runnable manual,
            Runnable off);

    public static DiagnosticBox create(final IntToDoubleFunction axisToValue,
            final IntToBooleanFunction buttonNumberToSwitch, final IntToIntFunction dPad,
            final IntSupplier axisCount,
            final IntSupplier buttonCount, final IntSupplier POVCount) {
        return new DiagnosticBox() {
            @Override
            public String getName() {
                return "diagBox";
            }

            @Override
            public ContinuousRange getWhitePot() {
                return getPot(Colour.WHITE);
            }

            @Override
            public ContinuousRange getRedPot() {
                return getPot(Colour.RED);
            }

            @Override
            public ContinuousRange getYellowPot() {
                return getPot(Colour.YELLOW);
            }

            @Override
            public ContinuousRange getGreenPot() {
                return getPot(Colour.GREEN);
            }

            @Override
            public ContinuousRange getBluePot() {
                return getPot(Colour.BLUE);
            }

            private ContinuousRange getPot(final Colour colour) {
                return getAxis(colour.potId);
            }

            @Override
            public ContinuousRange getAxis(final int axis) {
                return () -> axisToValue.applyAsDouble(axis);
            }

            @Override
            public Trigger whiteButton(final int button) {
                return button(Colour.WHITE, button);
            }

            @Override
            public Trigger redButton(final int button) {
                return button(Colour.WHITE, button);
            }

            @Override
            public Trigger yellowButton(final int button) {
                return button(Colour.WHITE, button);
            }

            @Override
            public Trigger greenButton(final int button) {
                return button(Colour.WHITE, button);
            }

            @Override
            public Trigger blueButton(final int button) {
                return button(Colour.WHITE, button);
            }

            /**
             * Converts a colour to a button with validation. Buttons start from 1 and
             * typically go to 5. See the Colour enum for details.
             */
            private Trigger button(final Colour colour, final int button) {
                if (button < 1 || button > colour.buttonCount) {
                    return null;
                }
                return new Trigger(getName(), colour.name + " button " + button,
                        () -> buttonNumberToSwitch.applyAsBoolean(colour.buttonBase + button));
            }

            @Override
            public Switch getButton(final int button) {
                return () -> buttonNumberToSwitch.applyAsBoolean(button);
            }

            @Override
            public Trigger button(final int button) {
                return new Trigger(getName(), "button " + button,
                        () -> buttonNumberToSwitch.applyAsBoolean(button));
            }

            @Override
            public void overrideSwitch(Colour colour, Runnable automatic, Runnable manual,
                    Runnable off) {
                button(colour.disableButtonId).onPress(off);
                button(colour.manualButtonId).onPress(manual);
                button(colour.disableButtonId).onRelease(automatic);
                button(colour.manualButtonId).onRelease(automatic);
            }

            @Override
            public DirectionalAxis getDPad(final int pad) {
                return () -> dPad.applyAsInt(pad);
            }

            @Override
            public Switch getDPad(final int pad, final int direction) {
                return () -> dPad.applyAsInt(pad) == direction;
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
        };
    }

}
