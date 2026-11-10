package model;

import java.util.*;

public class Timetable {
    private final Map<String, Assignment> sessionAssignments; // key: sessionId
    private final List<UnscheduledReason> unscheduledSessions;
    private final List<String> warnings;

    public Timetable() {
        this.sessionAssignments = new LinkedHashMap<>();
        this.unscheduledSessions = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public void addAssignment(Assignment assignment) {
        sessionAssignments.put(assignment.getSession().getSessionId(), assignment);
    }

    public Assignment removeAssignment(String sessionId) {
        return sessionAssignments.remove(sessionId);
    }

    public Assignment getAssignment(String sessionId) {
        return sessionAssignments.get(sessionId);
    }

    public Collection<Assignment> getAllAssignments() {
        return sessionAssignments.values();
    }

    public List<UnscheduledReason> getUnscheduledSessions() {
        return Collections.unmodifiableList(unscheduledSessions);
    }

    public void addUnscheduledSession(Session session, UnscheduledReason.ProblemType type, String details) {
        unscheduledSessions.add(new UnscheduledReason(session, type, details));
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void clear() {
        sessionAssignments.clear();
        unscheduledSessions.clear();
        warnings.clear();
    }

    public List<Assignment> getAssignmentsForYear(int yearGroup) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : sessionAssignments.values()) {
            if (a.getSession().getYearGroup() == yearGroup) {
                result.add(a);
            }
        }
        return result;
    }
}
