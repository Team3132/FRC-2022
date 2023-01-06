package frc.robot.lib;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Extract the team number from the hostname.
 * The team number is supplied by the drivers station,
 * but it may not be connected yet.
 */
public class TeamNumber {
    private static int teamNumber = 0;

    public static int get() {
        if (teamNumber == 0) {
            String hostname = "";
            try {
                Runtime run = Runtime.getRuntime();
                // Run the 'hostname' command and read it's output (eg "roborio-3132-frc.local")
                Process proc = run.exec("hostname");
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(proc.getInputStream()));
                hostname = in.readLine();
                teamNumber = Integer.parseInt(hostname.split("-")[1]);
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            } catch (NumberFormatException e1) {
                System.out.println("Cannot parse team number '" + hostname + "'");
                System.exit(1);
            }
        }
        return teamNumber;
    }
}
