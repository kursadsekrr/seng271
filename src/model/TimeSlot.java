package model;

import java.util.Objects;

public class TimeSlot {
    private final DayOfWeekEnum day;
    private final int startHour; // e.g. 9 for 09:00
    private final int duration;  // in hours

    public TimeSlot(DayOfWeekEnum day, int startHour, int duration) {
        this.day = day;
        this.startHour = startHour;
        this.duration = duration;
    }

    public DayOfWeekEnum getDay() {
        return day;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return startHour + duration;
    }

    public int getDuration() {
        return duration;
    }

    public boolean overlapsWith(TimeSlot other) {
        if (other == null || this.day != other.day) {
            return false;
        }
        return this.startHour < other.getEndHour() && other.startHour < this.getEndHour();
    }

    public boolean isWithinWorkingHours() {
        return startHour >= 9 && (startHour + duration) <= 17;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return startHour == timeSlot.startHour && duration == timeSlot.duration && day == timeSlot.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startHour, duration);
    }

    @Override
    public String toString() {
        return String.format("%s %02d:00-%02d:00", day.getDisplayName(), startHour, startHour + duration);
    }
}
