package model;

public enum SessionType {
    THEORY("T"),
    LABORATORY("L");

    private final String code;

    SessionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
