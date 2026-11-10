package model;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private final String roomId;
    private final String building;
    private final int capacity;
    private final RoomType roomType;
    private final List<TimeSlot> unavailableSlots;

    public Room(String roomId, String building, int capacity, RoomType roomType) {
        this.roomId = roomId;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
        this.unavailableSlots = new ArrayList<>();
    }

    public String getRoomId() {
        return roomId;
    }

    public String getBuilding() {
        return building;
    }

    public int capacity() {
        return capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public RoomType getRoomType() {
        return roomType;
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
        return roomId + " (" + roomType + ", cap=" + capacity + ")";
    }
}
