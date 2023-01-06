package frc.robot.interfaces;

/**
 * The smart dashboard is a way to pass important information to the operator's console.
 * Each subsystem needs to have a method that can be called to update the smart dashboard with
 * important information.
 * 
 * This should NOT be the same as the information that is logged. The information that is passed to
 * the smart dashboard
 * should be restricted to things that affects the operator's choices during a match.
 */
public interface DashboardUpdater {

    /**
     * Called when this should update the smartdashboard.
     */
    public default void updateDashboard() {
        // do nothing by default
    }
}
