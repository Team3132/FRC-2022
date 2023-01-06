package frc.robot.lib;



import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import org.strongback.components.Gyroscope;

/*
 * This class grabs the interesting data from the navX for use in the location subsystem.
 * We rotate the three axis as necessary to return robot oriented yaw, pitch and roll.
 * 
 * We also pass through the Gyro compatible interface so we can use the navX as the robot's gyro.
 * 
 * We reset the navX's view of the angle when we first start.
 * 
 * This class should become a subsystem, and have a thread which compensates for the navX drift.
 * First cut at compensation:
 * 1) when the drivebase is not moving (i.e the encoders are not changing, AND the navX is not
 * detecting movement)
 * we watch the navX and calculate its drift over a period of time. We set the drift rate per unit
 * time and periodically
 * adjust the drift (or we can calculate the adjusted drift in 'getAngle()' When we become a
 * subsystem
 * we need to decide how often to update the drift values.
 * 
 * When any movement stops we start to calculate and adjust the drift rate again.
 * 
 * 
 * The NavX has positive YAW as clockwise. We need it to be negative.
 * Since we start at an angle of zero we need to return the negative.
 * This affects YAW and angle
 * 
 */
public class NavXGyroscope implements Gyroscope, DashboardUpdater {
    public enum Drift {
        NOT_STARTED, CALCULATING, FINISHED
    }

    private AHRS ahrs = null;
    private double baseAngle = 0;
    private double basePitch = 0;
    private double driftStartTime; // In drift calculations we see how much the gyro drifts during a
                                   // known stop period.
    private double driftStartHeading;
    private double driftRate = 0.0; // rate of gyro drift in degrees per second.
    private Drift driftState = Drift.NOT_STARTED;
    private String name;

    public NavXGyroscope(String name, boolean present) {
        this.name = name;

        ahrs = null;
        if (present) {
            try {
                /* Communicate w/navX MXP via the MXP SPI Bus. */
                /* Alternatively: I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB */
                /*
                 * See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for details.
                 */
                ahrs = new AHRS(SPI.Port.kMXP);
            } catch (RuntimeException ex) {
                DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(),
                        true);
                ahrs = null;
            }
        }
        if (ahrs != null) {
            Chart.register((() -> {
                return getAngle();
            }), "%s/angle", name);
            Chart.register((() -> {
                return getYaw();
            }), "%s/yaw", name);
            Chart.register((() -> {
                return getRoll();
            }), "%s/roll", name);
            Chart.register((() -> {
                return getPitch();
            }), "%s/pitch", name);
            Chart.register((() -> {
                return driftRate;
            }), "%s/drift", name);
            Chart.register((() -> {
                return isCalibrating() ? 1.0 : 0.0;
            }), "%s/Misc/Calibrating", name);
            Chart.register((() -> {
                return getDisplacementX();
            }), "%s/disp/X", name);
            Chart.register((() -> {
                return getDisplacementY();
            }), "%s/disp/Y", name);
            Chart.register((() -> {
                return getDisplacementZ();
            }), "%s/disp/Z", name);
            Chart.register((() -> {
                return getWorldLinearAccelX();
            }), "%s/WorldAccel/X", name);
            Chart.register((() -> {
                return getWorldLinearAccelY();
            }), "%s/WorldAccel/Y", name);
            Chart.register((() -> {
                return getWorldLinearAccelZ();
            }), "%s/WorldAccel/Z", name);
            Chart.register((() -> {
                return getRawAccelX();
            }), "%s/RawAccel/X", name);
            Chart.register((() -> {
                return getRawAccelY();
            }), "%s/RawAccel/Y", name);
            Chart.register((() -> {
                return getRawAccelZ();
            }), "%s/RawAccel/Z", name);
        }
        zero();
    }

    /**
     * Manual drift calculations.
     * 
     * @return True is the drift calculations can start. False if the gyro is still calibrating.
     */
    public boolean startDriftCalculation() {
        if (isCalibrating())
            return false;
        driftStartTime = Timer.getFPGATimestamp();
        driftStartHeading = getRawAngle();
        driftState = Drift.CALCULATING;
        return true;
    }

    /**
     * Manual Drift Calculations. The end routine measures the drift that occurred, and the
     * amount of time that passed. We assume a constant drift during that time.
     */
    public void endDriftCalculation() {
        driftRate =
                (getRawAngle() - driftStartHeading) / (Timer.getFPGATimestamp() - driftStartTime);
        driftState = Drift.FINISHED;
        Log.debug("NavX", "Finished Drift Calculation: %f degrees per second", driftRate);
    }

    public Drift currentDriftCalculation() {
        return driftState;
    }

    public double getYaw() {
        if (ahrs == null)
            return 0.0;
        return -ahrs.getYaw();
    }

    public double getRoll() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getRoll();
    }

    public double getPitch() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getPitch() - basePitch;
    }

    public void calibrate() {}

    public String getName() {
        return name;
    }

    public Gyroscope zero() {
        if (ahrs == null) {
            baseAngle = 0.0;
            basePitch = 0.0;
        } else {
            baseAngle = getRawAngle();
            basePitch = ahrs.getYaw();
            driftStartTime = Timer.getFPGATimestamp();
        }
        System.out.println("reset baseAngle = " + baseAngle);
        Log.debug("NavX", "reset baseAngle = %f", baseAngle);
        return this;
    }

    public void setAngle(double angle) {
        zero();
        baseAngle -= angle;
        Log.debug("NavX", "set baseAngle = %f", baseAngle);
    }

    private double getRawAngle() {

        if (ahrs == null)
            return 0.0;
        return -ahrs.getAngle();
    }

    @Override
    public double getAngle() {
        return (getRawAngle() - baseAngle);
    }

    @Override
    public double getRate() {
        if (ahrs == null)
            return 0.0;
        return -ahrs.getRate();
    }

    public void free() {}

    private double getRawAccelZ() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getRawAccelZ();
    }

    private double getRawAccelY() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getRawAccelY();
    }

    private double getRawAccelX() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getRawAccelX();
    }

    private double getWorldLinearAccelZ() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getWorldLinearAccelZ();
    }

    private double getWorldLinearAccelY() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getWorldLinearAccelY();
    }

    private double getDisplacementY() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getDisplacementY();
    }

    private double getWorldLinearAccelX() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getWorldLinearAccelX();
    }

    private double getDisplacementZ() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getDisplacementZ();
    }

    private double getDisplacementX() {
        if (ahrs == null)
            return 0.0;
        return ahrs.getDisplacementX();
    }

    public boolean isCalibrating() {
        if (ahrs == null)
            return true;
        return ahrs.isCalibrating();
    }

    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber("Gyro angle: ", getAngle());
        SmartDashboard.putNumber("Gyro Yaw: ", getYaw());
        SmartDashboard.putNumber("Gyro Roll: ", getRoll());
        SmartDashboard.putNumber("Gyro Pitch: ", getPitch());
        SmartDashboard.putNumber("Gyro DriftRate: ", driftRate);
    }
}
