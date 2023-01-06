package frc.robot.subsystems;



import frc.robot.interfaces.LogHelper;
import frc.robot.lib.chart.Chart;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A shim layer between a real subsystem and the robot controller so that the
 * behavior can be overridden with the button box.
 * 
 * It wraps the real interface and chooses what it should pass down the the
 * wrapped subsystem.
 * 
 * It uses a simulator when not using the real interface so that it still
 * behaves like the real subsystem.
 * 
 * The button box has a switch to select between: - automatic: (just pass
 * through to the real interface) - manual: only listen to the pots/buttons on
 * the button box - off: nothing gets through to the subsystem.
 * 
 * Note: Using anything other than automatic risks damage to the robot.
 * 
 * USE AT YOUR OWN RISK!!
 * 
 * Example: OverrideableSubsystem overridable = new OverridableSubsystem("Lift",
 * LiftInterface.class, lift, liftSimulator, mockLift);
 * // Get the endpoint that the controller would use.
 * IntakeInterface normalIntake = intakeOverride.getNormalInterface();
 * // Get the endpoint that the diag box uses.
 * IntakeInterface overrideLift = intakeOverride.getOverrideInterface();
 * // And in OI, map the buttons:
 * onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.LIFT_ADD_HEIGHT), () ->
 * overrideLift.liftHeightByDelta(2));
 * onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.LIFT_DECREASE_HEIGHT), () ->
 * overrideLift.liftHeightByDelta(-2));
 * onTriggered(buttonBoxJoystick.getButton(ButtonBoxMap.SHIFTER_BUTTON), () ->
 * overrideLift.setGear(overridableLift.isInHighGear()));
 * 
 * onTriggered(box.getButton(OperatorBoxButtons.INTAKE_DISABLE), () -> overridable.turnOff());
 * onTriggered(box.getButton(OperatorBoxButtons.INTAKE_MANUAL), () -> overridable.setManualMode());
 * onUntriggered(
 * Switch.or(box.getButton(OperatorBoxButtons.INTAKE_MANUAL),
 * box.getButton(OperatorBoxButtons.INTAKE_DISABLE)),
 * () -> overridable.setAutomaticMode());
 *
 * Issues: Doesn't poll for pots etc. - may not be necessary.
 */
public class OverridableSubsystem<SubIF> implements LogHelper {

    // The real and simulator lifts that commands are sent to depending on the mode.
    private String name;
    private SubIF normalInterface, overrideInterface;

    private enum OverrideMode {
        AUTOMATIC(1), MANUAL(-1), OFF(0);

        private OverrideMode(int value) {
            this.value = value;
        }

        public final int value;
    }

    private OverrideMode mode = OverrideMode.AUTOMATIC;

    @SuppressWarnings("unchecked")
    public OverridableSubsystem(String name, Class<?> clazz, SubIF real, SubIF simulator,
            SubIF mock) {
        this.name = name;
        Chart.register(() -> (double) mode.value, "%s/overrideMode", name);

        // Create the magic that allows this class to pick between which backend is used by what.
        InvocationHandler normalHandler = new InvocationHandler() {
            /**
             * Called to pick the between the real or simulator whenever the
             * instance returned by getNormalInterface is called.
             */
            @Override
            public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
                return method.invoke(isAuto() ? real : simulator, args);
            }
        };
        // Return a proxy so that when the subsystem methods are called, it choses depending
        // which subsystem to use depending on the override switch setting.
        normalInterface = (SubIF) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz}, normalHandler);

        InvocationHandler overrideHandler = new InvocationHandler() {
            /**
             * Called to pick the between the real or mock whenever the
             * instance returned by getOverrideInterface is called.
             */
            @Override
            public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
                return method.invoke(isManual() ? real : mock, args);
            }
        };
        // Return a proxy so that when the subsystem methods are called, it choses depending
        // which subsystem to use depending on the override switch setting.
        overrideInterface = (SubIF) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[] {clazz}, overrideHandler);
    }

    /**
     * Determine which thing (real or simulator) is used on the normal nterface. In
     * automatic mode, this is the real interface. Normally passed to the controller.
     */
    public SubIF getNormalInterface() {
        return normalInterface;
    }

    /**
     * Determine which type of subsystem (real or simulator) is used on the override
     * interface. In automatic and off modes, this is the simulator interface so
     * that the button box overrides are ignored. Normally used in OI for the diagBox
     * button mappings.
     */
    public SubIF getOverrideInterface() {
        return overrideInterface;
    }

    // Mode change methods.
    public void setAutomaticMode() {
        // This may need to be more clever to carry over state.
        debug("switched to normal/automatic mode");
        mode = OverrideMode.AUTOMATIC;
    }

    public void setManualMode() {
        // This may need to be more clever to carry over state.
        debug("switched manual mode");
        mode = OverrideMode.MANUAL;
    }

    public void turnOff() {
        // This may need to be more clever to carry over state.
        debug("switched off");
        mode = OverrideMode.OFF;
    }

    private boolean isManual() {
        return mode == OverrideMode.MANUAL;
    }

    private boolean isAuto() {
        return mode == OverrideMode.AUTOMATIC;
    }

    @Override
    public String getName() {
        return name;
    }
}
