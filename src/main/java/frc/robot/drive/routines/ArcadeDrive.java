package frc.robot.drive.routines;



import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.lib.chart.Chart;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.ui.ContinuousRange;

public class ArcadeDrive extends DriveRoutine {
    private double scale = 1;
    private ContinuousRange move;
    private ContinuousRange turn;

    public ArcadeDrive(String name, ControlMode mode, double scale, ContinuousRange move,
            ContinuousRange turn) {
        super(name, mode);
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
        return arcadeToTank(m, t, scale);
    }
}
