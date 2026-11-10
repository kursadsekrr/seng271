package model;

public class UnscheduledReason {
    public enum ProblemType {
        NO_ROOM,
        CONSTRAINT
    }

    private final Session session;
    private final ProblemType problemType;
    private final String details;

    public UnscheduledReason(Session session, ProblemType problemType, String details) {
        this.session = session;
        this.problemType = problemType;
        this.details = details;
    }

    public Session getSession() {
        return session;
    }

    public ProblemType getProblemType() {
        return problemType;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return session.getSessionId() + " | " + problemType + " | " + details;
    }
}
