package frc.robot.drive.routines;



import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.ui.ContinuousRange;

public class ArcadeClimb extends DriveRoutine {
    private double scale = 1;
    private ContinuousRange move;
    private ContinuousRange turn;

    public ArcadeClimb(String name, double scale, ContinuousRange move,
            ContinuousRange turn) {
        super(name, ControlMode.DutyCycle);
        this.scale = scale;
        this.move = move;
        this.turn = turn;

        Chart.register(() -> move.read(), "UI/%s/Move", name);
        Chart.register(() -> turn.read(), "UI/%s/Turn", name);
    }

    @Override
    public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
        double m = move.read();
        double t = turn.read();
        DriveMotion driveMotion = arcadeToTank(m, t, scale);
        // Restrict motors to only positive direction during climb
        driveMotion.left = Math.max(0.0, driveMotion.left);
        driveMotion.right = Math.max(0.0, driveMotion.right);
        return driveMotion;
    }
}
