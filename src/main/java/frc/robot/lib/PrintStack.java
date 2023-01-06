package frc.robot.lib;



import frc.robot.lib.log.Log;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PrintStack {

    public static void trace(String details) {
        try {
            throw new Exception();
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.error("Print", details);
            Log.error("Print", errors.toString());
        }
    }
}
