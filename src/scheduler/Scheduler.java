package scheduler;

import model.*;
import java.util.*;

public class Scheduler {
    private final ConstraintChecker constraintChecker;

    public Scheduler() {
        this.constraintChecker = new ConstraintChecker();
    }

    public ConstraintChecker getConstraintChecker() {
        return constraintChecker;
    }

    public void generateTimetable(List<Course> courses, List<Instructor> instructors, List<Room> rooms,
                                  List<SpecialConstraint> constraints, Timetable timetable) {
        timetable.clear();

        // 1. Map lookups
        Map<String, Instructor> instructorMap = new HashMap<>();
        for (Instructor inst : instructors) {
            instructorMap.put(inst.getInstructorId(), inst);
        }

        Map<String, List<SpecialConstraint>> constraintMap = new HashMap<>();
        for (SpecialConstraint sc : constraints) {
            constraintMap.computeIfAbsent(sc.getCourseCode(), k -> new ArrayList<>()).add(sc);
        }

        // 2. Expand all courses into session blocks
        List<Session> allSessions = new ArrayList<>();
        for (Course c : courses) {
            Instructor inst = instructorMap.get(c.getInstructorId());
            allSessions.addAll(c.createSessions(inst));
        }

        // 3. Separate fixed-time sessions (Phase 1) and flexible sessions (Phase 2)
        List<Session> fixedSessions = new ArrayList<>();
        List<Session> flexibleSessions = new ArrayList<>();

        Map<String, TimeSlot> fixedTimeSlots = new HashMap<>();
        Map<String, String> specificRooms = new HashMap<>();

        for (Session s : allSessions) {
            List<SpecialConstraint> scList = constraintMap.get(s.getCourseCode());
            boolean isFixed = false;
            if (scList != null) {
                for (SpecialConstraint sc : scList) {
                    if ("FixedTime".equalsIgnoreCase(sc.getConstraintType())) {
                        TimeSlot parsedSlot = parseTimeSlotString(sc.getValue(), s.getDurationHours());
                        if (parsedSlot != null) {
                            fixedTimeSlots.put(s.getSessionId(), parsedSlot);
                            isFixed = true;
                        }
                    } else if ("SpecificRoom".equalsIgnoreCase(sc.getConstraintType())) {
                        specificRooms.put(s.getSessionId(), sc.getValue().trim());
                    }
                }
            }
            if (isFixed) {
                fixedSessions.add(s);
            } else {
                flexibleSessions.add(s);
            }
        }

        // Sort flexible sessions: Theory first, then Lab, then descending duration and enrollment
        flexibleSessions.sort((a, b) -> {
            if (a.getSessionType() != b.getSessionType()) {
                return a.getSessionType() == SessionType.THEORY ? -1 : 1;
            }
            if (b.getDurationHours() != a.getDurationHours()) {
                return Integer.compare(b.getDurationHours(), a.getDurationHours());
            }
            return Integer.compare(b.getStudentCount(), a.getStudentCount());
        });

        // ---------------- PHASE 1: Schedule Fixed-Time Sessions ----------------
        for (Session session : fixedSessions) {
            TimeSlot targetSlot = fixedTimeSlots.get(session.getSessionId());
            String specificRoomId = specificRooms.get(session.getSessionId());

            Room assignedRoom = null;
            String lastFailureReason = "No matching room available";

            for (Room r : rooms) {
                if (specificRoomId != null && !r.getRoomId().equalsIgnoreCase(specificRoomId)) {
                    continue;
                }
                String hcViolation = constraintChecker.validateHardConstraints(session, targetSlot, r, timetable, null);
                if (hcViolation == null) {
                    assignedRoom = r;
                    break;
                } else {
                    lastFailureReason = hcViolation;
                }
            }

            if (assignedRoom != null) {
                timetable.addAssignment(new Assignment(session, targetSlot, assignedRoom));
                List<String> scWarns = constraintChecker.checkSoftConstraints(session, targetSlot, assignedRoom, timetable);
                for (String w : scWarns) timetable.addWarning(w);
            } else {
                UnscheduledReason.ProblemType prob = lastFailureReason.contains("HC-05") || lastFailureReason.contains("HC-06") ?
                        UnscheduledReason.ProblemType.NO_ROOM : UnscheduledReason.ProblemType.CONSTRAINT;
                timetable.addUnscheduledSession(session, prob, lastFailureReason);
            }
        }

        // ---------------- PHASE 2: Schedule Flexible Sessions ----------------
        DayOfWeekEnum[] days = DayOfWeekEnum.values(); // Mon - Fri

        for (Session session : flexibleSessions) {
            String specificRoomId = specificRooms.get(session.getSessionId());
            boolean placed = false;
            String bestFailureReason = "No available slot/room permutation found";
            boolean hadRoomCapacityDeficit = false;

            // Search days Mon-Fri, slots 09:00 - (17 - dur)
            outerLoop:
            for (DayOfWeekEnum day : days) {
                int maxStart = 17 - session.getDurationHours();
                for (int startHour = 9; startHour <= maxStart; startHour++) {
                    TimeSlot candidateSlot = new TimeSlot(day, startHour, session.getDurationHours());

                    for (Room room : rooms) {
                        if (specificRoomId != null && !room.getRoomId().equalsIgnoreCase(specificRoomId)) {
                            continue;
                        }

                        // Filter room type first
                        if (session.getSessionType() == SessionType.THEORY && room.getRoomType() != RoomType.CLASSROOM) continue;
                        if (session.getSessionType() == SessionType.LABORATORY && room.getRoomType() != RoomType.LABORATORY) continue;

                        if (room.getCapacity() < session.getStudentCount()) {
                            hadRoomCapacityDeficit = true;
                            bestFailureReason = "HC-05: no " + room.getRoomType().getDisplayName().toLowerCase() + " with capacity >= " + session.getStudentCount();
                            continue;
                        }

                        String hcError = constraintChecker.validateHardConstraints(session, candidateSlot, room, timetable, null);
                        if (hcError == null) {
                            timetable.addAssignment(new Assignment(session, candidateSlot, room));
                            List<String> scWarns = constraintChecker.checkSoftConstraints(session, candidateSlot, room, timetable);
                            for (String w : scWarns) timetable.addWarning(w);
                            placed = true;
                            break outerLoop;
                        } else {
                            bestFailureReason = hcError;
                        }
                    }
                }
            }

            if (!placed) {
                UnscheduledReason.ProblemType prob = hadRoomCapacityDeficit ?
                        UnscheduledReason.ProblemType.NO_ROOM : UnscheduledReason.ProblemType.CONSTRAINT;
                timetable.addUnscheduledSession(session, prob, bestFailureReason);
            }
        }
    }

    private TimeSlot parseTimeSlotString(String val, int expectedDuration) {
        try {
            // e.g., "Wed 13:00-16:00" or "Wed, 13:00-16:00" or "Friday 09:00-12:00"
            String[] parts = val.replace(",", " ").trim().split("\\s+");
            if (parts.length >= 2) {
                DayOfWeekEnum day = DayOfWeekEnum.fromString(parts[0]);
                String[] hours = parts[1].split("-");
                int start = Integer.parseInt(hours[0].split(":")[0]);
                int end = Integer.parseInt(hours[1].split(":")[0]);
                int duration = end - start;
                return new TimeSlot(day, start, duration > 0 ? duration : expectedDuration);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
