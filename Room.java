public class Room {
    private String id;
    private String type;
    private int capacity;

    // "Classroom" or "Laboratory"
    public Room(String id, String type, int capacity) {
        this.id = id;
        this.type = type;
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }
    
}
