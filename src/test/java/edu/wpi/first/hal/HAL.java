package edu.wpi.first.hal;

/**
 * Mock out the JNI classes that can't easily be included in unit tests.
 * 
 * There is likely a much better way to do this.
 */

public class HAL {
    // Don't report back.
    public static void report(int resource_type, int instanace) {}
}
