package model;

public class SpecialConstraint {
    private final String courseCode;
    private final String constraintType; // "FixedTime", "SpecificRoom", "UnavailableDay", "LabBeforeTheory"
    private final String value;          // e.g. "Wed 13:00-16:00" or "B202"
    private final boolean isMandatory;   // Mandatory vs Preferred

    public SpecialConstraint(String courseCode, String constraintType, String value, boolean isMandatory) {
        this.courseCode = courseCode;
        this.constraintType = constraintType;
        this.value = value;
        this.isMandatory = isMandatory;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public String getValue() {
        return value;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    @Override
    public String toString() {
        return "[" + (isMandatory ? "Mandatory" : "Preferred") + "] " + courseCode + " - " + constraintType + ": " + value;
    }
}
