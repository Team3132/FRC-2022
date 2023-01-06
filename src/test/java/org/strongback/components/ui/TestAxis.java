package org.strongback.components.ui;



import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.strongback.components.ui.Axis.CompassPoint;

public class TestAxis {
    @Test
    public void testCompassPoints() {
        assertEquals(CompassPoint.getClosestPoint(-360 - 22), CompassPoint.NORTH);
        assertEquals(CompassPoint.getClosestPoint(0 - 22), CompassPoint.NORTH);
        assertEquals(CompassPoint.getClosestPoint(0), CompassPoint.NORTH);
        assertEquals(CompassPoint.getClosestPoint(0 + 22), CompassPoint.NORTH);
        assertEquals(CompassPoint.getClosestPoint(45 - 22), CompassPoint.NORTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(45), CompassPoint.NORTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(45 + 22), CompassPoint.NORTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(90 - 22), CompassPoint.EAST);
        assertEquals(CompassPoint.getClosestPoint(90), CompassPoint.EAST);
        assertEquals(CompassPoint.getClosestPoint(90 + 22), CompassPoint.EAST);
        assertEquals(CompassPoint.getClosestPoint(135 - 22), CompassPoint.SOUTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(135), CompassPoint.SOUTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(135 + 22), CompassPoint.SOUTH_EAST);
        assertEquals(CompassPoint.getClosestPoint(180 - 22), CompassPoint.SOUTH);
        assertEquals(CompassPoint.getClosestPoint(180), CompassPoint.SOUTH);
        assertEquals(CompassPoint.getClosestPoint(180 + 22), CompassPoint.SOUTH);
        assertEquals(CompassPoint.getClosestPoint(225 - 22), CompassPoint.SOUTH_WEST);
        assertEquals(CompassPoint.getClosestPoint(225), CompassPoint.SOUTH_WEST);
        assertEquals(CompassPoint.getClosestPoint(225 + 22), CompassPoint.SOUTH_WEST);
        assertEquals(CompassPoint.getClosestPoint(270 - 22), CompassPoint.WEST);
        assertEquals(CompassPoint.getClosestPoint(270), CompassPoint.WEST);
        assertEquals(CompassPoint.getClosestPoint(270 + 22), CompassPoint.WEST);
        assertEquals(CompassPoint.getClosestPoint(315 - 22), CompassPoint.NORTH_WEST);
        assertEquals(CompassPoint.getClosestPoint(315), CompassPoint.NORTH_WEST);
        assertEquals(CompassPoint.getClosestPoint(315 + 22), CompassPoint.NORTH_WEST);
    }
}
