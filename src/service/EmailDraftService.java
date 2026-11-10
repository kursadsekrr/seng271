package service;

import model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class EmailDraftService {

    public String generateClassroomRequestDraft(Session session, List<Assignment> existingAssignments) {
        // Suggest a conflict-free time slot for year group and instructor
        TimeSlot suggestedSlot = findSuggestedSlot(session, existingAssignments);
        String slotStr = (suggestedSlot != null) ?
                suggestedSlot.getDay().getDisplayName() + ", " + String.format("%02d:00-%02d:00", suggestedSlot.getStartHour(), suggestedSlot.getEndHour()) :
                "Flexible during standard operating hours (Mon-Fri 09:00-17:00)";

        String roomTypeStr = session.getSessionType() == SessionType.LABORATORY ? "Computer Laboratory" : "Lecture Classroom";

        StringBuilder sb = new StringBuilder();
        sb.append("Subject: Classroom Request ").append(session.getCourseCode()).append("\n\n");
        sb.append("Dear Student Affairs Office,\n\n");
        sb.append("We are preparing the department course timetable for the upcoming semester.\n");
        sb.append("A suitable classroom could not be assigned for the following session:\n\n");
        sb.append("Course: ").append(session.getCourseCode()).append(" (").append(session.getSessionType() == SessionType.THEORY ? "Theory" : "Laboratory").append(")\n");
        sb.append("Number of Students: ").append(session.getStudentCount()).append("\n");
        sb.append("Required Room Type: ").append(roomTypeStr).append("\n");
        sb.append("Preferred Time: ").append(slotStr).append("\n\n");
        sb.append("We kindly request your assistance in identifying an available room that meets these requirements.\n\n");
        sb.append("Best regards,\n");
        sb.append("Department Timetable Coordinator\n");

        return sb.toString();
    }

    private TimeSlot findSuggestedSlot(Session session, List<Assignment> existingAssignments) {
        DayOfWeekEnum[] days = DayOfWeekEnum.values();
        int dur = session.getDurationHours();

        for (DayOfWeekEnum d : days) {
            for (int s = 9; s <= (17 - dur); s++) {
                TimeSlot candidate = new TimeSlot(d, s, dur);
                if (session.getInstructor() != null && !session.getInstructor().isAvailableAt(candidate)) {
                    continue;
                }

                boolean conflict = false;
                for (Assignment a : existingAssignments) {
                    if (a.getTimeSlot().overlapsWith(candidate)) {
                        if (a.getSession().getYearGroup() == session.getYearGroup()) {
                            conflict = true;
                            break;
                        }
                        if (session.getInstructor() != null && a.getSession().getInstructor() != null &&
                                session.getInstructor().getInstructorId().equalsIgnoreCase(a.getSession().getInstructor().getInstructorId())) {
                            conflict = true;
                            break;
                        }
                    }
                }
                if (!conflict) {
                    return candidate;
                }
            }
        }
        return null;
    }

    public void saveDraftToFile(String draftContent, String courseCode, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File targetFile = new File(outputDir, "classroom_request_" + courseCode + ".txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.print(draftContent);
        }
    }
}
