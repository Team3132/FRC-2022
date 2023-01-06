package org.strongback.mock;



import org.strongback.components.ui.DirectionalAxis;

public class MockDirectionalAxis implements DirectionalAxis {
    public int direction;

    public MockDirectionalAxis(int initial) {
        direction = initial;
    }

    public MockDirectionalAxis setDirection(int direction) {
        this.direction = direction;
        return this;
    }

    @Override
    public int getDirection() {
        return direction;
    }
}
