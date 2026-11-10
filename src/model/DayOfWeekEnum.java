package model;

public enum DayOfWeekEnum {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday");

    private final String displayName;

    DayOfWeekEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DayOfWeekEnum fromString(String text) {
        if (text == null) return null;
        String cleaned = text.trim().toUpperCase();
        for (DayOfWeekEnum d : DayOfWeekEnum.values()) {
            if (d.name().equalsIgnoreCase(cleaned) || d.displayName.equalsIgnoreCase(cleaned) ||
                (cleaned.startsWith("MON") && d == MONDAY) ||
                (cleaned.startsWith("TUE") && d == TUESDAY) ||
                (cleaned.startsWith("WED") && d == WEDNESDAY) ||
                (cleaned.startsWith("THU") && d == THURSDAY) ||
                (cleaned.startsWith("FRI") && d == FRIDAY)) {
                return d;
            }
        }
        return null;
    }
}
