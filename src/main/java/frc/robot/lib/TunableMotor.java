package frc.robot.lib;



import frc.robot.Config;
import org.strongback.Executable;
import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Motor;
import org.strongback.components.NetworkTableHelper;
import org.strongback.components.PIDF;


/**
 * Whenever a motor is created, networkTable for the motor PIDF values are implemented from here.
 * The PIDF values are initiliased on the network table and and the motors are updated from the PIDF
 * values in the network tables once per second. This allows the values to be updated on the fly by
 * changing
 * the values in the network tables.
 */

class TunableMotor implements Executable {
    private final Motor motor;
    private final NetworkTableHelper networkTable;
    private double lastUpdateSec = 0;
    private final PIDF pidf;

    public static void tuneMotor(Motor motor, PIDF pidf, NetworkTableHelper networkTable) {
        var tunable = new TunableMotor(motor, pidf, networkTable);
        pidf.saveTo(networkTable);
        Strongback.executor().register(tunable, Priority.LOW);
    }

    public TunableMotor(Motor motor, PIDF pidf, NetworkTableHelper networkTable) {
        this.motor = motor;
        this.pidf = pidf;
        this.networkTable = networkTable;
    }

    // Executes the command 1 time every 1 second.
    @Override
    public void execute(long timeInMillis) {
        double now = Strongback.timeSystem().currentTime();
        if (now < lastUpdateSec + Config.intervals.dashboardUpdateSec)
            return;
        update();
        lastUpdateSec = now;
    }

    private void update() {
        pidf.readFrom(networkTable);
        motor.setPIDF(0, pidf);
    }
}
