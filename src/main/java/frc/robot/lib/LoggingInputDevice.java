package frc.robot.lib;



import frc.robot.lib.chart.Chart;
import org.strongback.components.ui.InputDevice;

/**
 * Add generic logging to a Human Interface device (an InputDevice)
 * 
 * This could be customised for a particular joystick later.
 */
public class LoggingInputDevice {

    public static void AddLog(InputDevice input, String name) {
        for (int i = 0; i < input.getAxisCount(); i++) {
            final int axis = i;
            Chart.register(() -> input.getAxis(axis).read(), "%s/Axis/%d", name, i);
        }

        for (int i = 0; i < input.getButtonCount(); i++) {
            final int button = i + 1;
            Chart.register(input.getButton(button), "%s/Button/%d", name, i);
        }

        for (int i = 0; i < input.getPOVCount(); i++) {
            final int axis = i;
            Chart.register(input.getDPad(axis), "%s/DPad/%d", name, i);
        }
    }
}
