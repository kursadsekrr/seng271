package io;

import model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Exporter {

    public static void exportYearTimetable(Timetable timetable, int year, File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("=== WEEKLY TIMETABLE YEAR " + year + " ===");
            writer.println(renderYearGrid(timetable, year));
        }
    }

    public static void exportCompleteTimetable(Timetable timetable, File csvFile, File txtFile) throws IOException {
        // CSV Export
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            pw.println("CourseCode,SessionType,YearGroup,Day,StartTime,EndTime,Room,Instructor");
            for (Assignment a : timetable.getAllAssignments()) {
                pw.printf("%s,%s,%d,%s,%02d:00,%02d:00,%s,%s%n",
                        a.getSession().getCourseCode(),
                        a.getSession().getSessionType().getCode(),
                        a.getSession().getYearGroup(),
                        a.getTimeSlot().getDay().getDisplayName(),
                        a.getTimeSlot().getStartHour(),
                        a.getTimeSlot().getEndHour(),
                        a.getRoom().getRoomId(),
                        a.getSession().getInstructor() != null ? a.getSession().getInstructor().getFullName() : "N/A"
                );
            }
        }

        // TXT Export
        try (PrintWriter pw = new PrintWriter(new FileWriter(txtFile))) {
            pw.println("================================================================================");
            pw.println("                     COMPLETE DEPARTMENTAL TIMETABLE");
            pw.println("================================================================================");
            pw.printf("%-10s %-6s %-6s %-12s %-14s %-10s %-20s%n",
                    "Course", "Type", "Year", "Day", "Time", "Room", "Instructor");
            pw.println("--------------------------------------------------------------------------------");
            for (Assignment a : timetable.getAllAssignments()) {
                pw.printf("%-10s %-6s %-6d %-12s %02d:00-%02d:00   %-10s %-20s%n",
                        a.getSession().getCourseCode(),
                        a.getSession().getSessionType().getCode(),
                        a.getSession().getYearGroup(),
                        a.getTimeSlot().getDay().getDisplayName(),
                        a.getTimeSlot().getStartHour(),
                        a.getTimeSlot().getEndHour(),
                        a.getRoom().getRoomId(),
                        a.getSession().getInstructor() != null ? a.getSession().getInstructor().getFullName() : "N/A"
                );
            }
        }
    }

    public static void exportSummary(Timetable timetable, File summaryFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(summaryFile))) {
            pw.println("=== TIMETABLE GENERATION SUMMARY REPORT ===");
            int scheduled = timetable.getAllAssignments().size();
            int unscheduled = timetable.getUnscheduledSessions().size();
            pw.println("Total Scheduled Sessions: " + scheduled);
            pw.println("Total Unscheduled Sessions: " + unscheduled);
            pw.println("Total Warnings Logged: " + timetable.getWarnings().size());
            pw.println();

            pw.println("--- UNSCHEDULED SESSIONS ---");
            for (UnscheduledReason ur : timetable.getUnscheduledSessions()) {
                pw.printf("%-12s | Problem: %-12s | Details: %s%n",
                        ur.getSession().getSessionId(), ur.getProblemType(), ur.getDetails());
            }
            pw.println();

            pw.println("--- SOFT-CONSTRAINT WARNINGS ---");
            for (String w : timetable.getWarnings()) {
                pw.println("[WARN] " + w);
            }
        }
    }

    public static String renderYearGrid(Timetable timetable, int year) {
        StringBuilder sb = new StringBuilder();
        // DayOfWeekEnum[] days = DayOfWeekEnum.values();
        // 8 slots: 9 to 16 (09:00 - 17:00)
        String[][] grid = new String[8][5];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = "";
            }
        }

        for (Assignment a : timetable.getAssignmentsForYear(year)) {
            int col = a.getTimeSlot().getDay().ordinal();
            int startRow = a.getTimeSlot().getStartHour() - 9;
            for (int i = 0; i < a.getTimeSlot().getDuration(); i++) {
                int row = startRow + i;
                if (row >= 0 && row < 8) {
                    grid[row][col] = a.getSession().getSessionId() + " " + a.getRoom().getRoomId();
                }
            }
        }

        sb.append("+-------------+-----------------+-----------------+-----------------+-----------------+-----------------+\n");
        sb.append(String.format("| %-11s | %-15s | %-15s | %-15s | %-15s | %-15s |\n",
                "Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        sb.append("+-------------+-----------------+-----------------+-----------------+-----------------+-----------------+\n");

        for (int h = 9; h < 17; h++) {
            int row = h - 9;
            String timeLabel = String.format("%02d:00-%02d:00", h, h + 1);
            sb.append(String.format("| %-11s | %-15s | %-15s | %-15s | %-15s | %-15s |\n",
                    timeLabel, grid[row][0], grid[row][1], grid[row][2], grid[row][3], grid[row][4]));
        }
        sb.append("+-------------+-----------------+-----------------+-----------------+-----------------+-----------------+\n");
        sb.append("T = Theory, L = Laboratory\n");
        return sb.toString();
    }
}
