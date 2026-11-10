package model;

import java.util.ArrayList;
import java.util.List;

public class Instructor {
    private final String instructorId;
    private final String fullName;
    private final List<TimeSlot> unavailableSlots;
    private final String preferredTimePreference; // e.g. "Morning", "Afternoon", or "None"

    public Instructor(String instructorId, String fullName, String preferredTimePreference) {
        this.instructorId = instructorId;
        this.fullName = fullName;
        this.preferredTimePreference = preferredTimePreference != null ? preferredTimePreference : "None";
        this.unavailableSlots = new ArrayList<>();
    }

    public String getInstructorId() {
        return instructorId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPreferredTimePreference() {
        return preferredTimePreference;
    }

    public List<TimeSlot> getUnavailableSlots() {
        return unavailableSlots;
    }

    public void addUnavailableSlot(TimeSlot slot) {
        this.unavailableSlots.add(slot);
    }

    public boolean isAvailableAt(TimeSlot slot) {
        for (TimeSlot unavailable : unavailableSlots) {
            if (unavailable.overlapsWith(slot)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return fullName + " (" + instructorId + ")";
    }
}
