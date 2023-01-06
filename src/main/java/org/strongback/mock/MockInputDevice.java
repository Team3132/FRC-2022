package org.strongback.mock;



import org.strongback.components.Switch;
import org.strongback.components.ui.ContinuousRange;
import org.strongback.components.ui.DirectionalAxis;
import org.strongback.components.ui.InputDevice;
import org.strongback.components.ui.Trigger;

public class MockInputDevice implements InputDevice {
    private final int numAxes;
    private final int numButtons;
    private final int numPOV;
    private MockContinuousRange[] axes; // start at zero
    private MockSwitchImplementation[] buttons; // start at 1 => we adjust offset
    private MockDirectionalAxis[] POVs; // start at zero

    public MockInputDevice(int numAxes, int numButtons, int numPOV) {
        this.numAxes = numAxes;
        this.numButtons = numButtons;
        this.numPOV = numPOV;
        axes = new MockContinuousRange[numAxes];
        for (int i = 0; i < numAxes; i++) {
            axes[i] = new MockContinuousRange(0);
        }
        buttons = new MockSwitchImplementation[numButtons];
        for (int i = 0; i < numButtons; i++) {
            buttons[i] = new MockSwitchImplementation();
            buttons[i].setTriggered(false);
        }
        POVs = new MockDirectionalAxis[numPOV];
        for (int i = 0; i < numPOV; i++) {
            POVs[i] = new MockDirectionalAxis(0);
        }
    }

    @Override
    public String getName() {
        return "MockInputDevice";
    }

    @Override
    public ContinuousRange getAxis(int axis) {
        if (axis < 0 || axis >= numAxes) {
            return null;
        }
        return axes[axis];
    }

    @Override
    public Switch getButton(int button) {
        if (button < 1 || button > numButtons) {
            return null;
        }
        return buttons[button - 1];
    }

    @Override
    public Trigger button(int button) {
        if (button < 1 || button > numButtons) {
            return null;
        }
        return new Trigger(getName(), "button " + button, () -> buttons[button - 1].isTriggered());
    }

    @Override
    public DirectionalAxis getDPad(int pad) {
        if (pad < 0 || pad >= numPOV) {
            return null;
        }
        return POVs[pad];
    }

    @Override
    public Switch getDPad(int pad, int direction) {
        if (pad < 0 || pad >= numPOV) {
            return null;
        }
        DirectionalAxis d = POVs[pad];
        return () -> d.getDirection() == direction;
    }

    @Override
    public int getAxisCount() {
        return numAxes;
    }

    @Override
    public int getButtonCount() {
        return numButtons;
    }

    @Override
    public int getPOVCount() {
        return numPOV;
    }

    public MockInputDevice setAxis(int axis, double value) {
        if (axis < 0 || axis >= numAxes) {
            return null;
        }
        axes[axis].set(value);
        return this;
    }

    public MockInputDevice setButton(int button, boolean triggered) {
        if (button < 1 || button > numButtons) {
            return null;
        }
        buttons[button - 1].setTriggered(triggered);
        return this;
    }

    public MockInputDevice setDPad(int pad, int direction) {
        if (pad < 0 || pad >= numPOV) {
            return null;
        }
        POVs[pad].setDirection(direction);
        return this;
    }
}
