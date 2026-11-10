package io;

import model.*;
import java.io.*;
import java.util.*;

public class FileParser {
    private final List<String> parsingWarnings = new ArrayList<>();

    public List<String> getParsingWarnings() {
        return parsingWarnings;
    }

    public List<Course> parseCourses(File file) {
        List<Course> courses = new ArrayList<>();
        if (!file.exists()) {
            parsingWarnings.add("Error: File not found: " + file.getPath());
            return courses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // header check
                if (lineNum == 1 && line.toLowerCase().contains("coursecode")) continue;

                String[] parts = line.split(",");
                if (parts.length < 8) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": incomplete record -> skipped");
                    continue;
                }

                try {
                    String code = parts[0].trim();
                    String title = parts[1].trim();
                    int year = Integer.parseInt(parts[2].trim());
                    String instId = parts[3].trim();
                    int theory = Integer.parseInt(parts[4].trim());
                    int lab = Integer.parseInt(parts[5].trim());
                    int students = Integer.parseInt(parts[6].trim());
                    RoomType roomType = RoomType.fromString(parts[7].trim());

                    if (year < 1 || year > 4) {
                        parsingWarnings.add(file.getName() + ", line " + lineNum + ": year group must be 1-4 -> skipped");
                        continue;
                    }
                    courses.add(new Course(code, title, year, instId, theory, lab, students, roomType));
                } catch (NumberFormatException e) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": invalid numeric value -> record skipped");
                }
            }
        } catch (IOException e) {
            parsingWarnings.add("Error reading " + file.getName() + ": " + e.getMessage());
        }
        return courses;
    }

    public List<Instructor> parseInstructors(File file) {
        List<Instructor> instructors = new ArrayList<>();
        if (!file.exists()) {
            parsingWarnings.add("Error: File not found: " + file.getPath());
            return instructors;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (lineNum == 1 && line.toLowerCase().contains("instructorid")) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": incomplete record -> skipped");
                    continue;
                }

                String id = parts[0].trim();
                String name = parts[1].trim();
                String unavailStr = parts.length > 2 ? parts[2].trim() : "";
                String pref = parts.length > 3 ? parts[3].trim() : "None";

                Instructor inst = new Instructor(id, name, pref);
                if (!unavailStr.isEmpty() && !unavailStr.equalsIgnoreCase("None")) {
                    // e.g. "Fri 09:00-17:00" or "Friday"
                    String[] unavailParts = unavailStr.split("\\s+");
                    DayOfWeekEnum day = DayOfWeekEnum.fromString(unavailParts[0]);
                    if (day != null) {
                        if (unavailParts.length > 1 && unavailParts[1].contains("-")) {
                            String[] h = unavailParts[1].split("-");
                            int s = Integer.parseInt(h[0].split(":")[0]);
                            int e = Integer.parseInt(h[1].split(":")[0]);
                            inst.addUnavailableSlot(new TimeSlot(day, s, e - s));
                        } else {
                            inst.addUnavailableSlot(new TimeSlot(day, 9, 8)); // full day
                        }
                    }
                }
                instructors.add(inst);
            }
        } catch (Exception e) {
            parsingWarnings.add("Error reading " + file.getName() + ": " + e.getMessage());
        }
        return instructors;
    }

    public List<Room> parseRooms(File file) {
        List<Room> rooms = new ArrayList<>();
        if (!file.exists()) {
            parsingWarnings.add("Error: File not found: " + file.getPath());
            return rooms;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (lineNum == 1 && line.toLowerCase().contains("roomid")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": incomplete record -> skipped");
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    String bld = parts[1].trim();
                    int cap = Integer.parseInt(parts[2].trim());
                    RoomType type = RoomType.fromString(parts[3].trim());
                    rooms.add(new Room(id, bld, cap, type));
                } catch (NumberFormatException e) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": invalid capacity number -> record skipped");
                }
            }
        } catch (Exception e) {
            parsingWarnings.add("Error reading " + file.getName() + ": " + e.getMessage());
        }
        return rooms;
    }

    public List<SpecialConstraint> parseConstraints(File file, Set<String> validCourseCodes) {
        List<SpecialConstraint> constraints = new ArrayList<>();
        if (!file.exists()) {
            parsingWarnings.add("Error: File not found: " + file.getPath());
            return constraints;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (lineNum == 1 && line.toLowerCase().contains("coursecode")) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": incomplete record -> skipped");
                    continue;
                }

                String code = parts[0].trim();
                String type = parts[1].trim();
                String val = parts[2].trim();
                boolean isMandatory = parts[3].trim().equalsIgnoreCase("Mandatory");

                if (!validCourseCodes.contains(code)) {
                    parsingWarnings.add(file.getName() + ", line " + lineNum + ": course \"" + code + "\" not found -> request ignored");
                    continue;
                }

                constraints.add(new SpecialConstraint(code, type, val, isMandatory));
            }
        } catch (Exception e) {
            parsingWarnings.add("Error reading " + file.getName() + ": " + e.getMessage());
        }
        return constraints;
    }
}
