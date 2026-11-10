package scheduler;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class ConstraintChecker {

    /**
     * Checks all inviolable Hard Constraints (HC-01 to HC-09).
     * Returns null if valid, or a descriptive error message if violated.
     */
    public String validateHardConstraints(Session session, TimeSlot slot, Room room, Timetable currentTimetable, SpecialConstraint specConstraint) {
        // HC-04: Working Hours (Mon-Fri, 09:00 - 17:00)
        if (!slot.isWithinWorkingHours()) {
            return "HC-04: Working Hours violation (Session must be scheduled between 09:00-17:00 Monday-Friday).";
        }

        // HC-06: Room Type (Classroom for Theory, Laboratory for Lab)
        if (session.getSessionType() == SessionType.THEORY && room.getRoomType() != RoomType.CLASSROOM) {
            return "HC-06: Room Type mismatch (Theoretical session requires Classroom, but " + room.getRoomId() + " is Laboratory).";
        }
        if (session.getSessionType() == SessionType.LABORATORY && room.getRoomType() != RoomType.LABORATORY) {
            return "HC-06: Room Type mismatch (Laboratory session requires Laboratory, but " + room.getRoomId() + " is Classroom).";
        }

        // HC-05: Room Capacity
        if (room.getCapacity() < session.getStudentCount()) {
            return "HC-05: Room capacity deficit (" + room.getRoomId() + " capacity " + room.getCapacity() + " < required " + session.getStudentCount() + ").";
        }

        // HC-09: Room Availability (Room maintenance or pre-booked slots)
        if (!room.isAvailableAt(slot)) {
            return "HC-09: Room " + room.getRoomId() + " is unavailable/maintained during " + slot;
        }

        // HC-07: Instructor Availability
        if (session.getInstructor() != null && !session.getInstructor().isAvailableAt(slot)) {
            return "HC-07: Instructor " + session.getInstructor().getFullName() + " is unavailable during " + slot;
        }

        // Check against active assignments in the timetable
        for (Assignment active : currentTimetable.getAllAssignments()) {
            if (active.getSession().getSessionId().equals(session.getSessionId())) {
                continue; // ignore self during manual reassignment
            }

            if (active.getTimeSlot().overlapsWith(slot)) {
                // HC-02: Room Conflict
                if (active.getRoom().getRoomId().equalsIgnoreCase(room.getRoomId())) {
                    return "HC-02: Room conflict (" + room.getRoomId() + " is occupied by " + active.getSession().getSessionId() + " at " + active.getTimeSlot() + ").";
                }

                // HC-01: Instructor Conflict
                if (session.getInstructor() != null && active.getSession().getInstructor() != null &&
                        session.getInstructor().getInstructorId().equalsIgnoreCase(active.getSession().getInstructor().getInstructorId())) {
                    return "HC-01: Instructor conflict (" + session.getInstructor().getFullName() + " is teaching " + active.getSession().getSessionId() + " at " + active.getTimeSlot() + ").";
                }

                // HC-03: Year-Group Conflict
                if (session.getYearGroup() == active.getSession().getYearGroup()) {
                    return "HC-03: Year-Group conflict (Year " + session.getYearGroup() + " already has " + active.getSession().getSessionId() + " scheduled at " + active.getTimeSlot() + ").";
                }
            }
        }

        // HC-08: Fixed-Time Requirements & Specific Room (if mandatory)
        if (specConstraint != null && specConstraint.isMandatory()) {
            if ("SpecificRoom".equalsIgnoreCase(specConstraint.getConstraintType())) {
                if (!room.getRoomId().equalsIgnoreCase(specConstraint.getValue().trim())) {
                    return "HC-08: Specific room requirement violated (Session must be assigned to " + specConstraint.getValue() + ").";
                }
            }
        }

        return null; // Valid
    }

    /**
     * Checks Soft Constraints (SC-01 to SC-04).
     * Returns a list of warning strings.
     */
    public List<String> checkSoftConstraints(Session session, TimeSlot slot, Room room, Timetable currentTimetable) {
        List<String> warnings = new ArrayList<>();

        // SC-01: Theory Before Laboratory
        if (session.getSessionType() == SessionType.LABORATORY) {
            String theoryId = session.getCourseCode() + "-T";
            Assignment theoryAssn = currentTimetable.getAssignment(theoryId);
            if (theoryAssn != null) {
                // check if lab is before theory in the week
                if (slot.getDay().ordinal() < theoryAssn.getTimeSlot().getDay().ordinal() ||
                   (slot.getDay() == theoryAssn.getTimeSlot().getDay() && slot.getStartHour() < theoryAssn.getTimeSlot().getStartHour())) {
                    warnings.add("SC-01: Laboratory block scheduled before Theory lecture for " + session.getCourseCode());
                }
            }
        }

        // SC-02: Instructor Preferred Hours (e.g. Morning vs Afternoon)
        if (session.getInstructor() != null) {
            String pref = session.getInstructor().getPreferredTimePreference();
            if ("Morning".equalsIgnoreCase(pref) && slot.getStartHour() >= 12) {
                warnings.add("SC-02: " + session.getSessionId() + " placed at " + slot.getStartHour() + ":00; " + session.getInstructor().getFullName() + " prefers morning hours.");
            } else if ("Afternoon".equalsIgnoreCase(pref) && slot.getStartHour() < 12) {
                warnings.add("SC-02: " + session.getSessionId() + " placed at " + slot.getStartHour() + ":00; " + session.getInstructor().getFullName() + " prefers afternoon hours.");
            }
        }

        // SC-03: Avoiding unnecessary schedule gaps for student year-group (evaluated per day)
        List<Assignment> yearAssignments = currentTimetable.getAssignmentsForYear(session.getYearGroup());
        for (Assignment ya : yearAssignments) {
            if (ya.getTimeSlot().getDay() == slot.getDay()) {
                int gap = Math.abs(slot.getStartHour() - ya.getTimeSlot().getEndHour());
                int revGap = Math.abs(ya.getTimeSlot().getStartHour() - slot.getEndHour());
                int minGap = Math.min(gap, revGap);
                if (minGap >= 3 && minGap < 6) {
                    warnings.add("SC-03: Year " + session.getYearGroup() + " will have a " + minGap + "-hour idle gap on " + slot.getDay().getDisplayName() + ".");
                    break;
                }
            }
        }

        return warnings;
    }
}
