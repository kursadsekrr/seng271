package model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String courseCode;
    private final String courseTitle;
    private final int yearGroup;
    private final String instructorId;
    private final int theoryHours;
    private final int labHours;
    private final int studentCount;
    private final RoomType requiredRoomType;

    public Course(String courseCode, String courseTitle, int yearGroup, String instructorId,
                  int theoryHours, int labHours, int studentCount, RoomType requiredRoomType) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.yearGroup = yearGroup;
        this.instructorId = instructorId;
        this.theoryHours = theoryHours;
        this.labHours = labHours;
        this.studentCount = studentCount;
        this.requiredRoomType = requiredRoomType;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getYearGroup() {
        return yearGroup;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public int getTheoryHours() {
        return theoryHours;
    }

    public int getLabHours() {
        return labHours;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public RoomType getRequiredRoomType() {
        return requiredRoomType;
    }

    public List<Session> createSessions(Instructor instructor) {
        List<Session> sessions = new ArrayList<>();
        if (theoryHours > 0) {
            sessions.add(new Session(courseCode, SessionType.THEORY, theoryHours, studentCount, instructor, yearGroup));
        }
        if (labHours > 0) {
            sessions.add(new Session(courseCode, SessionType.LABORATORY, labHours, studentCount, instructor, yearGroup));
        }
        return sessions;
    }
}
