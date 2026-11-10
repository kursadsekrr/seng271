package ui;

import io.Exporter;
import io.FileParser;
import model.*;
import scheduler.Scheduler;
import service.EmailDraftService;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConsoleUI {
    private final Scanner scanner;
    private final FileParser fileParser;
    private final Scheduler scheduler;
    private final EmailDraftService emailService;
    private final Timetable timetable;

    private List<Course> loadedCourses = new ArrayList<>();
    private List<Instructor> loadedInstructors = new ArrayList<>();
    private List<Room> loadedRooms = new ArrayList<>();
    private List<SpecialConstraint> loadedConstraints = new ArrayList<>();

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.fileParser = new FileParser();
        this.scheduler = new Scheduler();
        this.emailService = new EmailDraftService();
        this.timetable = new Timetable();
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Select an option: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleLoadData();
                    break;
                case "2":
                    handleGenerateTimetable();
                    break;
                case "3":
                    handleViewYearTimetable();
                    break;
                case "4":
                    handleViewAllScheduled();
                    break;
                case "5":
                    handleViewUnscheduledAndWarnings();
                    break;
                case "6":
                    handleManualModification();
                    break;
                case "7":
                    handleClassroomRequestEmail();
                    break;
                case "8":
                    handleSaveTimetable();
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting Timetable Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose a valid option (0-8).\n");
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("=================================================");
        System.out.println(" DEPARTMENT COURSE TIMETABLE MANAGEMENT SYSTEM  ");
        System.out.println("         Software Engineering Department         ");
        System.out.println("=================================================");
        System.out.println("1. Load input data");
        System.out.println("2. Generate timetable automatically");
        System.out.println("3. View timetable by year group");
        System.out.println("4. View all scheduled sessions");
        System.out.println("5. View unscheduled sessions and warnings");
        System.out.println("6. Modify / remove an assignment");
        System.out.println("7. Generate classroom request email");
        System.out.println("8. Save timetable to file");
        System.out.println("0. Exit");
        System.out.println("=================================================");
    }

    private void handleLoadData() {
        System.out.println("\n=== LOAD INPUT DATA ===");
        fileParser.getParsingWarnings().clear();

        File coursesFile = new File("data/courses.csv");
        File instFile = new File("data/instructors.txt");
        File roomsFile = new File("data/rooms.csv");
        File constraintsFile = new File("data/constraints.txt");

        System.out.println("Reading " + coursesFile.getPath() + " ...");
        loadedCourses = fileParser.parseCourses(coursesFile);

        System.out.println("Reading " + instFile.getPath() + " ...");
        loadedInstructors = fileParser.parseInstructors(instFile);

        System.out.println("Reading " + roomsFile.getPath() + " ...");
        loadedRooms = fileParser.parseRooms(roomsFile);

        Set<String> validCodes = new HashSet<>();
        for (Course c : loadedCourses) validCodes.add(c.getCourseCode());

        System.out.println("Reading " + constraintsFile.getPath() + " ...");
        loadedConstraints = fileParser.parseConstraints(constraintsFile, validCodes);

        System.out.printf("%d courses loaded%n", loadedCourses.size());
        System.out.printf("%d instructors loaded%n", loadedInstructors.size());
        System.out.printf("%d rooms loaded%n", loadedRooms.size());
        System.out.printf("%d requests loaded%n", loadedConstraints.size());

        if (!fileParser.getParsingWarnings().isEmpty()) {
            System.out.println("\nWARNINGS:");
            for (String w : fileParser.getParsingWarnings()) {
                System.out.println(w);
            }
            System.out.println("Data loaded with " + fileParser.getParsingWarnings().size() + " warnings.");
        } else {
            System.out.println("All data loaded successfully with 0 warnings.");
        }
        System.out.println("Press Enter to return to the main menu...");
        scanner.nextLine();
    }

    private void handleGenerateTimetable() {
        if (loadedCourses.isEmpty()) {
            System.out.println("Please load input data first (Option 1).");
            return;
        }

        System.out.println("\n=== AUTOMATIC TIMETABLE GENERATION ===");
        scheduler.generateTimetable(loadedCourses, loadedInstructors, loadedRooms, loadedConstraints, timetable);

        for (Assignment a : timetable.getAllAssignments()) {
            System.out.printf("[OK]   %-12s %-18s %s%n",
                    a.getSession().getSessionId(),
                    a.getTimeSlot().toString(),
                    a.getRoom().getRoomId());
        }

        for (UnscheduledReason ur : timetable.getUnscheduledSessions()) {
            System.out.printf("[FAIL] %-12s %s: %s%n",
                    ur.getSession().getSessionId(),
                    ur.getProblemType(),
                    ur.getDetails());
        }

        for (String w : timetable.getWarnings()) {
            System.out.println("[WARN] " + w);
        }

        int totalSessions = timetable.getAllAssignments().size() + timetable.getUnscheduledSessions().size();
        System.out.printf("%nSUMMARY: Total sessions: %d | Scheduled: %d | Unscheduled: %d | Warnings: %d%n",
                totalSessions, timetable.getAllAssignments().size(), timetable.getUnscheduledSessions().size(), timetable.getWarnings().size());
    }

    private void handleViewYearTimetable() {
        System.out.print("Enter Year Group (1-4): ");
        String yearStr = scanner.nextLine().trim();
        try {
            int year = Integer.parseInt(yearStr);
            if (year < 1 || year > 4) {
                System.out.println("Invalid year. Must be between 1 and 4.");
                return;
            }
            System.out.println(Exporter.renderYearGrid(timetable, year));

            System.out.println("Scheduled sessions for Year " + year + ":");
            for (Assignment a : timetable.getAssignmentsForYear(year)) {
                System.out.printf(" - %-10s %-18s %s (Instructor: %s)%n",
                        a.getSession().getSessionId(),
                        a.getTimeSlot(),
                        a.getRoom().getRoomId(),
                        a.getSession().getInstructor() != null ? a.getSession().getInstructor().getFullName() : "N/A");
            }

            System.out.println("\nUnscheduled for Year " + year + ":");
            boolean hasUnscheduled = false;
            for (UnscheduledReason ur : timetable.getUnscheduledSessions()) {
                if (ur.getSession().getYearGroup() == year) {
                    System.out.printf(" - %-10s (%dh) -> %s%n", ur.getSession().getSessionId(), ur.getSession().getDurationHours(), ur.getDetails());
                    hasUnscheduled = true;
                }
            }
            if (!hasUnscheduled) System.out.println(" (None)");
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input.");
        }
    }

    private void handleViewAllScheduled() {
        System.out.println("\n=== ALL SCHEDULED SESSIONS ===");
        if (timetable.getAllAssignments().isEmpty()) {
            System.out.println("No sessions currently scheduled. Run option 2 first.");
            return;
        }

        System.out.printf("%-12s %-6s %-16s %-10s %-20s%n", "Session", "Year", "Day & Time", "Room", "Instructor");
        System.out.println("----------------------------------------------------------------------");
        for (Assignment a : timetable.getAllAssignments()) {
            System.out.printf("%-12s Year %-4d %-16s %-10s %-20s%n",
                    a.getSession().getSessionId(),
                    a.getSession().getYearGroup(),
                    a.getTimeSlot().toString(),
                    a.getRoom().getRoomId(),
                    a.getSession().getInstructor() != null ? a.getSession().getInstructor().getFullName() : "N/A");
        }
    }

    private void handleViewUnscheduledAndWarnings() {
        System.out.println("\n=== UNSCHEDULED SESSIONS ===");
        if (timetable.getUnscheduledSessions().isEmpty()) {
            System.out.println("All sessions successfully scheduled!");
        } else {
            System.out.printf("%-4s | %-12s | %-5s | %-6s | %-8s | %-12s | %s%n",
                    "#", "Session", "Year", "Hours", "Students", "Problem Type", "Reason");
            System.out.println("--------------------------------------------------------------------------------------------");
            int idx = 1;
            for (UnscheduledReason ur : timetable.getUnscheduledSessions()) {
                System.out.printf("%-4d | %-12s | %-5d | %-6d | %-8d | %-12s | %s%n",
                        idx++,
                        ur.getSession().getSessionId(),
                        ur.getSession().getYearGroup(),
                        ur.getSession().getDurationHours(),
                        ur.getSession().getStudentCount(),
                        ur.getProblemType(),
                        ur.getDetails());
            }
        }

        System.out.println("\n=== SOFT-CONSTRAINT WARNINGS ===");
        if (timetable.getWarnings().isEmpty()) {
            System.out.println("No warnings recorded.");
        } else {
            for (String w : timetable.getWarnings()) {
                System.out.println("[!] " + w);
            }
        }
    }

    private void handleManualModification() {
        System.out.println("\n=== MODIFY AN ASSIGNMENT ===");
        System.out.print("Session to modify (e.g., SENG203-L): ");
        String sessionId = scanner.nextLine().trim();

        Assignment current = timetable.getAssignment(sessionId);
        if (current == null) {
            System.out.println("Session " + sessionId + " is not currently scheduled.");
            return;
        }

        System.out.printf("Current assignment: %s @ %s%n", current.getTimeSlot(), current.getRoom().getRoomId());

        System.out.print("New day (Monday-Friday): ");
        DayOfWeekEnum day = DayOfWeekEnum.fromString(scanner.nextLine().trim());
        if (day == null) {
            System.out.println("Invalid day entered.");
            return;
        }

        System.out.print("New start time (e.g. 13 or 13:00): ");
        String startStr = scanner.nextLine().trim().replace(":00", "");
        int startHour;
        try {
            startHour = Integer.parseInt(startStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid hour format.");
            return;
        }

        System.out.print("New room (e.g. A101, B202): ");
        String roomId = scanner.nextLine().trim();
        Room targetRoom = null;
        for (Room r : loadedRooms) {
            if (r.getRoomId().equalsIgnoreCase(roomId)) {
                targetRoom = r;
                break;
            }
        }

        if (targetRoom == null) {
            System.out.println("Room " + roomId + " does not exist.");
            return;
        }

        TimeSlot newSlot = new TimeSlot(day, startHour, current.getSession().getDurationHours());

        System.out.println("Checking hard constraints...");
        String error = scheduler.getConstraintChecker().validateHardConstraints(current.getSession(), newSlot, targetRoom, timetable, null);
        if (error != null) {
            System.out.println("[X] Hard constraint violated: " + error);
            System.out.println("Modification REJECTED. The current assignment is kept.");
            return;
        }

        System.out.println("[OK] All hard constraints satisfied.");
        List<String> softWarnings = scheduler.getConstraintChecker().checkSoftConstraints(current.getSession(), newSlot, targetRoom, timetable);
        for (String w : softWarnings) {
            System.out.println("[!] " + w);
        }

        System.out.print("Apply the change? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(confirm)) {
            timetable.removeAssignment(sessionId);
            timetable.addAssignment(new Assignment(current.getSession(), newSlot, targetRoom));
            System.out.printf("Assignment updated: %s -> %s, %s%n", sessionId, newSlot, targetRoom.getRoomId());
        } else {
            System.out.println("Modification cancelled.");
        }
    }

    private void handleClassroomRequestEmail() {
        System.out.println("\n=== CLASSROOM REQUEST EMAIL ===");
        List<Session> noRoomSessions = new ArrayList<>();
        for (UnscheduledReason ur : timetable.getUnscheduledSessions()) {
            if (ur.getProblemType() == UnscheduledReason.ProblemType.NO_ROOM) {
                noRoomSessions.add(ur.getSession());
            }
        }

        if (noRoomSessions.isEmpty()) {
            System.out.println("There are no unscheduled sessions tagged with NO ROOM deficit.");
            return;
        }

        System.out.println("Sessions without a suitable room:");
        for (int i = 0; i < noRoomSessions.size(); i++) {
            Session s = noRoomSessions.get(i);
            System.out.printf("%d. %s (Year %d, %d students, %s, %d h)%n",
                    i + 1, s.getSessionId(), s.getYearGroup(), s.getStudentCount(), s.getSessionType(), s.getDurationHours());
        }

        System.out.print("Select a session index: ");
        try {
            int sel = Integer.parseInt(scanner.nextLine().trim());
            if (sel < 1 || sel > noRoomSessions.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            Session selected = noRoomSessions.get(sel - 1);
            String draft = emailService.generateClassroomRequestDraft(selected, new ArrayList<>(timetable.getAllAssignments()));
            System.out.println("\n--- EMAIL DRAFT ---");
            System.out.println(draft);
            System.out.println("-------------------");

            System.out.print("Save this email to a file? (y/n): ");
            String ans = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(ans)) {
                File outDir = new File("output");
                emailService.saveDraftToFile(draft, selected.getCourseCode(), outDir);
                System.out.println("Saved to output/classroom_request_" + selected.getCourseCode() + ".txt");
            }
        } catch (Exception e) {
            System.out.println("Error generating draft: " + e.getMessage());
        }
    }

    private void handleSaveTimetable() {
        System.out.println("\n=== SAVE TIMETABLE TO FILE ===");
        File outDir = new File("output");
        if (!outDir.exists()) outDir.mkdirs();

        try {
            for (int y = 1; y <= 4; y++) {
                File yf = new File(outDir, "timetable_year_" + y + ".txt");
                Exporter.exportYearTimetable(timetable, y, yf);
            }
            File csv = new File(outDir, "complete_timetable.csv");
            File txt = new File(outDir, "complete_timetable.txt");
            Exporter.exportCompleteTimetable(timetable, csv, txt);

            File summary = new File(outDir, "timetable_summary.txt");
            Exporter.exportSummary(timetable, summary);

            System.out.println("Successfully saved timetables to output/ directory:");
            System.out.println(" - output/timetable_year_1.txt ... year_4.txt");
            System.out.println(" - output/complete_timetable.csv");
            System.out.println(" - output/complete_timetable.txt");
            System.out.println(" - output/timetable_summary.txt");
        } catch (IOException e) {
            System.out.println("Failed to write output files: " + e.getMessage());
        }
    }
}
