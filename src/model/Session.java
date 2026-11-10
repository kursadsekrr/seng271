package model;

import java.util.Objects;

public class Session {
    private final String sessionId; // e.g. SENG201-T
    private final String courseCode;
    private final SessionType sessionType;
    private final int durationHours;
    private final int studentCount;
    private final Instructor instructor;
    private final int yearGroup;

    public Session(String courseCode, SessionType sessionType, int durationHours, int studentCount, Instructor instructor, int yearGroup) {
        this.sessionId = courseCode + "-" + sessionType.getCode();
        this.courseCode = courseCode;
        this.sessionType = sessionType;
        this.durationHours = durationHours;
        this.studentCount = studentCount;
        this.instructor = instructor;
        this.yearGroup = yearGroup;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public int getYearGroup() {
        return yearGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(sessionId, session.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return sessionId + " (" + durationHours + "h, " + studentCount + " students, Year " + yearGroup + ")";
    }
}
