package frc.robot.controller;

/**
 * A list of domains for conflict checking.
 * 
 * Normally each subsystem is its own domain, but subsystems that interfere with
 * each other should be in the same domain so they can be safely updated together
 * without harming the robot.
 * 
 * For example if the carriage and the lift can impact each other, then they should
 * be included in the same domain so that sequences that use one or the other will
 * conflict and abort the earlier sequence.
 * 
 * This is key to being able to run multiple sequences at the same time.
 * 
 * Note there is no time domain as two sequences using different delays aren't
 * going to conflict with each other.
 */
enum Domain {
    DRIVEBASE, INTAKE, CONVEYOR, FEEDER, SHOOTER, CLIMBER, LED
    // Note the lack of non-subsystems, eg time.
};
