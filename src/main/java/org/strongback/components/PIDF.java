package org.strongback.components;

/**
 * Store PIDF values for motor control.
 * 
 * Saves passing four parameters around everywhere.
 */
public class PIDF {
    public double p, i, d, f;

    public PIDF(double p, double i, double d, double f) {
        this.p = p;
        this.i = i;
        this.d = d;
        this.f = f;
    }

    public void readFrom(NetworkTableHelper networkTable) {
        p = networkTable.get("p", p);
        i = networkTable.get("i", i);
        d = networkTable.get("d", d);
        f = networkTable.get("f", f);
    }

    public void saveTo(NetworkTableHelper networkTable) {
        networkTable.set("p", p);
        networkTable.set("i", i);
        networkTable.set("d", d);
        networkTable.set("f", f);
    }
}
