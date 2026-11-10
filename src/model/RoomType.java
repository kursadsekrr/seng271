package model;

public enum RoomType {
    CLASSROOM("Classroom"),
    LABORATORY("Laboratory");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RoomType fromString(String text) {
        if (text == null) return null;
        String cleaned = text.trim().toLowerCase();
        if (cleaned.contains("lab")) {
            return LABORATORY;
        } else if (cleaned.contains("class") || cleaned.contains("lecture")) {
            return CLASSROOM;
        }
        return null;
    }
}
