// @Author RKouchoo
package frc.robot.lib;



import edu.wpi.first.wpilibj.Servo;

public class LinearServo {

    protected final double SERVO_MIN_INCHES = 0.0;
    public static final double SERVO_MIN = 1.0; // REX: This is a constant and should be in
                                                // Constants
    public static final double INCHES_PER_SECOND = 0.275591; // REX: This is a constant and should
                                                             // be in Constants
    public static final double SERVO_MAX = 2.0; // REX: This is a constant and should be in
                                                // Constants
    protected double targetPos;
    int SERVO_PORT;
    final double SERVO_LENGTH_INCHES;
    final double SERVO_LIMIT;

    final boolean isDebugEnabled = false;
    protected double atPosTimeSeconds = 0.0;
    protected double rawLastPos = 0.0;

    protected Servo myServo;

    // NOTE: The current Servo's that we are using have the value 4.92125984252 as their max.

    public LinearServo(int port, double length, double limit) {
        SERVO_LENGTH_INCHES = length;
        SERVO_PORT = port;
        SERVO_LIMIT = limit;
        myServo = new Servo(SERVO_PORT);

        // myServo.setBounds(max, deadMax, mid deadMin, min);
        myServo.setBounds(2, 1.5, 1.5, 1.5, 1);

        // Reset potentially saved values
        atPosTimeSeconds = 0.0;
        rawLastPos = 0.0;
    }

    public void setServo(double inputInches) {

        double newTargetInches = MathUtil.clamp(inputInches, SERVO_LIMIT, 0.0);
        double limitTargetPos =
                MathUtil.scale(newTargetInches, 0.0, SERVO_LENGTH_INCHES, -1.0, 1.0);

        // See how much the servo has moved
        double targetPosDelta = Math.abs(limitTargetPos - targetPos);
        if (targetPos == limitTargetPos)
            return;
        targetPos = limitTargetPos;

        // Calculate the time from when it will be at the target pos
        atPosTimeSeconds = calcTime(INCHES_PER_SECOND, targetPosDelta);
    }

    public double getTargetPosition() {
        return targetPos;
    }

    private double calcTime(double inchesPerSecond, double inchesDelta) { // REX: This could
                                                                          // probably be folded into
                                                                          // the call on line 54
        return inchesDelta / inchesPerSecond + System.currentTimeMillis() / 1000; // REX: Please
                                                                                  // change to
                                                                                  // Timer.getFPGATimestamp()
                                                                                  // which is of
                                                                                  // higher accuracy
                                                                                  // and already in
                                                                                  // seconds.
    }

    public boolean isOnTarget() {
        return System.currentTimeMillis() / 1000 >= atPosTimeSeconds; // REX: Please change to
                                                                      // Timer.getFPGATimestamp()
    }

    @SuppressWarnings("unused")
    public void updateServo() {
        targetPos = MathUtil.clamp(targetPos, 1.0, -1.0);
        myServo.setSpeed(targetPos);

        if (isDebugEnabled == true) {
            System.out.println("Servo Pos: " + targetPos);
        }

    }

}
