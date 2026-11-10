package model;

public class Assignment {
    private final Session session;
    private final TimeSlot timeSlot;
    private final Room room;

    public Assignment(Session session, TimeSlot timeSlot, Room room) {
        this.session = session;
        this.timeSlot = timeSlot;
        this.room = room;
    }

    public Session getSession() {
        return session;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return session.getSessionId() + " -> " + timeSlot + " @ " + room.getRoomId();
    }
}
